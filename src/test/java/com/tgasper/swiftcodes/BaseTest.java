package com.tgasper.swiftcodes;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;

@DataJpaTest
@ActiveProfiles("test")
public abstract class BaseTest {
    
    @Autowired
    protected EntityManager entityManager;
    
    @BeforeEach
    void setUp() {
        // clear the database before each test
        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE swift_codes").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE banks").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE countries").executeUpdate();
        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate();
        
        // setup test data specific to each test class
        setupTestData();
    }
    
    // each test class will implement this to set up its specific test data
    protected abstract void setupTestData();
}