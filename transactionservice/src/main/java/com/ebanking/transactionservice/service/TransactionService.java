package com.ebanking.transactionservice.service;

import com.ebanking.transactionservice.model.Transaction;
import com.ebanking.transactionservice.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransactionService {
    
    private final TransactionRepository transactionRepository;
    
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }
    
    public Optional<Transaction> getTransactionById(Long id) {
        return transactionRepository.findById(id);
    }
    
    public Transaction createTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }
    
    public Transaction updateTransaction(Long id, Transaction transaction) {
        Transaction existingTransaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        
        existingTransaction.setTransactionId(transaction.getTransactionId());
        existingTransaction.setTransactionType(transaction.getTransactionType());
        existingTransaction.setAmount(transaction.getAmount());
        existingTransaction.setFromAccountId(transaction.getFromAccountId());
        existingTransaction.setToAccountId(transaction.getToAccountId());
        existingTransaction.setUserId(transaction.getUserId());
        
        return transactionRepository.save(existingTransaction);
    }
    
    public void deleteTransaction(Long id) {
        transactionRepository.deleteById(id);
    }
}
