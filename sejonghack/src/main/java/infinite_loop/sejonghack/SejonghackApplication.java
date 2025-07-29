package infinite_loop.sejonghack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource(value = "classpath:.env", factory = DotenvPropertySourceFactory.class)
public class SejonghackApplication {

	public static void main(String[] args) {
		SpringApplication.run(SejonghackApplication.class, args);
	}

}
