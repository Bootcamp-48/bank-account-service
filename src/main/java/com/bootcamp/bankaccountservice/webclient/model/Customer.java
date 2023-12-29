package com.bootcamp.bankaccountservice.webclient.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Customer implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String customerId;

    private String name;
    private String type;

}