package com.tgasper.swiftcodes.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record SwiftCodeRequest(
    @NotNull
    @Pattern(regexp = "^[A-Za-z]{6}[A-Za-z0-9]{2}([A-Za-z0-9]{3})?$", 
            message = "Invalid SWIFT code format")
    String swiftCode,
    
    @NotNull(message = "Bank name is required")
    String bankName,
    
    @NotNull
    @Pattern(regexp = "^[A-Za-z]{2}$", 
            message = "Invalid country ISO2 code format")
    String countryISO2,
    
    @NotNull(message = "Country name is required")
    String countryName,
    
    String address,
    
    boolean isHeadquarter
) {}