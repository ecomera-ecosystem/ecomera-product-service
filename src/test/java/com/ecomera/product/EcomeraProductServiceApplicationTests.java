package com.ecomera.product;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Requires full environment: PostgreSQL, Redis, Config Server. Use repository integration tests instead.")
class EcomeraProductServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
