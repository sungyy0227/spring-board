import { apiFetch } from "./apiClient";
import { getCsrfToken } from "./csrf";
import { getErrorMessage } from "./errors";
import type { MemberActivity, MemberProfile } from "./members";

export async function getAdminRuntime(): Promise<{ devMode: boolean }> {
  const response = await apiFetch("/api/v1/admin/runtime", { credentials: "include" });
  if (!response.ok) throw new Error("관리자 정보를 불러오지 못했습니다.");
  return response.json() as Promise<{ devMode: boolean }>;
}

export async function searchMember(keyword: string, mode: string): Promise<MemberProfile> {
  const params = new URLSearchParams({ keyword, mode });
  const response = await apiFetch(`/api/v1/admin/members?${params}`, { credentials: "include" });
  if (!response.ok) throw new Error(await getErrorMessage(response, "회원을 찾지 못했습니다."));
  return response.json() as Promise<MemberProfile>;
}

export async function getMember(memberId: number): Promise<MemberActivity> {
  const response = await apiFetch(`/api/v1/admin/members/${memberId}`, { credentials: "include" });
  if (!response.ok) throw new Error(await getErrorMessage(response, "회원 정보를 불러오지 못했습니다."));
  return response.json() as Promise<MemberActivity>;
}

async function post(path: string, body?: unknown): Promise<Response> {
  const csrfToken = await getCsrfToken();
  return apiFetch(path, {
    method: "POST",
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
      [csrfToken.headerName]: csrfToken.token,
    },
    body: body === undefined ? undefined : JSON.stringify(body),
  });
}

export async function changeAdminRole(memberId: number, grant: boolean): Promise<void> {
  const action = grant ? "grant-admin" : "remove-admin";
  const response = await post(`/api/v1/admin/members/${memberId}/${action}`);
  if (!response.ok) throw new Error(await getErrorMessage(response, "회원 권한을 변경하지 못했습니다."));
}

export async function resetBoard(confirmation: string): Promise<{ message: string }> {
  const response = await post("/api/v1/admin/reset/board", { confirmation });
  if (!response.ok) throw new Error(await getErrorMessage(response, "게시판 초기화에 실패했습니다."));
  return response.json() as Promise<{ message: string }>;
}

export async function resetDevData(target: "posts" | "members"): Promise<{ message: string }> {
  const response = await post(`/api/v1/admin/dev/reset/${target}`);
  if (!response.ok) throw new Error(await getErrorMessage(response, "개발 데이터를 초기화하지 못했습니다."));
  return response.json() as Promise<{ message: string }>;
}
