package spring.board.image.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import spring.board.image.dto.EditorImageResponse;
import spring.board.image.service.ImageService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ImageApiControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ImageService imageService;

    @Test
    @DisplayName("게스트가 에디터 이미지를 업로드하면 임시 작성 토큰을 생성한다")
    void uploadGuestEditorImage() throws Exception {
        MockHttpSession session = new MockHttpSession();
        MockMultipartFile imageFile = new MockMultipartFile(
                "imageFile",
                "sample.png",
                "image/png",
                new byte[]{1, 2, 3}
        );

        given(imageService.uploadImage(any(), isNull(), anyString()))
                .willReturn(new EditorImageResponse(1L, "/images/post/sample.png"));

        mockMvc.perform(multipart("/api/v1/images")
                        .file(imageFile)
                        .session(session)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.imageId").value(1L))
                .andExpect(jsonPath("$.url").value("/images/post/sample.png"));

        String draftToken = (String) session.getAttribute("postDraftToken");
        assertThat(draftToken).isNotBlank();
        verify(imageService).uploadImage(any(), isNull(), eq(draftToken));
    }

    @Test
    @DisplayName("유효하지 않은 이미지는 오류 JSON을 반환한다")
    void uploadInvalidEditorImage() throws Exception {
        MockMultipartFile imageFile = new MockMultipartFile(
                "imageFile",
                "sample.txt",
                "text/plain",
                new byte[]{1, 2, 3}
        );

        given(imageService.uploadImage(any(), isNull(), anyString()))
                .willThrow(new IllegalArgumentException("이미지 파일만 업로드할 수 있습니다."));

        mockMvc.perform(multipart("/api/v1/images")
                        .file(imageFile)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("이미지 파일만 업로드할 수 있습니다."));
    }
}
