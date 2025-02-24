package com.tgasper.swiftcodes.service;

import com.tgasper.swiftcodes.exception.SwiftCodeValidationException;
import com.tgasper.swiftcodes.model.Bank;
import com.tgasper.swiftcodes.repository.BankRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BankService {
    private final BankRepository bankRepository;

    public BankService(BankRepository bankRepository) {
        this.bankRepository = bankRepository;
    }

    public Bank getOrCreateBank(String baseSwiftCode, String bankName) {
        Optional<Bank> existingBank = bankRepository.findBySwiftCode(baseSwiftCode);
        
        if (existingBank.isPresent()) {
            return validateAndReturnBank(existingBank.get(), bankName);
        } else {
            return createNewBank(baseSwiftCode, bankName);
        }
    }

    public void deleteBank(String baseSwiftCode) {
        bankRepository.deleteBySwiftCode(baseSwiftCode);
    }

    private Bank validateAndReturnBank(Bank existingBank, String providedBankName) {
        if (!existingBank.getBankName().equals(providedBankName)) {
            throw new SwiftCodeValidationException(
                String.format("Bank name mismatch. Existing bank name: %s, Provided bank name: %s",
                    existingBank.getBankName(), providedBankName));
        }
        return existingBank;
    }

    private Bank createNewBank(String swiftCode, String bankName) {
        Bank newBank = new Bank();
        newBank.setBankName(bankName);
        newBank.setSwiftCode(swiftCode);
        return bankRepository.save(newBank);
    }
} 