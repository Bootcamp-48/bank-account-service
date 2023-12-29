package com.bootcamp.bankaccountservice.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {
    @Id
    private String transactionId;
    private String accountId;
    private double amount;
    private String type;
    private LocalDateTime timestamp;
}
