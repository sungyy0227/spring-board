package spring.board.post.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;

public class PostDto {
    private String poster;

    private String title;

    private String content;

    private String guestPassword;
    private List<Long> imageIds = new ArrayList<>();

    public static PostDto from(PostCreateRequest request) {
        PostDto postDto = new PostDto();
        postDto.setTitle(request.title());
        postDto.setContent(request.content());
        postDto.setPoster(request.poster());
        postDto.setGuestPassword(request.guestPassword());

        if (request.imageIds() != null) {
            postDto.setImageIds(new ArrayList<>(request.imageIds()));
        }

        return postDto;
    }

    public static PostDto from(PostUpdateRequest request) {
        PostDto postDto = new PostDto();
        postDto.setTitle(request.title());
        postDto.setContent(request.content());
        postDto.setPoster(request.poster());
        postDto.setImageIds(request.imageIds());
        return postDto;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getGuestPassword() {
        return guestPassword;
    }

    public void setGuestPassword(String guestPassword) {
        this.guestPassword = guestPassword;
    }

    public List<Long> getImageIds() {
        return imageIds;
    }

    public void setImageIds(List<Long> imageIds) {
        this.imageIds = imageIds;
    }
}
