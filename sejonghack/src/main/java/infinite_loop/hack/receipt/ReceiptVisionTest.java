package infinite_loop.hack.receipt;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class ReceiptVisionTest {

    public static void main(String[] args) {
        // 1) 이미지/키 경로를 절대경로로 지정
        String imagePath = "/Users/jun/Desktop/영수증 샘플 1.jpg";         // <- 실제 영수증 이미지
        String keyPath   = "/Users/jun/Desktop/이상준/키/pro-gecko-469418-k9-d5a73f42ea83.json";     // <- 서비스 계정 키(json)

        // 2) 환경변수도 같이 찍어서 확인
        System.out.println("[ENV] GOOGLE_APPLICATION_CREDENTIALS=" + System.getenv("GOOGLE_APPLICATION_CREDENTIALS"));

        // 3) 파일 존재 확인
        File imgFile = new File(imagePath);
        File keyFile = new File(keyPath);
        System.out.println("[CHECK] image exists=" + imgFile.exists() + " size=" + (imgFile.exists() ? imgFile.length() : 0));
        System.out.println("[CHECK] key exists=" + keyFile.exists() + " size=" + (keyFile.exists() ? keyFile.length() : 0));
        if (!imgFile.exists()) {
            System.err.println("[ERROR] 이미지 경로가 잘못되었습니다.");
            System.exit(11);
        }
        if (!keyFile.exists()) {
            System.err.println("[ERROR] 키 파일 경로가 잘못되었습니다.");
            System.exit(12);
        }

        try (FileInputStream keyFis = new FileInputStream(keyFile)) {
            // 4) 코드에서 자격증명 주입 (환경변수 문제를 회피)
            GoogleCredentials credentials = GoogleCredentials.fromStream(keyFis)
                    .createScoped("https://www.googleapis.com/auth/cloud-platform");

            ImageAnnotatorSettings settings = ImageAnnotatorSettings.newBuilder()
                    .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                    .build();

            try (ImageAnnotatorClient client = ImageAnnotatorClient.create(settings)) {
                ByteString imgBytes = ByteString.readFrom(new FileInputStream(imgFile));
                Image img = Image.newBuilder().setContent(imgBytes).build();
                Feature feat = Feature.newBuilder()
                        .setType(Feature.Type.DOCUMENT_TEXT_DETECTION)
                        .build();

                AnnotateImageRequest req = AnnotateImageRequest.newBuilder()
                        .setImage(img)
                        .addFeatures(feat)
                        .build();

                System.out.println("[INFO] calling Vision API...");
                BatchAnnotateImagesResponse batch = client.batchAnnotateImages(new ArrayList<>(List.of(req)));
                AnnotateImageResponse res = batch.getResponses(0);

                if (res.hasError()) {
                    System.err.println("[VISION ERROR] " + res.getError().getMessage());
                    System.exit(21);
                }

                String text = res.getFullTextAnnotation().getText();
                if (text == null) text = "";
                System.out.println("---- OCR RESULT (first 800 chars) ----");
                System.out.println(text.length() > 800 ? text.substring(0, 800) + "...(truncated)" : text);
                System.out.println("---- LENGTH = " + text.length() + " ----");
                System.out.println("[SUCCESS] Vision OCR call completed.");
            }
        } catch (Exception e) {
            System.err.println("[EXCEPTION] " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            System.exit(99);
        }
    }
}