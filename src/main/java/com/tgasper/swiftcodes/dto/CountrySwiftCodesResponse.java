package com.tgasper.swiftcodes.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.tgasper.swiftcodes.model.SwiftCode;

public class CountrySwiftCodesResponse {
    private String countryISO2;
    private String countryName;
    private List<SwiftCodeResponse> swiftCodes;

    public CountrySwiftCodesResponse(String countryISO2, String countryName, List<SwiftCode> swiftCodes) {
        this.countryISO2 = countryISO2;
        this.countryName = countryName;
        this.swiftCodes = swiftCodes.stream()
                .map(SwiftCodeResponse::new)
                .collect(Collectors.toList());
    }
}