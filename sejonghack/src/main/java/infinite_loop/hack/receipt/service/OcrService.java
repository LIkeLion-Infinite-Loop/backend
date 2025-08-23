package infinite_loop.hack.receipt.service;

import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.ImageContext;
import com.google.cloud.vision.v1.TextAnnotation;
import com.google.protobuf.ByteString;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OcrService {

    private final ImageAnnotatorClient imageAnnotatorClient;

    /**
     * 영수증 이미지에서 전체 텍스트를 추출한다.
     * - 우선 DOCUMENT_TEXT_DETECTION 시도
     * - 결과가 비면 TEXT_DETECTION으로 폴백
     * - 한/영 languageHints 적용
     */
    public String extractText(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();

            String text = detectDocumentText(bytes);
            if (text == null || text.isBlank()) {
                text = detectText(bytes); // fallback
            }
            return text == null ? "" : text.trim();

        } catch (Exception e) {
            log.error("OCR 처리 실패", e);
            return "";
        }
    }

    // 신규: raw bytes 버전
    public String extractText(byte[] bytes) {
        try {
            String text = detectDocumentText(bytes);
            if (text == null || text.isBlank()) {
                text = detectText(bytes);
            }
            return text == null ? "" : text.trim();
        } catch (Exception e) {
            log.error("OCR 처리 실패", e);
            return "";
        }
    }


    private String detectDocumentText(byte[] bytes) throws Exception {
        Image image = Image.newBuilder()
                .setContent(ByteString.copyFrom(bytes))
                .build();

        ImageContext context = ImageContext.newBuilder()
                .addLanguageHints("ko")
                .addLanguageHints("en")
                .build();

        Feature feature = Feature.newBuilder()
                .setType(Feature.Type.DOCUMENT_TEXT_DETECTION)
                .build();

        AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                .setImage(image)
                .setImageContext(context)
                .addFeatures(feature)
                .build();

        BatchAnnotateImagesResponse batch = imageAnnotatorClient.batchAnnotateImages(List.of(request));
        AnnotateImageResponse res = batch.getResponses(0);

        if (res.hasError()) {
            log.warn("Vision error (DOCUMENT_TEXT_DETECTION): {}", res.getError().getMessage());
            return "";
        }

        TextAnnotation full = res.getFullTextAnnotation();
        return full == null ? "" : full.getText();
    }

    private String detectText(byte[] bytes) throws Exception {
        Image image = Image.newBuilder()
                .setContent(ByteString.copyFrom(bytes))
                .build();

        Feature feature = Feature.newBuilder()
                .setType(Feature.Type.TEXT_DETECTION)
                .build();

        AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                .setImage(image)
                .addFeatures(feature)
                .build();

        BatchAnnotateImagesResponse batch = imageAnnotatorClient.batchAnnotateImages(List.of(request));
        AnnotateImageResponse res = batch.getResponses(0);

        if (res.hasError()) {
            log.warn("Vision error (TEXT_DETECTION): {}", res.getError().getMessage());
            return "";
        }

        // TEXT_DETECTION은 첫 번째 annotation이 전체 텍스트
        if (res.getTextAnnotationsCount() > 0) {
            return res.getTextAnnotations(0).getDescription();
        }
        return "";
    }
}