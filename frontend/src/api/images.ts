import { apiFetch } from "./apiClient";
import { getCsrfToken } from "./csrf";
import { getErrorMessage } from "./errors";

export interface EditorImageResponse {
  imageId: number;
  url: string;
}

export async function uploadEditorImage(
  imageFile: Blob | File,
): Promise<EditorImageResponse> {
  const csrfToken = await getCsrfToken();
  const formData = new FormData();
  const filename = imageFile instanceof File ? imageFile.name : "image.png";
  formData.append("imageFile", imageFile, filename);

  const response = await apiFetch("/api/v1/images", {
    method: "POST",
    credentials: "include",
    headers: {
      [csrfToken.headerName]: csrfToken.token,
    },
    body: formData,
  });

  if (!response.ok) {
    throw new Error(await getErrorMessage(response, "이미지 업로드에 실패했습니다."));
  }

  return response.json() as Promise<EditorImageResponse>;
}
