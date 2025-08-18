package infinite_loop.sejonghack.openai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class OpenAiClient {
    private final WebClient web;
    private final ObjectMapper om = new ObjectMapper();

    public OpenAiClient() {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("OPENAI_API_KEY 환경변수가 없습니다.");
        }
        this.web = WebClient.builder()
                .baseUrl("https://api.openai.com/v1/chat/completions")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public GptQuizResponse createThreeQuestions(String category) {
        var body = Map.of(
                "model", "gpt-4o-mini",
                "response_format", Map.of("type","json_object"),
                "temperature", 0.2,
                "messages", List.of(
                        Map.of("role","system","content",
                                "너는 분리배출 교육용 4지선다 퀴즈 출제기다. 항상 JSON만 출력."),
                        Map.of("role","user","content",
                                "카테고리: " + category + "\n" +
                                        "정확히 3문항 생성.\n" +
                                        "각 문항은 prompt, choices(4개), correct_index(1..4), explanation 포함.\n" +
                                        "JSON 키는 prompt, choices, correct_index, explanation을 사용.")
                )
        );

        var res = web.post().bodyValue(body).retrieve().bodyToMono(Map.class).block();
        var choice0 = ((List<Map<String,Object>>)res.get("choices")).get(0);
        var message = (Map<String,Object>) choice0.get("message");
        var json = (String) message.get("content");

        try { return om.readValue(json, GptQuizResponse.class); }
        catch (Exception e) { throw new RuntimeException("GPT JSON 파싱 실패", e); }
    }

    public static class GptQuizResponse {
        public List<Question> questions;
        public static class Question {
            public String prompt;
            public List<String> choices;     // 길이 4
            public Integer correct_index;    // 1..4
            public String explanation;
        }
    }
}
