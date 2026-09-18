import { apiFetch } from "./apiClient";
import { getCsrfToken } from "./csrf";
import { getErrorMessage } from "./errors";

export interface CommentCreateRequest {
  commenter: string;
  content: string;
  guestPassword: string;
}

export async function createComment(postId: number, request: CommentCreateRequest): Promise<void> {
  const csrfToken = await getCsrfToken();
  const response = await apiFetch(`/api/v1/posts/${postId}/comments`, {
    method: "POST",
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
      [csrfToken.headerName]: csrfToken.token,
    },
    body: JSON.stringify(request),
  });
  if (!response.ok) {
    throw new Error(await getErrorMessage(response, "댓글을 등록하지 못했습니다."));
  }
}

export async function deleteComment(postId: number, commentId: number, guestPassword?: string): Promise<void> {
  const csrfToken = await getCsrfToken();
  const response = await apiFetch(`/api/v1/posts/${postId}/comments/${commentId}`, {
    method: "DELETE",
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
      [csrfToken.headerName]: csrfToken.token,
    },
    body: JSON.stringify({ guestPassword }),
  });
  if (!response.ok) {
    throw new Error(await getErrorMessage(response, "댓글을 삭제하지 못했습니다."));
  }
}
