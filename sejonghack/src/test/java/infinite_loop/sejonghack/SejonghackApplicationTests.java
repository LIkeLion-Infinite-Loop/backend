package infinite_loop.sejonghack;

import org.junit.jupiter.api.Disabled;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled // 🔥 테스트 제외
@SpringBootTest(properties = "spring.profiles.active=test")
class SejonghackApplicationTests {

	@Test
	void contextLoads() {
	}
}
