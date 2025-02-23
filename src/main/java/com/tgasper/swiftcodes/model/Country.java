package com.tgasper.swiftcodes.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "countries")
public class Country {
    @Id
    @Column(name = "iso2_code")
    @NotNull
    @Size(min = 2, max = 2)
    private String iso2Code;

    @Column(name = "name", nullable = false)
    @NotNull
    private String name;

    // Getters and Setters
    public String getIso2Code() {
        return iso2Code;
    }

    public void setIso2Code(String iso2Code) {
        this.iso2Code = iso2Code != null ? iso2Code.toUpperCase() : null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name != null ? name.toUpperCase() : null;
    }
}