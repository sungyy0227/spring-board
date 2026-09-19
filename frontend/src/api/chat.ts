import { apiFetch } from "./apiClient";
import { getCsrfToken } from "./csrf";
import { getErrorMessage } from "./errors";

export interface ChatRoomResponse {
  id: number;
  name: string;
}

export interface ChatRoomCreateRequest {
  name: string;
}

export interface ChatRoomInviteResponse {
  inviteUrl: string;
  expiresAt: string;
}

export interface ChatMessageResponse {
  senderId: number;
  senderNickname: string;
  content: string;
  sentAt: string;
}

export async function getMyChatRooms(): Promise<ChatRoomResponse[]> {
  const response = await apiFetch("/api/v1/chat/rooms", {
    credentials: "include",
  });

  if (!response.ok) {
    throw new Error(await getErrorMessage(response, "채팅방 목록을 불러오지 못했습니다."));
  }

  return response.json() as Promise<ChatRoomResponse[]>;
}

export async function createChatRoom(
  request: ChatRoomCreateRequest,
): Promise<ChatRoomResponse> {
  const csrfToken = await getCsrfToken();
  const response = await apiFetch("/api/v1/chat/rooms", {
    method: "POST",
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
      [csrfToken.headerName]: csrfToken.token,
    },
    body: JSON.stringify(request),
  });

  if (!response.ok) {
    throw new Error(await getErrorMessage(response, "채팅방을 만들지 못했습니다."));
  }

  return response.json() as Promise<ChatRoomResponse>;
}

export async function createChatRoomInvite(
  roomId: number,
): Promise<ChatRoomInviteResponse> {
  const csrfToken = await getCsrfToken();
  const response = await apiFetch(`/api/v1/chat/rooms/${roomId}/invites`, {
    method: "POST",
    credentials: "include",
    headers: {
      [csrfToken.headerName]: csrfToken.token,
    },
  });

  if (!response.ok) {
    throw new Error(await getErrorMessage(response, "초대 링크를 만들지 못했습니다."));
  }

  return response.json() as Promise<ChatRoomInviteResponse>;
}

export async function getActiveChatRoomInvite(
  roomId: number,
): Promise<ChatRoomInviteResponse | null> {
  const response = await apiFetch(`/api/v1/chat/rooms/${roomId}/invites/active`, {
    credentials: "include",
  });

  if (response.status === 204) {
    return null;
  }

  if (!response.ok) {
    throw new Error(await getErrorMessage(response, "초대 링크를 불러오지 못했습니다."));
  }

  return response.json() as Promise<ChatRoomInviteResponse>;
}

export async function rotateChatRoomInvite(
  roomId: number,
): Promise<ChatRoomInviteResponse> {
  const csrfToken = await getCsrfToken();
  const response = await apiFetch(`/api/v1/chat/rooms/${roomId}/invites/rotate`, {
    method: "POST",
    credentials: "include",
    headers: {
      [csrfToken.headerName]: csrfToken.token,
    },
  });

  if (!response.ok) {
    throw new Error(await getErrorMessage(response, "초대 링크를 새로 만들지 못했습니다."));
  }

  return response.json() as Promise<ChatRoomInviteResponse>;
}

export async function getChatRoom(roomId: number): Promise<ChatRoomResponse> {
  const response = await apiFetch(`/api/v1/chat/rooms/${roomId}`, {
    credentials: "include",
  });

  if (!response.ok) {
    throw new Error(await getErrorMessage(response, "채팅방 정보를 불러오지 못했습니다."));
  }

  return response.json() as Promise<ChatRoomResponse>;
}

export async function getChatMessages(roomId: number): Promise<ChatMessageResponse[]> {
  const response = await apiFetch(`/api/v1/chat/rooms/${roomId}/messages`, {
    credentials: "include",
  });

  if (!response.ok) {
    throw new Error(await getErrorMessage(response, "채팅 내역을 불러오지 못했습니다."));
  }

  return response.json() as Promise<ChatMessageResponse[]>;
}
