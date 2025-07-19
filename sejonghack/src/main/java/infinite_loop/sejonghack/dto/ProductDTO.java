package infinite_loop.sejonghack.dto;

public class ProductDTO {
    private String name;
    private String category;
    private int guideId;

    public ProductDTO(String name, String category, int guideId) {
        this.name = name;
        this.category = category;
        this.guideId = guideId;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public int getGuideId() {
        return guideId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setGuideId(int guideId) {
        this.guideId = guideId;
    }
}
