package spring.board.dto;

public class EditorImageResponse {
    private Long imageId;
    private String url;

    public EditorImageResponse(Long imageId, String url) {
        this.imageId = imageId;
        this.url = url;
    }

    public Long getImageId() {
        return imageId;
    }

    public String getUrl() {
        return url;
    }
}