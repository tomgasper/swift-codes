package com.tgasper.swiftcodes.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tgasper.swiftcodes.BaseTest;
import com.tgasper.swiftcodes.dto.request.SwiftCodeRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SwiftCodePostControllerIntegrationTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SwiftCodeRepository swiftCodeRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private BankRepository bankRepository;

    @Override
    protected void setupTestData() {
        // Create test country
        Country poland = new Country();
        poland.setIso2Code("PL");
        poland.setName("POLAND");
        countryRepository.save(poland);

        // Create test bank with HQ
        Bank pkoBp = new Bank();
        pkoBp.setSwiftCode("BPKOPLPW");
        pkoBp.setBankName("PKO BANK POLSKI");
        bankRepository.save(pkoBp);

        SwiftCode headquarters = new SwiftCode();
        headquarters.setSwiftCode("BPKOPLPWXXX");
        headquarters.setBank(pkoBp);
        headquarters.setCountry(poland);
        headquarters.setAddress("WARSZAWA, PULAWSKA 15");
        headquarters.setHeadquarter(true);
        swiftCodeRepository.save(headquarters);
    }

    @Test
    void shouldCreateValidHeadquarters() throws Exception {
        SwiftCodeRequest request = new SwiftCodeRequest(
            "PKOPPLPWXXX",
            "BANK PEKAO",
            "PL",
            "POLAND",
            "WARSZAWA, GRZYBOWSKA 53",
            true
        );

        mockMvc.perform(post("/v1/swift-codes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message", is("SWIFT code added successfully")));

        // Verify it's retrievable
        mockMvc.perform(get("/v1/swift-codes/PKOPPLPWXXX"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isHeadquarter", is(true)));
    }

    @Test
    void shouldCreateValidBranch() throws Exception {
        SwiftCodeRequest request = new SwiftCodeRequest(
            "BPKOPLPWKRA",
            "PKO BANK POLSKI",
            "PL",
            "POLAND",
            "KRAKOW, RYNEK 1",
            false
        );

        mockMvc.perform(post("/v1/swift-codes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Verify it's associated with HQ
        mockMvc.perform(get("/v1/swift-codes/BPKOPLPWXXX"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.branches[0].swiftCode", is("BPKOPLPWKRA")));
    }

    @Test
    void shouldRejectDuplicateSwiftCode() throws Exception {
        SwiftCodeRequest request = new SwiftCodeRequest(
            "BPKOPLPWXXX",
            "PKO BANK POLSKI",
            "PL",
            "POLAND",
            "WARSZAWA, PULAWSKA 15",
            true
        );

        mockMvc.perform(post("/v1/swift-codes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("already exists")));
    }

    @Test
    void shouldRejectInvalidSwiftCodeFormat() throws Exception {
        SwiftCodeRequest request = new SwiftCodeRequest(
            "INVALID",
            "PKO BANK POLSKI",
            "PL",
            "POLAND",
            "WARSZAWA, PULAWSKA 15",
            true
        );

        mockMvc.perform(post("/v1/swift-codes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Invalid SWIFT code format")));
    }

    @Test
    void shouldRejectMissingRequiredFields() throws Exception {
        String invalidJson = """
            {
                "swiftCode": "BPKOPLPWXXX",
                "address": "WARSZAWA, PULAWSKA 15",
                "isHeadquarter": true
            }
            """;

        mockMvc.perform(post("/v1/swift-codes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("required")));
    }

    @Test
    void shouldHandleInconsistentHqFlag() throws Exception {
        SwiftCodeRequest request = new SwiftCodeRequest(
            "BPKOPLPWXXX",
            "PKO BANK POLSKI",
            "PL",
            "POLAND",
            "WARSZAWA, PULAWSKA 15",
            false  // Inconsistent with XXX ending
        );

        mockMvc.perform(post("/v1/swift-codes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Inconsistent headquarters flag")));
    }

    @Test
    void shouldNormalizeCountryCode() throws Exception {
        SwiftCodeRequest request = new SwiftCodeRequest(
            "PKOPPLPWGDA",
            "BANK PEKAO",
            "pl",  // lowercase
            "POLAND",
            "GDANSK, DLUGA 10",
            false
        );

        mockMvc.perform(post("/v1/swift-codes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Verify country code is uppercase in response
        mockMvc.perform(get("/v1/swift-codes/PKOPPLPWGDA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.countryISO2", is("PL")));
    }
}