// src/main/java/infinite_loop/hack/receipt/config/HttpClientConfig.java
package infinite_loop.hack.receipt.config;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.Duration;

@Configuration
public class HttpClientConfig {

    @Bean
    public OkHttpClient okHttpClient() {
        // 최대 2회 재시도
        Interceptor retry = new Interceptor() {
            private static final int MAX_RETRY = 2;
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request req = chain.request();
                IOException last = null;
                for (int i = 0; i <= MAX_RETRY; i++) {
                    try {
                        return chain.proceed(req);
                    } catch (IOException e) {
                        last = e;
                        if (i == MAX_RETRY) throw e;
                    }
                }
                throw last;
            }
        };

        return new OkHttpClient.Builder()
                .addInterceptor(retry)
                .connectTimeout(Duration.ofSeconds(20))
                .readTimeout(Duration.ofSeconds(90))
                .writeTimeout(Duration.ofSeconds(90))
                .callTimeout(Duration.ofSeconds(120))
                .build();
    }
}