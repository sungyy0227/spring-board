interface ApiErrorResponse {
  message?: string;
}

export async function getErrorMessage(
  response: Response,
  fallbackMessage: string,
): Promise<string> {
  try {
    const errorResponse = (await response.json()) as ApiErrorResponse;

    if (typeof errorResponse.message === "string") {
      return errorResponse.message;
    }
  } catch {
    // JSON 형식의 오류 응답이 아니면 아래 기본 메시지를 사용한다.
  }

  return `${fallbackMessage} (${response.status})`;
}
