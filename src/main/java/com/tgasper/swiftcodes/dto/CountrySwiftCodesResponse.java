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

    // getters and setters
    public String getCountryISO2() {
        return countryISO2;
    }

    public void setCountryISO2(String countryISO2) {
        this.countryISO2 = countryISO2;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public List<SwiftCodeResponse> getSwiftCodes() {
        return swiftCodes;
    }

    public void setSwiftCodes(List<SwiftCodeResponse> swiftCodes) {
        this.swiftCodes = swiftCodes;
    }
}