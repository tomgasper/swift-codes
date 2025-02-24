package com.tgasper.swiftcodes.integration;

import com.tgasper.swiftcodes.BaseTest;
import com.tgasper.swiftcodes.model.Bank;
import com.tgasper.swiftcodes.model.Country;
import com.tgasper.swiftcodes.model.SwiftCode;
import com.tgasper.swiftcodes.repository.BankRepository;
import com.tgasper.swiftcodes.repository.CountryRepository;
import com.tgasper.swiftcodes.repository.SwiftCodeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CountrySwiftCodesControllerIntegrationTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SwiftCodeRepository swiftCodeRepository;

    @Autowired
    private BankRepository bankRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Override
    protected void setupTestData() {
        // Create test country
        Country poland = new Country();
        poland.setIso2Code("PL");
        poland.setName("POLAND");
        countryRepository.save(poland);

        // Create multiple banks
        Bank pkoBp = createBank("BPKOPLPW", "PKO BANK POLSKI");
        Bank pekao = createBank("PKOPPLPW", "BANK PEKAO");
        
        // Create headquarters and branches for PKO BP
        createSwiftCode("BPKOPLPWXXX", pkoBp, poland, "WARSZAWA, PULAWSKA 15", true);
        createSwiftCode("BPKOPLPWKRA", pkoBp, poland, "KRAKOW, RYNEK 1", false);
        
        // Create headquarters and branches for PEKAO
        createSwiftCode("PKOPPLPWXXX", pekao, poland, "WARSZAWA, GRZYBOWSKA 53", true);
        createSwiftCode("PKOPPLPWGDA", pekao, poland, "GDANSK, DLUGA 10", false);
    }

    private Bank createBank(String swiftCode, String name) {
        Bank bank = new Bank();
        bank.setSwiftCode(swiftCode);
        bank.setBankName(name);
        return bankRepository.save(bank);
    }

    private SwiftCode createSwiftCode(String code, Bank bank, Country country, String address, boolean isHq) {
        SwiftCode swiftCode = new SwiftCode();
        swiftCode.setSwiftCode(code);
        swiftCode.setBank(bank);
        swiftCode.setCountry(country);
        swiftCode.setAddress(address);
        swiftCode.setHeadquarter(isHq);
        return swiftCodeRepository.save(swiftCode);
    }

    @Test
    void shouldReturnAllSwiftCodesForCountry() throws Exception {
        mockMvc.perform(get("/v1/swift-codes/country/PL")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.countryISO2", is("PL")))
                .andExpect(jsonPath("$.countryName", is("POLAND")))
                .andExpect(jsonPath("$.swiftCodes", hasSize(4)))
                .andExpect(jsonPath("$.swiftCodes[?(@.swiftCode=='BPKOPLPWXXX')].isHeadquarter", contains(true)))
                .andExpect(jsonPath("$.swiftCodes[?(@.swiftCode=='BPKOPLPWKRA')].isHeadquarter", contains(false)))
                .andExpect(jsonPath("$.swiftCodes[?(@.swiftCode=='PKOPPLPWXXX')].isHeadquarter", contains(true)))
                .andExpect(jsonPath("$.swiftCodes[?(@.swiftCode=='PKOPPLPWGDA')].isHeadquarter", contains(false)));
    }

    @Test
    void shouldHandleCaseInsensitiveCountryCode() throws Exception {
        mockMvc.perform(get("/v1/swift-codes/country/pl")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.countryISO2", is("PL")))
                .andExpect(jsonPath("$.swiftCodes", hasSize(4)));
    }

    @Test
    void shouldReturnEmptyListForNonExistentCountry() throws Exception {
        mockMvc.perform(get("/v1/swift-codes/country/XY")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Country with ISO2 code XY not found")));
    }

    @Test
    void shouldRejectInvalidCountryCode() throws Exception {
        mockMvc.perform(get("/v1/swift-codes/country/POL")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Invalid country code format. Must be exactly 2 letters.")));
    }
} 