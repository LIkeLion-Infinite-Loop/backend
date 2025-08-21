package infinite_loop.hack.receipt.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("receiptOpenAiClient")
@RequiredArgsConstructor
public class OpenAiReceiptClient {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${openai.api.key}")
    private String apiKey;

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    /** rawText와 완성된 프롬프트를 받아 OpenAI 호출 → content 문자열 그대로 반환 */
    public String callChatCompletions(String prompt) {
        try {
            OkHttpClient client = new OkHttpClient();
            MediaType mediaType = MediaType.parse("application/json; charset=utf-8");

            String bodyJson = """
            {
              "model": "gpt-4o-mini",
              "temperature": 0,
              "messages": [
                {"role": "system", "content": "You output only raw JSON. No code fences, no extra text."},
                {"role": "user", "content": %s}
              ]
            }
            """.formatted(objectMapper.writeValueAsString(prompt));

            Request request = new Request.Builder()
                    .url(API_URL)
                    .post(RequestBody.create(bodyJson, mediaType))
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new RuntimeException("OpenAI API call failed: " + response);
                }
                String responseBody = response.body().string();
                JsonNode root = objectMapper.readTree(responseBody);
                return root.path("choices").get(0).path("message").path("content").asText().trim();
            }
        } catch (Exception e) {
            throw new RuntimeException("OpenAI API call failed", e);
        }
    }
}