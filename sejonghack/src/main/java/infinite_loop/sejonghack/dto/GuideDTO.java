package infinite_loop.sejonghack.dto;

public class GuideDTO {
    private String guideText;
    private String imageUrl;

    public GuideDTO(String guideText, String imageUrl) {
        this.guideText = guideText;
        this.imageUrl = imageUrl;
    }

    public String getGuideText() {
        return guideText;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setGuideText(String guideText) {
        this.guideText = guideText;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
