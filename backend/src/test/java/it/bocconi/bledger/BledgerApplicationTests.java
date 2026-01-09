package it.bocconi.bledger;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles({"unit-test", "unit-test-local"})
class BledgerApplicationTests {

	@Test
	void contextLoads() {
	}

}
