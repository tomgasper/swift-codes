package com.tgasper.swiftcodes.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.stream.Collectors;

import com.tgasper.swiftcodes.model.SwiftCode;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SwiftCodeResponse {
    private String address;
    private String bankName;
    private String countryISO2;
    private String countryName;
    private boolean isHeadquarter;
    private String swiftCode;
    private List<BranchResponse> branches;

    // constructor for branch response (without branches)
    public SwiftCodeResponse(SwiftCode swiftCode) {
        this.address = swiftCode.getAddress();
        this.bankName = swiftCode.getBank().getBankName();
        this.countryISO2 = swiftCode.getCountry().getIso2Code();
        this.countryName = swiftCode.getCountry().getName();
        this.isHeadquarter = swiftCode.isHeadquarter();
        this.swiftCode = swiftCode.getSwiftCode();
    }

    // constructor for headquarter response (with branches)
    public SwiftCodeResponse(SwiftCode mainCode, List<SwiftCode> branches) {
        this(mainCode);
        this.branches = branches.stream()
                .map(BranchResponse::new)
                .collect(Collectors.toList());
    }

    // getters and setters
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

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

    public boolean getIsHeadquarter() {
        return isHeadquarter;
    }

    public void setIsHeadquarter(boolean isHeadquarter) {
        this.isHeadquarter = isHeadquarter;
    }

    public String getSwiftCode() {
        return swiftCode;
    }

    public void setSwiftCode(String swiftCode) {
        this.swiftCode = swiftCode;
    }

    public List<BranchResponse> getBranches() {
        return branches;
    }

    public void setBranches(List<BranchResponse> branches) {
        this.branches = branches;
    }
}