package infinite_loop.sejonghack.config;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Component
public class EnvLoader {

    @PostConstruct
    public void loadEnv() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(".env"));

        for (String line : lines) {
            if (!line.trim().isEmpty() && line.contains("=")) {
                String[] parts = line.split("=", 2);
                System.setProperty(parts[0].trim(), parts[1].trim());
            }
        }
    }
}
