import { apiFetch } from "./apiClient";
import { getCsrfToken } from "./csrf";
import { getErrorMessage } from "./errors";
import type { PostSummary } from "./posts";

export interface SignupValidationError {
  fieldName: "loginId" | "password" | "nickname";
  errorCode: string;
  errorMessage: string;
}

export interface SignupRequest {
  loginId: string;
  password: string;
  nickname: string;
}

export interface MemberProfile {
  id: number;
  loginId: string;
  nickname: string;
  role: "USER" | "ADMIN";
  status: "ACTIVE" | "WITHDRAWN";
}

export interface MemberComment {
  id: number;
  content: string;
  postId: number;
  postTitle: string;
}

export interface MemberActivity {
  member: MemberProfile;
  posts: PostSummary[];
  comments: MemberComment[];
}

export class SignupValidationException extends Error {
  readonly errors: SignupValidationError[];

  constructor(errors: SignupValidationError[]) {
    super("입력값을 확인해주세요.");
    this.errors = errors;
  }
}

export async function signup(request: SignupRequest): Promise<void> {
  const csrfToken = await getCsrfToken();
  const response = await apiFetch("/api/v1/members", {
    method: "POST",
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
      [csrfToken.headerName]: csrfToken.token,
    },
    body: JSON.stringify(request),
  });
  if (!response.ok) {
    const clonedResponse = response.clone();
    const data = await clonedResponse.json().catch(() => null) as { validationErrors?: SignupValidationError[] } | null;
    if (data?.validationErrors) {
      throw new SignupValidationException(data.validationErrors);
    }
    throw new Error(await getErrorMessage(response, "회원가입에 실패했습니다."));
  }
}

export async function getMyPage(): Promise<MemberActivity> {
  const response = await apiFetch("/api/v1/members/me", { credentials: "include" });
  if (!response.ok) {
    throw new Error(await getErrorMessage(response, "회원 정보를 불러오지 못했습니다."));
  }
  return response.json() as Promise<MemberActivity>;
}

export async function withdraw(password: string): Promise<void> {
  const csrfToken = await getCsrfToken();
  const response = await apiFetch("/api/v1/members/me", {
    method: "DELETE",
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
      [csrfToken.headerName]: csrfToken.token,
    },
    body: JSON.stringify({ password }),
  });
  if (!response.ok) {
    throw new Error(await getErrorMessage(response, "회원탈퇴에 실패했습니다."));
  }
}
