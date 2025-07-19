package infinite_loop.sejonghack.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class BarcodeExternalApiService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String SERVICE_KEY = "21a231a755a8423aaa27";
    private final String API_URL =
            "https://openapi.foodsafetykorea.go.kr/api/" + SERVICE_KEY + "/C005/json/1/1/BRCD={barcode}";

    public String getProductNameByBarcode(String barcode) {
        String url = API_URL.replace("{barcode}", barcode);

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode() != HttpStatus.OK) {
                throw new IllegalStateException("식품안전나라 API 호출 실패");
            }

            JSONObject json = new JSONObject(response.getBody());
            JSONArray rowArray = json.getJSONObject("C005").getJSONArray("row");

            if (rowArray.length() == 0) {
                throw new IllegalArgumentException("해당 바코드의 제품 정보가 없습니다.");
            }

            JSONObject productInfo = rowArray.getJSONObject(0);
            return productInfo.getString("PRDLST_NM");

        } catch (JSONException e) {
            throw new IllegalStateException("외부 API 응답 파싱 실패", e);
        }
    }
}