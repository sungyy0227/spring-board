import { getCsrfToken } from "./csrf";
import { getErrorMessage } from "./errors";

export interface CurrentUser {
  authenticated: boolean;
  id: number | null;
  loginId: string | null;
  nickname: string | null;
  role: string | null;
  admin: boolean;
}

export async function getCurrentUser(): Promise<CurrentUser> {
  const response = await fetch("/api/v1/auth/me", { credentials: "include" });
  if (!response.ok) {
    throw new Error("로그인 정보를 불러오지 못했습니다.");
  }
  return response.json() as Promise<CurrentUser>;
}

export async function login(loginId: string, password: string): Promise<void> {
  const csrfToken = await getCsrfToken();
  const body = new URLSearchParams({ loginId, password });
  const response = await fetch("/api/v1/auth/login", {
    method: "POST",
    credentials: "include",
    headers: {
      "Accept": "application/json",
      "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
      [csrfToken.headerName]: csrfToken.token,
    },
    body,
  });
  if (!response.ok) {
    throw new Error(await getErrorMessage(response, "로그인에 실패했습니다."));
  }
}

export async function logout(): Promise<void> {
  const csrfToken = await getCsrfToken();
  const response = await fetch("/api/v1/auth/logout", {
    method: "POST",
    credentials: "include",
    headers: {
      "Accept": "application/json",
      [csrfToken.headerName]: csrfToken.token,
    },
  });
  if (!response.ok) {
    throw new Error("로그아웃에 실패했습니다.");
  }
}
