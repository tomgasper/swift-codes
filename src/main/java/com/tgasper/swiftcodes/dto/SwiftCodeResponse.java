package com.tgasper.swiftcodes.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.tgasper.swiftcodes.model.SwiftCode;

public class SwiftCodeResponse {
    private String address;
    private String bankName;
    private String countryISO2;
    private String countryName;
    private boolean isHeadquarter;
    private String swiftCode;
    private List<BranchResponse> branches;

    // Constructor for branch response (without branches)
    public SwiftCodeResponse(SwiftCode swiftCode) {
        this.address = swiftCode.getAddress();
        this.bankName = swiftCode.getBank().getBankName();
        this.countryISO2 = swiftCode.getCountry().getIso2Code();
        this.countryName = swiftCode.getCountry().getName();
        this.isHeadquarter = swiftCode.isHeadquarter();
        this.swiftCode = swiftCode.getSwiftCode();
    }

    // Constructor for headquarter response (with branches)
    public SwiftCodeResponse(SwiftCode mainCode, List<SwiftCode> branches) {
        this(mainCode);
        this.branches = branches.stream()
                .map(BranchResponse::new)
                .collect(Collectors.toList());
    }
}