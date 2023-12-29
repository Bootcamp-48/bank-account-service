package com.bootcamp.bankaccountservice.service;

import com.bootcamp.bankaccountservice.api.AccountsApiDelegate;
import com.bootcamp.bankaccountservice.entity.*;
import com.bootcamp.bankaccountservice.model.AccountType;
import com.bootcamp.bankaccountservice.model.BankAccountDTO;
import com.bootcamp.bankaccountservice.repository.BankAccountRepository;
import com.bootcamp.bankaccountservice.service.exceptions.InvalidAccountDataException;
import com.bootcamp.bankaccountservice.service.exceptions.InvalidAccountTypeException;
import com.bootcamp.bankaccountservice.service.exceptions.MaximumAccountsReachedException;
import com.bootcamp.bankaccountservice.webclient.CustomerWebClient;
import com.bootcamp.bankaccountservice.webclient.model.CustomerType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Service
@Slf4j
public class BankAccountService implements AccountsApiDelegate {

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private CustomerWebClient customerWebClient;

    /**
     * Create a new bank account based on the provided DTO.
     *
     * @param bankAccountDTOMono Mono of BankAccountDTO containing the account data.
     * @param exchange ServerWebExchange context.
     * @return Mono of ResponseEntity with BankAccountDTO if created successfully.
     */

    @Override
    public Mono<ResponseEntity<BankAccountDTO>> createBankAccount(Mono<BankAccountDTO> bankAccountDTOMono, ServerWebExchange exchange) {
        return bankAccountDTOMono
                .map(this::convertToEntity)
                .flatMap(this::determineAndValidateCustomerType)
                .flatMap(this::createAccount)
                .map(this::convertToDto)
                .map(dto -> ResponseEntity.status(HttpStatus.CREATED).body(dto))
                .doOnError(e -> log.error("Error creating bank account: {}", e.getMessage()));
    }


    /**
     * Retrieve a bank account by its ID.
     *
     * @param accountId The ID of the bank account.
     * @param exchange ServerWebExchange context.
     * @return Mono of ResponseEntity with BankAccountDTO if found, or not found response.
     */

    @Override
    public Mono<ResponseEntity<BankAccountDTO>> getBankAccountById(String accountId, ServerWebExchange exchange) {
        return bankAccountRepository.findById(accountId)
                .map(this::convertToDto)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
                .doOnError(e -> log.error("Error retrieving bank account by ID: {}", e.getMessage()));
    }


    /**
     * Retrieve all bank accounts for a specific customer.
     *
     * @param customerId The ID of the customer.
     * @param exchange ServerWebExchange context.
     * @return Mono of ResponseEntity containing a Flux of BankAccountDTOs.
     */

    @Override
    public Mono<ResponseEntity<Flux<BankAccountDTO>>> getAllAccountsForCustomer(String customerId, ServerWebExchange exchange) {
        Flux<BankAccountDTO> dtoFlux = bankAccountRepository.findAllByCustomerId(customerId)
                .map(this::convertToDto);
        return Mono.just(ResponseEntity.ok(dtoFlux))
                .doOnError(e -> log.error("Error retrieving all accounts for customer: {}", e.getMessage()));
    }


    /**
     * Update an existing bank account with new data provided in the DTO.
     *
     * @param accountId The ID of the bank account to update.
     * @param bankAccountDTOMono Mono of BankAccountDTO with updated account data.
     * @param exchange ServerWebExchange context.
     * @return Mono of ResponseEntity with updated BankAccountDTO.
     */
    @Override
    public Mono<ResponseEntity<BankAccountDTO>> updateBankAccount(String accountId, Mono<BankAccountDTO> bankAccountDTOMono, ServerWebExchange exchange) {
        return bankAccountDTOMono
                .flatMap(dto -> bankAccountRepository.findById(accountId)
                        .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found with ID: " + accountId)))
                        .flatMap(existingAccount -> {
                            existingAccount.setBalance(dto.getBalance());
                            return bankAccountRepository.save(existingAccount);
                        })
                )
                .map(this::convertToDto)
                .map(ResponseEntity::ok)
                .onErrorResume(ResponseStatusException.class, e -> Mono.just(ResponseEntity.status(e.getStatus()).build()))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }


    /**
     * Delete a bank account by its ID.
     *
     * @param accountId The ID of the bank account to delete.
     * @param exchange ServerWebExchange context.
     * @return Mono of ResponseEntity indicating success or failure.
     */
    @Override
    public Mono<ResponseEntity<Void>> deleteBankAccountById(String accountId, ServerWebExchange exchange) {
        return bankAccountRepository.findById(accountId)
                .flatMap(account -> bankAccountRepository.delete(account)
                        .then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT))))
                .switchIfEmpty(Mono.just(new ResponseEntity<Void>(HttpStatus.NOT_FOUND)))
                .onErrorResume(e -> {
                    log.error("Error deleting bank account: {}", e.getMessage());
                    return Mono.just(new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR));
                });
    }


    /**
     * Retrieve all bank accounts of a specific type for a given customer.
     *
     * @param customerId The ID of the customer.
     * @param accountType The type of bank accounts to retrieve.
     * @param exchange ServerWebExchange context.
     * @return Mono of ResponseEntity containing a Flux of BankAccountDTOs.
     */

    @Override
    public Mono<ResponseEntity<Flux<BankAccountDTO>>> accountsOfType(String customerId, AccountType accountType, ServerWebExchange exchange) {
        Flux<BankAccountDTO> accountsDtoFlux = bankAccountRepository.findByCustomerIdAndType(customerId, accountType)
                .map(this::convertToDto);

        return accountsDtoFlux
                .collectList()
                .map(list -> list.isEmpty()
                        ? ResponseEntity.notFound().<Flux<BankAccountDTO>>build()
                        : ResponseEntity.ok(Flux.fromIterable(list)))
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(error -> {
                    log.error("Error finding accounts: {}", error.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).<Flux<BankAccountDTO>>build());
                });
    }



    /**
     * Retrieve the first bank account of a specific type for a given customer.
     *
     * @param customerId The ID of the customer.
     * @param accountType The type of bank account to retrieve.
     * @param exchange ServerWebExchange context.
     * @return Mono of ResponseEntity with BankAccountDTO if found, or not found response.
     */

    @Override
    public Mono<ResponseEntity<BankAccountDTO>> getFirstAccountOfType(String customerId,
                                                                      AccountType accountType,
                                                                      ServerWebExchange exchange) {
        return bankAccountRepository.findByCustomerIdAndType(customerId, accountType)
                .next()
                .map(this::convertToDto)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
                .doOnError(error -> log.error("Error retrieving account for customer {} of account type {}: {}", customerId, accountType, error.getMessage()));
    }

    private Mono<BankAccount> determineAndValidateCustomerType(BankAccount account) {
        Function<CustomerType, Mono<BankAccount>> validationFunction = customerType -> {
            if (customerType == CustomerType.PERSONAL) {
                return validatePersonalCustomerForAccountCreation(account);
            } else if (customerType == CustomerType.BUSINESS) {
                return validateBusinessCustomerForAccountCreation(account);
            } else {
                return Mono.error(new InvalidAccountTypeException("Invalid customer type"));
            }
        };

        return customerWebClient.getCustomerById(account.getCustomerId())
                .flatMap(customer ->
                        validationFunction.apply(CustomerType.valueOf(customer.getType())));
    }

    private Mono<BankAccount> validatePersonalCustomerForAccountCreation(BankAccount bankAccount) {
        return bankAccountRepository.findByCustomerIdAndType(bankAccount.getCustomerId(), bankAccount.getType())
                .collectList()
                .flatMap(existingAccounts -> existingAccounts.isEmpty() ?
                        Mono.just(bankAccount) :
                        Mono.error(new MaximumAccountsReachedException("The personal customer already has an account of type " + bankAccount.getType())))
                .flatMap(bankAccountRepository::save);
    }

    private Mono<BankAccount> validateBusinessCustomerForAccountCreation(BankAccount bankAccount) {
        return Mono.just(bankAccount)
                .filter(account -> account.getType() == AccountType.CURRENT)
                .switchIfEmpty(Mono.error(new InvalidAccountTypeException("Invalid account type for business customer")))
                .flatMap(bankAccountRepository::save);
    }


    private Mono<BankAccount> createAccount(BankAccount account) {
        if (account instanceof SavingsAccount) {
            return validateAndCreateSavingsAccount((SavingsAccount) account).cast(BankAccount.class);
        } else if (account instanceof CurrentAccount) {
            return validateAndCreateCurrentAccount((CurrentAccount) account).cast(BankAccount.class);
        } else if (account instanceof FixedTermAccount) {
            return validateAndCreateFixedTermAccount((FixedTermAccount) account).cast(BankAccount.class);
        } else {
            return Mono.error(new InvalidAccountTypeException("Invalid account type"));
        }
    }

    private Mono<SavingsAccount> validateAndCreateSavingsAccount(SavingsAccount account) {
        return Mono.just(account)
                .filter(acc -> acc.getMonthlyMovementLimit() > 0)
                .switchIfEmpty(Mono.error(new InvalidAccountDataException("Invalid monthly movement limit")))
                .flatMap(bankAccountRepository::save);
    }

    private Mono<CurrentAccount> validateAndCreateCurrentAccount(CurrentAccount account) {
        return Mono.just(account)
                .filter(acc -> acc.getMaintenanceFee() >= 0)
                .switchIfEmpty(Mono.error(new InvalidAccountDataException("Maintenance fee cannot be negative")))
                .flatMap(bankAccountRepository::save);
    }

    private Mono<FixedTermAccount> validateAndCreateFixedTermAccount(FixedTermAccount account) {
        return Mono.just(account)
                .filter(acc -> acc.getWithdrawalDate() != null)
                .switchIfEmpty(Mono.error(new InvalidAccountDataException("Specific withdrawal date is required")))
                .flatMap(bankAccountRepository::save);
    }



    private BankAccount convertToEntity(BankAccountDTO dto) {
        return BankAccount.builder()
                .id(dto.getId())
                .customerId(dto.getCustomerId())
                .type(dto.getType())
                .balance(dto.getBalance() != null ? dto.getBalance() : 0.0)
                .build();
    }

    private BankAccountDTO convertToDto(BankAccount account) {
        BankAccountDTO dto = new BankAccountDTO();
        dto.setId(account.getId());
        dto.setCustomerId(account.getCustomerId());
        dto.setType(account.getType());
        dto.setBalance(account.getBalance());
        return dto;
    }
}

