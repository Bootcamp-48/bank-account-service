package com.bootcamp.bankaccountservice.entity;

import com.bootcamp.bankaccountservice.model.AccountType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CurrentAccount extends BankAccount {
    private double maintenanceFee;

    public CurrentAccount() {
        super();
        this.setType(AccountType.CURRENT);
    }
}