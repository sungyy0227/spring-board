package spring.board.image.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import spring.board.image.dto.EditorImageResponse;
import spring.board.image.service.ImageService;
import spring.board.security.CustomUserDetails;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/images")
public class ImageApiController {
    private final ImageService imageService;

    public ImageApiController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public EditorImageResponse uploadEditorImage(
            @RequestParam("imageFile") MultipartFile imageFile,
            @AuthenticationPrincipal CustomUserDetails loginMember,
            HttpServletRequest request
    ) throws IOException {
        Long loginMemberId = loginMember == null ? null : loginMember.getId();
        String draftToken = null;

        if (loginMemberId == null) {
            HttpSession session = request.getSession();
            draftToken = getOrCreateDraftToken(session);
        }

        return imageService.uploadImage(imageFile, loginMemberId, draftToken);
    }

    private String getOrCreateDraftToken(HttpSession session) {
        String draftToken = (String) session.getAttribute("postDraftToken");

        if (draftToken == null) {
            draftToken = UUID.randomUUID().toString();
            session.setAttribute("postDraftToken", draftToken);
        }

        return draftToken;
    }
}
