package com.upimesh.bankgateway.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.upimesh.bankgateway.client.BankClient;
import com.upimesh.bankgateway.client.BankClientRegistry;
import com.upimesh.bankgateway.exception.*;
import com.upimesh.bankgateway.model.entity.LinkedBankAccount;
import com.upimesh.bankgateway.model.enums.AccountType;
import com.upimesh.bankgateway.model.enums.BankCode;
import com.upimesh.bankgateway.model.enums.LinkStatus;
import com.upimesh.bankgateway.model.request.BalanceCheckRequest;
import com.upimesh.bankgateway.model.request.LinkBankAccountRequest;
import com.upimesh.bankgateway.model.response.BalanceResponse;
import com.upimesh.bankgateway.model.response.IfscResponse;
import com.upimesh.bankgateway.model.response.LinkedAccountResponse;
import com.upimesh.bankgateway.repository.LinkedBankAccountRepository;
import com.upimesh.bankgateway.util.AccountEncryptionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BankAccountServiceTest {

    @Mock
    private LinkedBankAccountRepository accountRepo;

    @Mock
    private IfscService ifscService;

    @Mock
    private BankClientRegistry registry;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private BankClient bankClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private BankAccountService bankAccountService;

    @BeforeEach
    public void setUp() {
        objectMapper.findAndRegisterModules();
        ReflectionTestUtils.setField(bankAccountService, "objectMapper", objectMapper);
        ReflectionTestUtils.setField(bankAccountService, "balanceCacheTtlSeconds", 30);
    }

    @Test
    public void linkAccountSuccess() {
        LinkBankAccountRequest request = LinkBankAccountRequest.builder()
                .userUpiId("user@upimesh")
                .accountNumber("123456789012")
                .ifscCode("HDFC0001234")
                .accountType(AccountType.SAVINGS)
                .setPrimary(true)
                .build();

        IfscResponse ifscResponse = IfscResponse.builder()
                .ifscCode("HDFC0001234")
                .bankCode(BankCode.HDFC)
                .bankName("HDFC Bank")
                .branchName("Mumbai Branch")
                .city("Mumbai")
                .build();

        Map<String, String> verifyMap = Map.of(
                "holderName", "John Doe",
                "active", "true"
        );

        when(ifscService.getIfscDetails("HDFC0001234")).thenReturn(ifscResponse);
        when(accountRepo.countByUserUpiIdAndStatus("user@upimesh", LinkStatus.ACTIVE)).thenReturn(0L);
        when(accountRepo.existsByUserUpiIdAndBankCodeAndStatus("user@upimesh", BankCode.HDFC, LinkStatus.ACTIVE)).thenReturn(false);
        when(registry.getClientByIfsc("HDFC0001234")).thenReturn(bankClient);
        when(bankClient.verifyAccount("123456789012", "HDFC0001234")).thenReturn(verifyMap);
        when(bankClient.linkAccount("123456789012", "HDFC0001234", "user@upimesh")).thenReturn("TOKEN123");
        when(accountRepo.save(any(LinkedBankAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LinkedAccountResponse response = bankAccountService.linkBankAccount(request);

        assertNotNull(response);
        assertEquals(LinkStatus.ACTIVE, response.getStatus());
        assertEquals("John Doe", response.getAccountHolderName());
        assertTrue(response.isPrimary());
        verify(accountRepo, times(1)).clearAllPrimary("user@upimesh");
        verify(accountRepo, times(1)).save(any(LinkedBankAccount.class));
    }

    @Test
    public void linkAccountDuplicateBank() {
        LinkBankAccountRequest request = LinkBankAccountRequest.builder()
                .userUpiId("user@upimesh")
                .accountNumber("123456789012")
                .ifscCode("HDFC0001234")
                .accountType(AccountType.SAVINGS)
                .build();

        IfscResponse ifscResponse = IfscResponse.builder()
                .ifscCode("HDFC0001234")
                .bankCode(BankCode.HDFC)
                .build();

        when(ifscService.getIfscDetails("HDFC0001234")).thenReturn(ifscResponse);
        when(accountRepo.countByUserUpiIdAndStatus("user@upimesh", LinkStatus.ACTIVE)).thenReturn(1L);
        when(accountRepo.existsByUserUpiIdAndBankCodeAndStatus("user@upimesh", BankCode.HDFC, LinkStatus.ACTIVE)).thenReturn(true);

        assertThrows(AccountAlreadyLinkedException.class, () -> {
            bankAccountService.linkBankAccount(request);
        });
    }

    @Test
    public void maxAccountsReached() {
        LinkBankAccountRequest request = LinkBankAccountRequest.builder()
                .userUpiId("user@upimesh")
                .accountNumber("123456789012")
                .ifscCode("HDFC0001234")
                .accountType(AccountType.SAVINGS)
                .build();

        IfscResponse ifscResponse = IfscResponse.builder()
                .ifscCode("HDFC0001234")
                .bankCode(BankCode.HDFC)
                .build();

        when(ifscService.getIfscDetails("HDFC0001234")).thenReturn(ifscResponse);
        when(accountRepo.countByUserUpiIdAndStatus("user@upimesh", LinkStatus.ACTIVE)).thenReturn(5L);

        assertThrows(MaxAccountsLinkedException.class, () -> {
            bankAccountService.linkBankAccount(request);
        });
    }

    @Test
    public void balanceCheckFromCache() throws Exception {
        BalanceCheckRequest request = new BalanceCheckRequest("ACC123", "user@upimesh");
        LinkedBankAccount account = LinkedBankAccount.builder()
                .accountId("ACC123")
                .userUpiId("user@upimesh")
                .bankName("HDFC Bank")
                .maskedAccountNumber("XXXX XXXX 1234")
                .build();

        BalanceResponse cachedResponse = BalanceResponse.builder()
                .accountId("ACC123")
                .availableBalance(BigDecimal.valueOf(5000))
                .ledgerBalance(BigDecimal.valueOf(5500))
                .currency("INR")
                .build();

        when(accountRepo.findByAccountId("ACC123")).thenReturn(Optional.of(account));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("balance:ACC123")).thenReturn(objectMapper.writeValueAsString(cachedResponse));

        BalanceResponse response = bankAccountService.checkBalance(request);

        assertNotNull(response);
        assertTrue(response.isFromCache());
        assertEquals(BigDecimal.valueOf(5000), response.getAvailableBalance());
        verifyNoInteractions(registry);
    }

    @Test
    public void balanceCheckFromBank() {
        BalanceCheckRequest request = new BalanceCheckRequest("ACC123", "user@upimesh");
        
        String plainAccountNumber = "123456789012";
        String encryptedAccount = AccountEncryptionUtil.encrypt(plainAccountNumber);

        LinkedBankAccount account = LinkedBankAccount.builder()
                .accountId("ACC123")
                .userUpiId("user@upimesh")
                .bankCode(BankCode.HDFC)
                .ifscCode("HDFC0001234")
                .bankReferenceToken("TOKEN123")
                .encryptedAccountNumber(encryptedAccount)
                .maskedAccountNumber("XXXX XXXX 1234")
                .bankName("HDFC Bank")
                .build();

        Map<String, Object> mockBal = Map.of(
                "available", BigDecimal.valueOf(10000),
                "ledger", BigDecimal.valueOf(10500),
                "currency", "INR"
        );

        when(accountRepo.findByAccountId("ACC123")).thenReturn(Optional.of(account));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("balance:ACC123")).thenReturn(null);
        when(registry.getClientOrThrow(BankCode.HDFC)).thenReturn(bankClient);
        when(bankClient.fetchBalance(plainAccountNumber, "HDFC0001234", "TOKEN123")).thenReturn(mockBal);

        BalanceResponse response = bankAccountService.checkBalance(request);

        assertNotNull(response);
        assertFalse(response.isFromCache());
        assertEquals(BigDecimal.valueOf(10000), response.getAvailableBalance());
        verify(valueOperations, times(1)).set(eq("balance:ACC123"), any(String.class), any(Duration.class));
    }

    @Test
    public void invalidIfscRejects() {
        LinkBankAccountRequest request = LinkBankAccountRequest.builder()
                .userUpiId("user@upimesh")
                .accountNumber("123456789012")
                .ifscCode("INVALIDIFSC")
                .accountType(AccountType.SAVINGS)
                .build();

        when(ifscService.getIfscDetails("INVALIDIFSC")).thenThrow(new InvalidIfscException("Invalid IFSC code format"));

        assertThrows(InvalidIfscException.class, () -> {
            bankAccountService.linkBankAccount(request);
        });
    }

    @Test
    public void bankVerificationFailureRejects() {
        LinkBankAccountRequest request = LinkBankAccountRequest.builder()
                .userUpiId("user@upimesh")
                .accountNumber("123456789012")
                .ifscCode("HDFC0001234")
                .accountType(AccountType.SAVINGS)
                .build();

        IfscResponse ifscResponse = IfscResponse.builder()
                .ifscCode("HDFC0001234")
                .bankCode(BankCode.HDFC)
                .build();

        when(ifscService.getIfscDetails("HDFC0001234")).thenReturn(ifscResponse);
        when(accountRepo.countByUserUpiIdAndStatus("user@upimesh", LinkStatus.ACTIVE)).thenReturn(0L);
        when(registry.getClientByIfsc("HDFC0001234")).thenReturn(bankClient);
        when(bankClient.verifyAccount("123456789012", "HDFC0001234")).thenReturn(Map.of("error", "Invalid Account"));

        assertThrows(BankVerificationException.class, () -> {
            bankAccountService.linkBankAccount(request);
        });
    }

    @Test
    public void setPrimaryAccount() {
        LinkedBankAccount account = LinkedBankAccount.builder()
                .accountId("ACC123")
                .userUpiId("user@upimesh")
                .status(LinkStatus.ACTIVE)
                .isPrimary(false)
                .build();

        when(accountRepo.findByAccountId("ACC123")).thenReturn(Optional.of(account));
        when(accountRepo.save(any(LinkedBankAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LinkedAccountResponse response = bankAccountService.setPrimaryAccount("ACC123", "user@upimesh");

        assertNotNull(response);
        assertTrue(response.isPrimary());
        verify(accountRepo, times(1)).clearAllPrimary("user@upimesh");
    }

    @Test
    public void deactivateNonPrimaryAccount() {
        LinkedBankAccount account = LinkedBankAccount.builder()
                .accountId("ACC123")
                .userUpiId("user@upimesh")
                .isPrimary(false)
                .status(LinkStatus.ACTIVE)
                .build();

        when(accountRepo.findByAccountId("ACC123")).thenReturn(Optional.of(account));

        bankAccountService.deactivateAccount("ACC123", "user@upimesh");

        assertEquals(LinkStatus.DEACTIVATED, account.getStatus());
        verify(accountRepo, times(1)).save(account);
    }

    @Test
    public void cannotDeactivatePrimaryAccount() {
        LinkedBankAccount account = LinkedBankAccount.builder()
                .accountId("ACC123")
                .userUpiId("user@upimesh")
                .isPrimary(true)
                .status(LinkStatus.ACTIVE)
                .build();

        when(accountRepo.findByAccountId("ACC123")).thenReturn(Optional.of(account));

        assertThrows(InvalidTransactionException.class, () -> {
            bankAccountService.deactivateAccount("ACC123", "user@upimesh");
        });
    }
}
