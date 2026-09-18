export interface CsrfTokenResponse {
  headerName: string;
  parameterName: string;
  token: string;
}

export async function getCsrfToken(): Promise<CsrfTokenResponse> {
  const response = await fetch("/api/v1/csrf", {
    credentials: "include",
  });

  if (!response.ok) {
    throw new Error("보안 토큰을 불러오지 못했습니다.");
  }

  return response.json() as Promise<CsrfTokenResponse>;
}
