import { apiFetch } from "./apiClient";
import { getCsrfToken } from "./csrf";
import { getErrorMessage } from "./errors";

export interface PostCreateRequest {
  title: string;
  content: string;
  poster: string;
  guestPassword: string;
  imageIds: number[];
}

export interface PostCreateResponse {
  postId: number;
}

export interface CommentResponse {
  id: number;
  commenter: string;
  commentContent: string;
  memberId: number | null;
  memberWithdrawn: boolean;
}

export interface PostSummary {
  id: number;
  title: string;
  poster: string;
  viewCount: number;
  createdAt: string;
}

export interface PostPageResponse {
  content: PostSummary[];
  currentPage: number;
  totalPages: number;
  totalElements: number;
}

export interface PostDetailResponse extends PostSummary {
  content: string;
  memberId: number | null;
  memberWithdrawn: boolean;
  comments: CommentResponse[];
}

export interface PostEditResponse {
  id: number;
  title: string;
  content: string;
  poster: string;
  guest: boolean;
}

export interface PostUpdateRequest {
  title: string;
  content: string;
  poster: string;
  imageIds: number[];
}

export interface PostSearchParams {
  page?: number;
  type?: string;
  keyword?: string;
}

export async function createPost(
  request: PostCreateRequest,
): Promise<PostCreateResponse> {
  const csrfToken = await getCsrfToken();
  const response = await apiFetch("/api/v1/posts", {
    method: "POST",
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
      [csrfToken.headerName]: csrfToken.token,
    },
    body: JSON.stringify(request),
  });

  if (!response.ok) {
    throw new Error(await getErrorMessage(response, "게시글 작성에 실패했습니다."));
  }

  return response.json() as Promise<PostCreateResponse>;
}

export async function getPosts(params: PostSearchParams = {}): Promise<PostPageResponse> {
  const searchParams = new URLSearchParams();
  searchParams.set("page", String(params.page ?? 1));
  if (params.keyword !== undefined) {
    searchParams.set("keyword", params.keyword);
    searchParams.set("type", params.type ?? "title_content");
  }

  const response = await apiFetch(`/api/v1/posts?${searchParams}`, {
    credentials: "include",
  });
  if (!response.ok) {
    throw new Error(await getErrorMessage(response, "게시글 목록을 불러오지 못했습니다."));
  }
  return response.json() as Promise<PostPageResponse>;
}

export async function getPost(postId: number): Promise<PostDetailResponse> {
  const response = await apiFetch(`/api/v1/posts/${postId}`, {
    credentials: "include",
  });
  if (!response.ok) {
    throw new Error(await getErrorMessage(response, "게시글을 불러오지 못했습니다."));
  }
  return response.json() as Promise<PostDetailResponse>;
}

async function csrfJsonRequest(path: string, method: string, body?: unknown): Promise<Response> {
  const csrfToken = await getCsrfToken();
  const response = await apiFetch(path, {
    method,
    credentials: "include",
    headers: {
      "Accept": "application/json",
      "Content-Type": "application/json",
      [csrfToken.headerName]: csrfToken.token,
    },
    body: body === undefined ? undefined : JSON.stringify(body),
  });
  return response;
}

export async function deletePost(postId: number, password?: string): Promise<void> {
  const response = await csrfJsonRequest(`/api/v1/posts/${postId}`, "DELETE", { password });
  if (!response.ok) {
    throw new Error(await getErrorMessage(response, "게시글을 삭제하지 못했습니다."));
  }
}

export async function verifyPostEditAccess(postId: number, password?: string): Promise<PostEditResponse> {
  const response = await csrfJsonRequest(`/api/v1/posts/${postId}/edit-access`, "POST", { password });
  if (!response.ok) {
    throw new Error(await getErrorMessage(response, "게시글 수정 권한을 확인하지 못했습니다."));
  }
  return response.json() as Promise<PostEditResponse>;
}

export async function getPostForEdit(postId: number): Promise<PostEditResponse> {
  const response = await apiFetch(`/api/v1/posts/${postId}/edit`, { credentials: "include" });
  if (!response.ok) {
    throw new Error(await getErrorMessage(response, "수정할 게시글을 불러오지 못했습니다."));
  }
  return response.json() as Promise<PostEditResponse>;
}

export async function updatePost(postId: number, request: PostUpdateRequest): Promise<void> {
  const response = await csrfJsonRequest(`/api/v1/posts/${postId}`, "PATCH", request);
  if (!response.ok) {
    throw new Error(await getErrorMessage(response, "게시글을 수정하지 못했습니다."));
  }
}
