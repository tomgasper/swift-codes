package com.tgasper.swiftcodes.dto;

import com.tgasper.swiftcodes.model.SwiftCode;

public class BranchResponse {
    private String address;
    private String bankName;
    private String countryISO2;
    private boolean isHeadquarter;
    private String swiftCode;

    public BranchResponse(SwiftCode swiftCode) {
        this.address = swiftCode.getAddress();
        this.bankName = swiftCode.getBank().getBankName();
        this.countryISO2 = swiftCode.getCountry().getIso2Code();
        this.isHeadquarter = swiftCode.isHeadquarter();
        this.swiftCode = swiftCode.getSwiftCode();
    }
}