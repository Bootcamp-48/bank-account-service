package com.bootcamp.bankaccountservice.entity;

import com.bootcamp.bankaccountservice.model.AccountType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SavingsAccount extends BankAccount {
    private int monthlyMovementLimit;

    public SavingsAccount(){
        super();
        this.setType(AccountType.SAVINGS);
    }
}