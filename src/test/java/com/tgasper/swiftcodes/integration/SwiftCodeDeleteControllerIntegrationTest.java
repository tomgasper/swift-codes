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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SwiftCodeDeleteControllerIntegrationTest extends BaseTest {

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
        headquarters.setAddress("WARSZAWA, PULAWSKA 15");
        headquarters.setHeadquarter(true);
        swiftCodeRepository.save(headquarters);

        // create branch SWIFT code
        SwiftCode branch = new SwiftCode();
        branch.setSwiftCode("BPKOPLPWKRA");
        branch.setBank(pkoBp);
        branch.setCountry(poland);
        branch.setAddress("KRAKOW, RYNEK 1");
        branch.setHeadquarter(false);
        swiftCodeRepository.save(branch);
    }

    @Test
    void shouldDeleteExistingCode() throws Exception {
        mockMvc.perform(delete("/v1/swift-codes/BPKOPLPWKRA")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("SWIFT code deleted successfully")));

        // verify it's no longer retrievable
        mockMvc.perform(get("/v1/swift-codes/BPKOPLPWKRA"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404ForNonExistentCode() throws Exception {
        mockMvc.perform(delete("/v1/swift-codes/BPKOPLPWZZZ")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("not found")));
    }

    @Test
    void shouldRejectInvalidSwiftCodeFormat() throws Exception {
        mockMvc.perform(delete("/v1/swift-codes/INVALID")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Invalid SWIFT code length")));
    }

    @Test
    void shouldHandleHeadquarterDeletionWithBranches() throws Exception {
        mockMvc.perform(delete("/v1/swift-codes/BPKOPLPWXXX")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("SWIFT code deleted successfully")));
    }
}