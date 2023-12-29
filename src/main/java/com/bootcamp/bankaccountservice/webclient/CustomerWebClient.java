package com.bootcamp.bankaccountservice.webclient;

import com.bootcamp.bankaccountservice.entity.Customer;
import reactor.core.publisher.Mono;

public interface CustomerWebClient {

    Mono<Customer> getCustomerById(String customerId);
}