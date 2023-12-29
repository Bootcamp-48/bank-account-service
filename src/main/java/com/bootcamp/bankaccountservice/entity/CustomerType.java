package com.bootcamp.bankaccountservice.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Gets or Sets CustomerType
 */
public enum CustomerType {

    PERSONAL("PERSONAL"),

    BUSINESS("BUSINESS"),

    VIP("VIP"),

    PYME("PYME");

    private String value;

    CustomerType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static CustomerType fromValue(String value) {
        for (CustomerType b : CustomerType.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}
