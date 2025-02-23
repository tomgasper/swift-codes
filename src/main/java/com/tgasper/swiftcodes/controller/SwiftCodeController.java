package com.tgasper.swiftcodes.controller;

import com.tgasper.swiftcodes.dto.CountrySwiftCodesResponse;
import com.tgasper.swiftcodes.dto.SwiftCodeResponse;
import com.tgasper.swiftcodes.service.SwiftCodeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/swift-codes")
public class SwiftCodeController {
    private final SwiftCodeService swiftCodeService;

    public SwiftCodeController(SwiftCodeService swiftCodeService) {
        this.swiftCodeService = swiftCodeService;
    }

    @GetMapping("/{swiftCode}")
    public ResponseEntity<SwiftCodeResponse> getSwiftCodeDetails(@PathVariable String swiftCode) {
        SwiftCodeResponse response = swiftCodeService.getSwiftCodeDetails(swiftCode);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/country/{countryISO2code}")
    public ResponseEntity<CountrySwiftCodesResponse> getSwiftCodesByCountry(
            @PathVariable String countryISO2code) {
        CountrySwiftCodesResponse response = swiftCodeService.getSwiftCodesByCountry(countryISO2code);
        return ResponseEntity.ok(response);
    }
}