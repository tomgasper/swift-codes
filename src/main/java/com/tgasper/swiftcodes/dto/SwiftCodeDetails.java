package com.tgasper.swiftcodes.dto;

import com.tgasper.swiftcodes.model.SwiftCode;
import java.util.List;

public class SwiftCodeDetails {
    private final SwiftCode mainCode;
    private final List<SwiftCode> branches;

    public SwiftCodeDetails(SwiftCode mainCode, List<SwiftCode> branches) {
        this.mainCode = mainCode;
        this.branches = branches;
    }

    public SwiftCode getMainCode() {
        return mainCode;
    }

    public List<SwiftCode> getBranches() {
        return branches;
    }
}