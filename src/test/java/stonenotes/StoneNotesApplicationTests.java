package stonenotes;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import stonenotes.config.TestSecurityConfig;

@SpringBootTest
@Import(TestSecurityConfig.class)
class StoneNotesApplicationTests {

	@Test
	void contextLoads() {
	}

}
