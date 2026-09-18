/*
 * AuthProvider.tsx에서 사용할 import
 * import { setUnauthorizedHandler } from "../api/apiClient";
 *
 * posts.ts, comments.ts, members.ts, images.ts, admin.ts에서 사용할 import
 * import { apiFetch } from "./apiClient";
 */

export type UnauthorizedHandler = () => void;
let unauthorizedHandler: UnauthorizedHandler | null = null;

export function setUnauthorizedHandler(
  handler: UnauthorizedHandler | null,
): void {
  unauthorizedHandler = handler;
}

export async function apiFetch(
  input: RequestInfo | URL,
  init?: RequestInit,
): Promise<Response> {
  const response = await fetch(input, init);

  if (response.status === 401) {
    if (unauthorizedHandler !== null) {
      unauthorizedHandler();
    }
  }

  return response;
}
