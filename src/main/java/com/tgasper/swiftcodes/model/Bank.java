package com.tgasper.swiftcodes.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "banks")
public class Bank {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bank_name", nullable = false)
    @NotNull
    private String bankName;

    @OneToOne(mappedBy = "bank")
    private SwiftCode swiftCode;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public SwiftCode getSwiftCode() {
        return swiftCode;
    }

    public void setSwiftCode(SwiftCode swiftCode) {
        this.swiftCode = swiftCode;
    }
}