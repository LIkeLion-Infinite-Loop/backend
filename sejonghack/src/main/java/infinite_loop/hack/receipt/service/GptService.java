// src/main/java/infinite_loop/hack/receipt/service/GptService.java
package infinite_loop.hack.receipt.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import infinite_loop.hack.receipt.dto.ParsedItemDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class GptService {

    private final OkHttpClient okHttpClient; // HttpClientConfig의 Bean 주입
    private final ObjectMapper om = new ObjectMapper();

    @Value("${openai.api.key}")
    private String apiKey;

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String MODEL = "gpt-4o-mini";

    public List<ParsedItemDto> parseReceipt(String rawText) {
        try {
            String prompt = buildPrompt(rawText);
            String body = buildJsonBody(prompt);

            Request req = new Request.Builder()
                    .url(API_URL)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .post(RequestBody.create(body, JSON))
                    .build();

            try (Response resp = okHttpClient.newCall(req).execute()) {
                if (!resp.isSuccessful()) {
                    String err = resp.body() != null ? resp.body().string() : "";
                    log.error("[GPT] API 실패 status={} body={}", resp.code(), err);
                    throw new RuntimeException("OpenAI API 실패: " + resp.code());
                }
                String respBody = resp.body() != null ? resp.body().string() : "";
                log.debug("[GPT] raw response (len={}): {}", respBody.length(),
                        respBody.substring(0, Math.min(respBody.length(), 2000)));

                String content = om.readTree(respBody)
                        .path("choices").get(0)
                        .path("message").path("content")
                        .asText();

                content = stripFence(content).trim();
                if (content.isBlank()) throw new RuntimeException("GPT content empty");

                List<ParsedItemDto> items;
                if (content.startsWith("{")) {
                    var root = om.readTree(content);
                    if (root.has("items")) {
                        items = om.convertValue(root.get("items"),
                                new TypeReference<List<ParsedItemDto>>() {});
                    } else {
                        items = List.of(om.readValue(content, ParsedItemDto.class));
                    }
                } else {
                    items = om.readValue(content, new TypeReference<List<ParsedItemDto>>() {});
                }

                List<ParsedItemDto> cleaned = new ArrayList<>();
                for (ParsedItemDto it : items) {
                    if (it == null || it.getName() == null || it.getName().isBlank()) continue;
                    if (it.getQuantity() == null || it.getQuantity() <= 0) it.setQuantity(1);
                    cleaned.add(it);
                }
                return cleaned;
            }
        } catch (Exception e) {
            log.error("[GPT] 파싱 실패", e);
            throw new RuntimeException("GPT JSON 파싱 실패", e);
        }
    }

    private String buildPrompt(String raw) {
        return """
        다음은 한국어 영수증 OCR 결과입니다. 품목들을 구조화하세요.

        # 출력 형식 (반드시 이 JSON 배열만 출력)
        [
          {
            "name": "품목명(문자열)",
            "quantity": 1,
            "category": "PLASTIC|PAPER|GLASS|CAN|VINYL|FOOD|CLOTHES|ETC"
          }
        ]

        - 출력은 JSON 배열만. 부가 텍스트/설명/코드펜스 금지.
        - quantity는 못 알겠으면 1.
        - category는 추정해서 가장 적합한 것 1개만 선택. 모르면 ETC.

        # OCR 원문
        """ + raw;
    }

    private String buildJsonBody(String prompt) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", MODEL);
        body.put("temperature", 0);
        body.put("messages", List.of(
                Map.of("role", "system", "content", "You output only raw JSON. No code fences, no extra words."),
                Map.of("role", "user", "content", prompt)
        ));
        body.put("response_format", Map.of("type", "json_object"));
        return om.writeValueAsString(body);
    }

    private String stripFence(String s) {
        String t = s.trim();
        if (t.startsWith("```")) {
            int idx = t.indexOf('\n');
            if (idx > 0) t = t.substring(idx + 1);
            if (t.endsWith("```")) t = t.substring(0, t.length() - 3);
        }
        return t.trim();
    }
}