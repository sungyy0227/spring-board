import { afterEach, describe, expect, it, vi } from "vitest";
import * as chatApiModule from "./chat";

interface InviteApi {
  getChatRoomInvite?: (token: string) => Promise<{
    roomName: string;
    expiresAt: string;
  }>;
  joinChatRoomByInvite?: (token: string) => Promise<{ roomId: number }>;
}

const chatApi = chatApiModule as InviteApi;

afterEach(() => {
  vi.unstubAllGlobals();
  vi.restoreAllMocks();
});

describe("chat invite API", () => {
  it("loads invite information with an encoded token", async () => {
    const fetchMock = vi.fn().mockResolvedValue(
      new Response(
        JSON.stringify({
          roomName: "스터디방",
          expiresAt: "2026-09-27T12:00:00",
        }),
        { status: 200, headers: { "Content-Type": "application/json" } },
      ),
    );
    vi.stubGlobal("fetch", fetchMock);

    expect(chatApi.getChatRoomInvite).toEqual(expect.any(Function));
    if (!chatApi.getChatRoomInvite) return;

    await expect(chatApi.getChatRoomInvite("invite/token")).resolves.toEqual({
      roomName: "스터디방",
      expiresAt: "2026-09-27T12:00:00",
    });
    expect(fetchMock).toHaveBeenCalledWith(
      "/api/v1/chat/invites/invite%2Ftoken",
      { credentials: "include" },
    );
  });

  it("joins through an invite with CSRF protection", async () => {
    const fetchMock = vi
      .fn()
      .mockResolvedValueOnce(
        new Response(
          JSON.stringify({
            headerName: "X-CSRF-TOKEN",
            parameterName: "_csrf",
            token: "csrf-token",
          }),
          { status: 200, headers: { "Content-Type": "application/json" } },
        ),
      )
      .mockResolvedValueOnce(
        new Response(JSON.stringify({ roomId: 42 }), {
          status: 200,
          headers: { "Content-Type": "application/json" },
        }),
      );
    vi.stubGlobal("fetch", fetchMock);

    expect(chatApi.joinChatRoomByInvite).toEqual(expect.any(Function));
    if (!chatApi.joinChatRoomByInvite) return;

    await expect(chatApi.joinChatRoomByInvite("test-token")).resolves.toEqual({
      roomId: 42,
    });
    expect(fetchMock).toHaveBeenNthCalledWith(
      2,
      "/api/v1/chat/invites/test-token/join",
      {
        method: "POST",
        credentials: "include",
        headers: { "X-CSRF-TOKEN": "csrf-token" },
      },
    );
  });
});
