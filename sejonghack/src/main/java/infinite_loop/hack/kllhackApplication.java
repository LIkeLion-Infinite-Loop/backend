package infinite_loop.hack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class kllhackApplication {

	public static void main(String[] args) {
		SpringApplication.run(kllhackApplication.class, args);
	}

}
