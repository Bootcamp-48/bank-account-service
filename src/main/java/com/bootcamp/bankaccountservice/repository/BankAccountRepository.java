package com.bootcamp.bankaccountservice.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import com.bootcamp.bankaccountservice.entity.BankAccount;
import reactor.core.publisher.Flux;
import com.bootcamp.bankaccountservice.model.AccountType;

@Repository
public interface BankAccountRepository extends ReactiveMongoRepository<BankAccount, String> {
    Flux<BankAccount> findAllByCustomerId(String customerId);

    Flux<BankAccount> findByCustomerIdAndType(String customerId, AccountType type);
}
