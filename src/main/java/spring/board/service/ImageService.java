package spring.board.service;

import jakarta.annotation.PostConstruct;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ImageService {
    private final Path uploadPath;
    private final Tika tika = new Tika();

    public ImageService(@Value("${file.upload-dir}") String uploadDir){
        this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(uploadPath);
    }

    public String store(MultipartFile file) throws IOException{
        if (file == null || file.isEmpty()) {
            return null;
        }

        String originalFilename = file.getOriginalFilename();
        String ext = getExtension(originalFilename);

        if (!isImageExtension(ext)) {
            throw new IllegalArgumentException("이미지 파일만 업로드할 수 있습니다.");
        }

        if (!isImageContentByTika(file)) {
            throw new IllegalArgumentException("이미지 파일만 업로드할 수 있습니다.");
        }

        String storedFilename = UUID.randomUUID() + "." + ext;
        Path targetPath = uploadPath.resolve(storedFilename);

        file.transferTo(targetPath.toFile()); //

        return "/images/post/" + storedFilename;
    }

    public void deleteByImageUrl(String imageUrl){
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        String prefix = "/images/post/";

        if (!imageUrl.startsWith(prefix)) {
            throw new IllegalArgumentException("잘못된 이미지 경로입니다.");
        }

        String storedFilename = imageUrl.substring(prefix.length());

        Path filePath = uploadPath.resolve(storedFilename).normalize();

        if (!filePath.startsWith(uploadPath)) {
            throw new IllegalArgumentException("잘못된 파일 경로입니다.");
        }

        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("이미지 파일 삭제에 실패했습니다.", e);
        }
    }


    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new IllegalArgumentException("파일 확장자가 없습니다.");
        }

        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase(); //lastIndexOf를 쓰는건 뒤에서부터 검사해야 제대로 된 확장자이기떄문?
    }

    private boolean isImageExtension(String ext) {
        return ext.equals("jpg") || ext.equals("jpeg") || ext.equals("png")
                || ext.equals("gif")
                || ext.equals("webp");
    }

    private boolean isImageContentByTika(MultipartFile file) throws IOException {
        String mimeType = tika.detect(file.getInputStream());

        return mimeType.equals("image/jpeg") || mimeType.equals("image/png")
                || mimeType.equals("image/gif")
                || mimeType.equals("image/webp");
    }

}
