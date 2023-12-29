package com.bootcamp.bankaccountservice.entity;

import com.bootcamp.bankaccountservice.model.AccountType;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

@Document(collection = "bank_accounts")
public class BankAccount {
    @Id
    private String id;

    @NotBlank(message = "Customer ID must not be blank")
    private String customerId;

    @NotNull(message = "Account type is required")
    private AccountType type;

    @Min(value = 0, message = "Balance cannot be negative")
    private double balance;

}