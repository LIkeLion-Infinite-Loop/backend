package infinite_loop.hack.receipt.config;

import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.ImageAnnotatorSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VisionClientConfig {

    @Bean
    public ImageAnnotatorClient imageAnnotatorClient() throws Exception {
        // GOOGLE_APPLICATION_CREDENTIALS 환경변수(ADC) 사용
        ImageAnnotatorSettings settings = ImageAnnotatorSettings.newBuilder().build();
        return ImageAnnotatorClient.create(settings);
    }
}