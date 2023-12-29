package com.bootcamp.bankaccountservice.entity;

import com.bootcamp.bankaccountservice.model.AccountType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class FixedTermAccount extends BankAccount {
    private LocalDate withdrawalDate;

    public FixedTermAccount(){
        super();
        this.setType(AccountType.FIXED_TERM);
    }
}