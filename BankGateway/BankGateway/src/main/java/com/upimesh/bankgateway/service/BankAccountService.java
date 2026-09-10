package com.upimesh.bankgateway.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.upimesh.bankgateway.client.BankClient;
import com.upimesh.bankgateway.client.BankClientRegistry;
import com.upimesh.bankgateway.exception.*;
import com.upimesh.bankgateway.model.entity.LinkedBankAccount;
import com.upimesh.bankgateway.model.enums.BankCode;
import com.upimesh.bankgateway.model.enums.LinkStatus;
import com.upimesh.bankgateway.model.request.BalanceCheckRequest;
import com.upimesh.bankgateway.model.request.LinkBankAccountRequest;
import com.upimesh.bankgateway.model.response.BalanceResponse;
import com.upimesh.bankgateway.model.response.IfscResponse;
import com.upimesh.bankgateway.model.response.LinkedAccountResponse;
import com.upimesh.bankgateway.repository.LinkedBankAccountRepository;
import com.upimesh.bankgateway.util.AccountEncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BankAccountService {

    private static final int MAX_ACCOUNTS_PER_USER = 5;

    private final LinkedBankAccountRepository accountRepo;
    private final IfscService ifscService;
    private final BankClientRegistry registry;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${balance.cache-ttl-seconds:30}")
    private int balanceCacheTtlSeconds;

    @Transactional
    public LinkedAccountResponse linkBankAccount(LinkBankAccountRequest request) {
        log.info("Linking bank account request | user={} | ifsc={}", request.getUserUpiId(), request.getIfscCode());

        // 1. Validate IFSC
        IfscResponse ifscResponse = ifscService.getIfscDetails(request.getIfscCode());

        // 2. Check max accounts linked (ACTIVE status)
        long activeCount = accountRepo.countByUserUpiIdAndStatus(request.getUserUpiId(), LinkStatus.ACTIVE);
        if (activeCount >= MAX_ACCOUNTS_PER_USER) {
            throw new MaxAccountsLinkedException("Cannot link account. Maximum limit of " + MAX_ACCOUNTS_PER_USER + " linked accounts reached.");
        }

        // 3. Check duplicate (ACTIVE account already linked for user and bank code)
        boolean exists = accountRepo.existsByUserUpiIdAndBankCodeAndStatus(
                request.getUserUpiId(), ifscResponse.getBankCode(), LinkStatus.ACTIVE);
        if (exists) {
            throw new AccountAlreadyLinkedException("An active account with " + ifscResponse.getBankCode().getDisplayName() + " is already linked to this UPI handle.");
        }

        // 4. Verify account with bank client
        BankClient client = registry.getClientByIfsc(request.getIfscCode());
        Map<String, String> verification = client.verifyAccount(request.getAccountNumber(), request.getIfscCode());
        if (verification == null || verification.containsKey("error")) {
            throw new BankVerificationException("Bank verification failed: " + (verification != null ? verification.get("error") : "Empty response"));
        }

        // 5. Register/Link account with Bank system to get a reference token
        String bankReferenceToken = client.linkAccount(request.getAccountNumber(), request.getIfscCode(), request.getUserUpiId());

        // 6. Encrypt account number
        String encryptedAccount = AccountEncryptionUtil.encrypt(request.getAccountNumber());
        String maskedAccount = AccountEncryptionUtil.mask(request.getAccountNumber());

        // 7. Handle primary status
        if (request.isSetPrimary()) {
            accountRepo.clearAllPrimary(request.getUserUpiId());
        } else {
            // If it's the first active account, make it primary automatically
            if (activeCount == 0) {
                request.setSetPrimary(true);
            }
        }

        // 8. Save LinkedBankAccount
        String accountId = "ACC" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        LinkedBankAccount entity = LinkedBankAccount.builder()
                .accountId(accountId)
                .userUpiId(request.getUserUpiId())
                .bankCode(ifscResponse.getBankCode())
                .encryptedAccountNumber(encryptedAccount)
                .maskedAccountNumber(maskedAccount)
                .ifscCode(request.getIfscCode())
                .bankName(ifscResponse.getBankName())
                .branchName(ifscResponse.getBranchName())
                .city(ifscResponse.getCity())
                .accountType(request.getAccountType())
                .accountHolderName(verification.get("holderName"))
                .status(LinkStatus.ACTIVE)
                .isPrimary(request.isSetPrimary())
                .bankReferenceToken(bankReferenceToken)
                .lastVerifiedAt(LocalDateTime.now())
                .build();

        entity = accountRepo.save(entity);
        log.info("Successfully linked account | accountId={} | primary={}", accountId, entity.isPrimary());

        return mapToResponse(entity);
    }

    public List<LinkedAccountResponse> getLinkedAccounts(String userUpiId) {
        log.info("Fetching linked accounts for user: {}", userUpiId);
        List<LinkedBankAccount> accounts = accountRepo.findByUserUpiIdOrderByIsPrimaryDescLinkedAtDesc(userUpiId);
        return accounts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public BalanceResponse checkBalance(BalanceCheckRequest request) {
        log.info("Checking balance | accountId={} | user={}", request.getAccountId(), request.getUserUpiId());

        // 1. Fetch account
        LinkedBankAccount account = accountRepo.findByAccountId(request.getAccountId())
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + request.getAccountId()));

        // 2. Verify ownership
        if (!account.getUserUpiId().equalsIgnoreCase(request.getUserUpiId())) {
            throw new AccountNotFoundException("Unauthorized access to account details");
        }

        // 3. Read from Redis Cache
        String cacheKey = "balance:" + request.getAccountId();
        try {
            String cachedJson = redisTemplate.opsForValue().get(cacheKey);
            if (cachedJson != null) {
                log.info("Balance found in Redis cache for accountId: {}", request.getAccountId());
                BalanceResponse cachedResponse = objectMapper.readValue(cachedJson, BalanceResponse.class);
                cachedResponse.setFromCache(true);
                return cachedResponse;
            }
        } catch (Exception e) {
            log.warn("Failed to retrieve balance from Redis cache", e);
        }

        // 4. Query Bank
        String decryptedAccount = AccountEncryptionUtil.decrypt(account.getEncryptedAccountNumber());
        BankClient client = registry.getClientOrThrow(account.getBankCode());

        Map<String, Object> balanceData = client.fetchBalance(decryptedAccount, account.getIfscCode(), account.getBankReferenceToken());
        if (balanceData == null || balanceData.containsKey("error")) {
            throw new BankVerificationException("Failed to fetch balance from bank: " + (balanceData != null ? balanceData.get("error") : "Empty response"));
        }

        BigDecimal available = (BigDecimal) balanceData.get("available");
        BigDecimal ledger = (BigDecimal) balanceData.get("ledger");
        String currency = (String) balanceData.get("currency");

        BalanceResponse response = BalanceResponse.builder()
                .accountId(account.getAccountId())
                .maskedAccountNumber(account.getMaskedAccountNumber())
                .bankName(account.getBankName())
                .availableBalance(available)
                .ledgerBalance(ledger)
                .currency(currency != null ? currency : "INR")
                .fetchedAt(LocalDateTime.now())
                .fromCache(false)
                .build();

        // 5. Cache in Redis
        try {
            String json = objectMapper.writeValueAsString(response);
            redisTemplate.opsForValue().set(cacheKey, json, Duration.ofSeconds(balanceCacheTtlSeconds));
            log.info("Cached balance in Redis for 30s: {}", cacheKey);
        } catch (Exception e) {
            log.warn("Failed to cache balance in Redis", e);
        }

        return response;
    }

    @Transactional
    public LinkedAccountResponse setPrimaryAccount(String accountId, String userUpiId) {
        log.info("Setting primary account | accountId={} | user={}", accountId, userUpiId);

        LinkedBankAccount account = accountRepo.findByAccountId(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountId));

        if (!account.getUserUpiId().equalsIgnoreCase(userUpiId)) {
            throw new AccountNotFoundException("Unauthorized access to account details");
        }

        if (account.getStatus() != LinkStatus.ACTIVE) {
            throw new InvalidTransactionException("Cannot set inactive account as primary");
        }

        // Clear existing primary accounts
        accountRepo.clearAllPrimary(userUpiId);

        // Set as primary
        account.setPrimary(true);
        account = accountRepo.save(account);

        log.info("Account set as primary: {}", accountId);
        return mapToResponse(account);
    }

    @Transactional
    public void deactivateAccount(String accountId, String userUpiId) {
        log.info("Deactivating account | accountId={} | user={}", accountId, userUpiId);

        LinkedBankAccount account = accountRepo.findByAccountId(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountId));

        if (!account.getUserUpiId().equalsIgnoreCase(userUpiId)) {
            throw new AccountNotFoundException("Unauthorized access to account details");
        }

        if (account.isPrimary()) {
            throw new InvalidTransactionException("Cannot remove primary account. Set another as primary first.");
        }

        account.setStatus(LinkStatus.DEACTIVATED);
        accountRepo.save(account);
        log.info("Account successfully deactivated: {}", accountId);
    }

    private LinkedAccountResponse mapToResponse(LinkedBankAccount account) {
        return LinkedAccountResponse.builder()
                .accountId(account.getAccountId())
                .userUpiId(account.getUserUpiId())
                .bankCode(account.getBankCode())
                .bankName(account.getBankName())
                .maskedAccountNumber(account.getMaskedAccountNumber())
                .ifscCode(account.getIfscCode())
                .branchName(account.getBranchName())
                .city(account.getCity())
                .accountType(account.getAccountType())
                .accountHolderName(account.getAccountHolderName())
                .status(account.getStatus())
                .isPrimary(account.isPrimary())
                .linkedAt(account.getLinkedAt())
                .build();
    }

    public LinkedBankAccount getPrimaryAccount(String userUpiId) {
        return accountRepo.findByUserUpiIdAndIsPrimaryTrue(userUpiId)
                .orElseGet(() -> accountRepo.findByUserUpiIdAndStatus(userUpiId, LinkStatus.ACTIVE)
                        .stream().findFirst().orElse(null));
    }
}
