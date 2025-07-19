package infinite_loop.sejonghack;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.FileOutputStream;

public class ProductExcelExporter {

    private static final String SERVICE_KEY = "21a231a755a8423aaa27";
    private static final String API_URL = "https://openapi.foodsafetykorea.go.kr/api/"
            + SERVICE_KEY + "/I2570/json/%d/%d";

    public static void main(String[] args) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("제품 정보");

        // 엑셀 헤더 생성
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("제품명");
        header.createCell(1).setCellValue("바코드 번호");
        header.createCell(2).setCellValue("제조사");
        header.createCell(3).setCellValue("분류");

        int rowIndex = 1;
        int startIdx = 1;
        int batchSize = 100;

        RestTemplate restTemplate = new RestTemplate();

        while (true) {
            String url = String.format(API_URL, startIdx, startIdx + batchSize - 1);

            try {
                ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

                System.out.println("\n[요청 URL] " + url);

                JSONObject json = new JSONObject(response.getBody());

                if (!json.has("I2570")) {
                    System.out.println("⚠️ 'I2570' 항목 없음 - 응답 오류 발생");
                    break;
                }

                JSONObject data = json.getJSONObject("I2570");
                JSONArray rows = data.optJSONArray("row");

                if (rows == null || rows.isEmpty()) {
                    System.out.println("✅ 전체 데이터 수집 완료");
                    break;
                }

                // 데이터 엑셀에 쓰기
                for (int i = 0; i < rows.length(); i++) {
                    JSONObject product = rows.getJSONObject(i);

                    Row row = sheet.createRow(rowIndex++);
                    row.createCell(0).setCellValue(product.optString("PRDT_NM", "없음"));
                    row.createCell(1).setCellValue(product.optString("BRCD_NO", "없음"));
                    row.createCell(2).setCellValue(product.optString("CMPNY_NM", "없음"));
                    row.createCell(3).setCellValue(product.optString("PRDLST_NM", "없음"));
                }

                System.out.println("수집 완료: " + (startIdx + batchSize - 1) + "건");

            } catch (Exception e) {
                System.out.println("❌ 오류 발생: " + e.getMessage());
                e.printStackTrace();
                break;
            }

            startIdx += batchSize;
        }

        // 파일로 저장
        try (FileOutputStream out = new FileOutputStream("products.xlsx")) {
            workbook.write(out);
        }

        workbook.close();
        System.out.println("\n✅ 제품 목록 엑셀 생성 완료: products.xlsx");
    }
}