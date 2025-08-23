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

                // json_object를 쓰므로 {"items":[...]} 우선 처리
                if (content.startsWith("{")) {
                    var root = om.readTree(content);
                    if (root.has("items")) {
                        items = om.convertValue(root.get("items"),
                                new TypeReference<List<ParsedItemDto>>() {});
                    } else {
                        // 혹시 단일 객체가 올 경우 방어
                        items = List.of(om.readValue(content, ParsedItemDto.class));
                    }
                } else {
                    // 혹시 배열만 오는 경우도 방어
                    items = om.readValue(content, new TypeReference<List<ParsedItemDto>>() {});
                }

                List<ParsedItemDto> cleaned = new ArrayList<>();
                for (ParsedItemDto it : items) {
                    if (it == null || it.getName() == null || it.getName().isBlank()) continue;
                    if (it.getQuantity() == null || it.getQuantity() <= 0) it.setQuantity(1);
                    // category 대문자 보정(혹시 소문자/혼용 나올 수 있으니)
                    if (it.getCategory() != null) {
                        it.setCategory(it.getCategory().trim().toUpperCase(Locale.ROOT));
                    }
                    cleaned.add(it);
                }
                return cleaned;
            }
        } catch (Exception e) {
            log.error("[GPT] 파싱 실패", e);
            throw new RuntimeException("GPT JSON 파싱 실패", e);
        }
    }

    /**
     * 판단 기준을 매우 자세히 정의한 프롬프트
     * - 출력: 반드시 {"items":[...]} 형태( json_object 와 일치 )
     * - 카테고리: CAN, PLASTIC, GLASS, PAPER, VINYL, CLOTHES, SMALL_ELECTRONICS, ETC
     */
    private String buildPrompt(String raw) {
        return """
        너의 임무: 한국 마트/편의점 영수증의 OCR 원문에서 '품목명'을 보고 해당 품목이 담겼을 가능성이 높은
        '용기/재질 또는 분류'를 **하나만** 추정해 할당한다. 
        이름만 보고 추정해야 하며, 가격/카테고리 라벨이 없어도 규칙 기반으로 최대한 분류한다.

        ## 허용 카테고리 (대문자 고정)
        CAN, PLASTIC, GLASS, PAPER, VINYL, CLOTHES, SMALL_ELECTRONICS, ETC

        ## 강력 규칙(우선순위 높은 매칭)
        1) '캔' 또는 'CAN' 또는 '스프레이', '에어로졸' 포함  → CAN
           - 예: '맥주 500ml 캔', '헤어스프레이', '살충제 스프레이'
        2) '병' 또는 '유리' 명시 → GLASS
           - 예: '와인 750ml 병', '소주 병', '유리병 ○○'
        3) 음료/세제/샴푸/표백제/세정제/핸드워시 등 + (ml|mL|L|리터) 용량 표현이 있고 '캔/병'이 아니면 → PLASTIC (PET/PE가 일반적)
           - 예: '스프라이트 500ml', '유한락스 500ml', '샴푸 1L', '주방세제 750ml'
           - 특기: '막걸리 750ml'는 보통 플라스틱 병이 흔함 → GLASS 키워드 없으면 PLASTIC
        4) '팩', '종이팩', '테트라팩', '카톤', '곽'  → PAPER
           - 예: '우유 1L(종이팩)', '주스 테트라팩'
        5) 비닐/필름류 포장 키워드 → VINYL
           - '위생장갑', '비닐장갑', '지퍼백', '봉투', '랩(랩/랩필름)', '리필파우치', '쓰레기봉투', '일회용 비닐' 등
        6) 의류/섬유류 → CLOTHES
           - '양말', '티셔츠', '팬티', '속옷', '수건', '행주(섬유)', '앞치마', '마스크(면)'
        7) 휴대/가정용 소형 전기·전자 제품 및 부속품 → SMALL_ELECTRONICS
           - '충전기', '이어폰/헤드셋', '전구/LED램프', '면도기(전동)', '가습기', '전기토치', '리모컨', '선풍기', '배터리/건전지'
        8) 위 규칙에 해당하지 않거나 포장 재질이 전혀 유추되지 않으면 → ETC
           - 예: '주방가위', '도마', '볼펜', '노트', '과자(용량/포장 불명)'

        ## 추가 디테일/충돌 해결
        - 우선순위: (1 캔) > (2 유리) > (3 플라스틱 규칙) > (4 종이팩) > (5 비닐) > (6 의류) > (7 전자) > (8 ETC)
        - '테이크아웃컵' 계열:
            * '종이컵' 명시 → PAPER
            * '플라스틱컵/PET컵/뚜껑/빨대' 강조 → PLASTIC
            * 애매하게 '컵'만 있으면 대개 플라스틱이 흔함 → PLASTIC
        - 유제품:
            * '우유 1L'처럼 팩이 일반적이면 PAPER (단, '병' 표기 시 GLASS)
            * '불가리스/요거트 드링크' 등 소형 병음료는 대개 PLASTIC
        - 주류:
            * '맥주 캔' → CAN
            * '소주/와인' + '병' → GLASS
            * '막걸리 750ml' → 보통 PLASTIC (병/캔 명시 시 해당 재질 우선)
        - 키워드가 여러 개 겹치면 위 우선순위에 따라 결정.
        - 카테고리는 반드시 위의 8개 중 하나, **대문자**로만 출력.
        - 수량은 보이면 사용, 없으면 1.

        ## 출력 형식 (반드시 이 JSON 객체만 출력)
        {
          "items": [
            { "name": "품목명", "quantity": 1, "category": "PLASTIC|PAPER|GLASS|CAN|VINYL|CLOTHES|SMALL_ELECTRONICS|ETC" }
          ]
        }

        ## 예시
        입력 품목(예):
        - 유한락스 500ml
        - 스프라이트 500ml
        - 맥주 500ml 캔
        - (生)골목막걸리 750ml
        - 위생장갑 30매
        - 면양말 2족
        - USB C 충전기 20W
        - 테이크아웃컵 세트

        기대 분류(예):
        {
          "items": [
            {"name":"유한락스 500ml","quantity":1,"category":"PLASTIC"},
            {"name":"스프라이트 500ml","quantity":1,"category":"PLASTIC"},
            {"name":"맥주 500ml 캔","quantity":1,"category":"CAN"},
            {"name":"(生)골목막걸리 750ml","quantity":1,"category":"PLASTIC"},
            {"name":"위생장갑 30매","quantity":1,"category":"VINYL"},
            {"name":"면양말 2족","quantity":1,"category":"CLOTHES"},
            {"name":"USB C 충전기 20W","quantity":1,"category":"SMALL_ELECTRONICS"},
            {"name":"테이크아웃컵 세트","quantity":1,"category":"PLASTIC"}
          ]
        }

        ## OCR 원문
        """ + raw;
    }

    private String buildJsonBody(String prompt) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", MODEL);
        body.put("temperature", 0);
        body.put("messages", List.of(
                Map.of("role", "system", "content", "You output ONLY valid JSON that matches the user's required schema. No code fences, no explanations."),
                Map.of("role", "user", "content", prompt)
        ));
        // json_object로 강제 → {"items":[...]} 형태와 잘 맞음
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