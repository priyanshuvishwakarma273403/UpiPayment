package com.walletService.service;

import com.walletService.entity.Wallet;
import com.walletService.entity.WalletTransaction;
import com.walletService.exception.WalletException;
import com.walletService.repository.WalletRepository;
import com.walletService.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ================================================================
 * Wallet Service - Core Business Logic
 * ================================================================
 * Important: Yahan DB transactions bahut important hain.
// * @Transactional ensure karta hai ki debit + credit ya dono ho ya koi nahi.
 *
 * Redis Caching:
 * - Balance frequently read hoti hai, kam likhate hain
 * - @Cacheable: pehli baar DB se, baad mein Redis se
 * - @CacheEvict: balance update hone par cache invalidate karo
 *
 * Double Entry Bookkeeping:
 * Har payment mein do entries:
 * 1. Sender ka DEBIT record
 * 2. Receiver ka CREDIT record
 * Dono ek hi transaction mein hoti hain (atomicity)
 * ================================================================
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;

    /**
     * Naya wallet create karo (user registration ke baad call hoga)
     */
    @Transactional
    public Wallet createWallet(Long userId, String upiId){

        if(walletRepository.existsByUserId(userId)){
            throw new WalletException("Wallet exists for user : "+ userId);
        }

        Wallet wallet = Wallet.builder()
                .userId(userId)
                .upiId(upiId)
                .balance(BigDecimal.ZERO)
                .frozenAmount(BigDecimal.ZERO)
                .isActive(true)
                .build();
        return walletRepository.save(wallet);

    }

    /**
     * Balance check karo - Redis cached
     * Cache name: "wallet-balance", key: userId
     */
    @Cacheable(value = "wallet-balance", key = "#userId")
    @Transactional(readOnly = true)
    public BigDecimal getBalance(Long userId) {
        Wallet wallet = getWalletOrThrow(userId);
        log.debug("Balance fetched from DB for userId: {} (cache miss)", userId);
        return wallet.getAvailableBalance();
    }

    /**
     * Money add karo (bank se top-up)
     */
    @Transactional
    @CacheEvict(value = "wallet-balance", key = "#userId" ) // cache clear karo
    public Wallet addMoney(Long userId, BigDecimal amount , String reference){
        if(amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new WalletException("Amount must be greater than zero");
        }

        Wallet wallet = getWalletOrThrow(userId);
        wallet.setBalance(wallet.getBalance().add(amount));
        wallet = walletRepository.save(wallet);

        // Transaction record save karo
        saveTransaction(wallet, amount, WalletTransaction.TransactionType.CREDIT,
                "Money added: "+ reference);
        log.info("Money added: userId={}, amount={}, newBalance={}", userId, amount, wallet.getBalance());
        return wallet;
    }

    /**
     * Wallet debit karo (payment ke liye)
     * Atomic operation: balance sufficient nahi to exception
     */
    @Transactional
    @CacheEvict(value = "wallet-balance" , key = "#userId")
    public void debitWallet(Long userId, BigDecimal amount, String paymentId){
        Wallet wallet = getWalletOrThrow(userId);

        if (!wallet.hasSufficientBalance(amount)) {
            throw new WalletException(
                    String.format("Insufficient balance. Available: ₹%s, Required: ₹%s",
                            wallet.getAvailableBalance(), amount));
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);

        // Debit transaction record
        saveTransaction(wallet,amount,WalletTransaction.TransactionType.DEBIT,
                "Payment debit : "+paymentId);

        log.info("Wallet debited: userId={}, amount={}, paymentId={}", userId, amount, paymentId);
    }

    /**
     * Wallet credit karo (payment receive karne ke liye)
     */
    @Transactional
    @CacheEvict(value = "wallet-balance", key = "#userId")
    public void creditWallet(Long userId, BigDecimal amount , String paymentId){
        Wallet wallet = getWalletOrThrow(userId);
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);

        // Credit transaction record
        saveTransaction(wallet, amount, WalletTransaction.TransactionType.CREDIT,
                "Payment credit : "+paymentId);

        log.info("Wallet credited : userId ={}, amount={}, paymentId={}", userId, amount, paymentId);
    }

    /**
     * Amount freeze karo - payment in-progress mein
     * Double spending prevent karta hai
     */
    @Transactional
    @CacheEvict(value = "wallet-balance", key = "#userId")
    public void freezeAmount(Long userId, BigDecimal amount){
        Wallet wallet = getWalletOrThrow(userId);
        if(!wallet.hasSufficientBalance(amount)){
            throw new WalletException("Insufficient balance to freeze ");
        }

        BigDecimal currentFrozen = wallet.getFrozenAmount();
        wallet.setFrozenAmount(currentFrozen.add(amount));
        walletRepository.save(wallet);
        log.info("Amount frozen : userId = {}, amount={}", userId, amount);
    }

    /**
     * Frozen amount release karo (payment complete ya failed hone par)
     */
    @Transactional
    @CacheEvict(value = "wallet-balance", key = "#userId")
    public void releaseAmount(Long userId, BigDecimal amount){
        Wallet wallet = getWalletOrThrow(userId);
        BigDecimal newFrozen = wallet.getFrozenAmount().subtract(amount);

        if(newFrozen.compareTo(BigDecimal.ZERO) < 0){
            newFrozen = BigDecimal.ZERO; // Safety check
        }

        wallet.setFrozenAmount(newFrozen);
        walletRepository.save(wallet);
        log.info("Amount released : userId = {}, amount= {}" , userId, amount);
    }


    /**
     * WalletTransaction record save karo (audit trail)
     */
    private void saveTransaction(Wallet wallet, BigDecimal amount,
                                 WalletTransaction.TransactionType type , String description){
        WalletTransaction txn = WalletTransaction.builder()
                .walletId(wallet.getId())
                .userId(wallet.getUserId()).amount(amount)
                .transactionType(type)
                .balanceAfter(wallet.getBalance())
                .description(description)
                .transactionTime(LocalDateTime.now())
                .build();
        transactionRepository.save(txn);
    }

    /**
     * Helper: wallet dhundo ya exception throw karo
     */
    private Wallet getWalletOrThrow(Long userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new WalletException("Wallet not found for userId: " + userId));
    }
}
