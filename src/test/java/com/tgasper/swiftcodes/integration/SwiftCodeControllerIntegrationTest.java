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
class SwiftCodeControllerIntegrationTest extends BaseTest {

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
        // create test country
        Country poland = new Country();
        poland.setIso2Code("PL");
        poland.setName("POLAND");
        countryRepository.save(poland);

        // create test bank
        Bank pkoBp = new Bank();
        pkoBp.setSwiftCode("BPKOPLPW");
        pkoBp.setBankName("PKO BANK POLSKI");
        bankRepository.save(pkoBp);

        // create headquarters SWIFT code
        SwiftCode headquarters = new SwiftCode();
        headquarters.setSwiftCode("BPKOPLPWXXX");
        headquarters.setBank(pkoBp);
        headquarters.setCountry(poland);
        headquarters.setAddress("UL. PULAWSKA 15, WARSZAWA");
        headquarters.setHeadquarter(true);
        swiftCodeRepository.save(headquarters);

        // create branch SWIFT code
        SwiftCode branch = new SwiftCode();
        branch.setSwiftCode("BPKOPLPWKRA");
        branch.setBank(pkoBp);
        branch.setCountry(poland);
        branch.setAddress("RYNEK GLOWNY 31, KRAKOW");
        branch.setHeadquarter(false);
        swiftCodeRepository.save(branch);
    }

    @Test
    void shouldReturnHeadquartersWithBranches() throws Exception {
        mockMvc.perform(get("/v1/swift-codes/BPKOPLPWXXX")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.swiftCode", is("BPKOPLPWXXX")))
                .andExpect(jsonPath("$.bankName", is("PKO BANK POLSKI")))
                .andExpect(jsonPath("$.countryISO2", is("PL")))
                .andExpect(jsonPath("$.address", is("UL. PULAWSKA 15, WARSZAWA")))
                .andExpect(jsonPath("$.headquarter", is(true)))
                .andExpect(jsonPath("$.branches", hasSize(1)))
                .andExpect(jsonPath("$.branches[0].swiftCode", is("BPKOPLPWKRA")))
                .andExpect(jsonPath("$.branches[0].address", is("RYNEK GLOWNY 31, KRAKOW")))
                .andExpect(jsonPath("$.branches[0].headquarter", is(false)));
    }

    @Test
    void shouldReturnBranchDetails() throws Exception {
        mockMvc.perform(get("/v1/swift-codes/BPKOPLPWKRA")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.swiftCode", is("BPKOPLPWKRA")))
                .andExpect(jsonPath("$.bankName", is("PKO BANK POLSKI")))
                .andExpect(jsonPath("$.headquarter", is(false)))
                .andExpect(jsonPath("$.branches").doesNotExist());
    }

    @Test
    void shouldReturn404ForNonExistentSwiftCode() throws Exception {
        mockMvc.perform(get("/v1/swift-codes/BPKOPLPWZZZ")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", containsString("not found")));
    }

    @Test
    void shouldHandleEightCharacterSwiftCode() throws Exception {
        mockMvc.perform(get("/v1/swift-codes/BPKOPLPW")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.swiftCode", is("BPKOPLPWXXX")))
                .andExpect(jsonPath("$.isHeadquarter", is(true)));
    }
}