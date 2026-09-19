import { cleanup, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { createMemoryRouter, RouterProvider } from "react-router";
import ChatRoomInvitePage from "./ChatRoomInvitePage";

const inviteApi = vi.hoisted(() => ({
  getChatRoomInvite: vi.fn(),
  joinChatRoomByInvite: vi.fn(),
}));

vi.mock("../api/chat", () => inviteApi);

function renderInvitePage() {
  const router = createMemoryRouter(
    [
      {
        path: "/chat/invites/:token",
        element: <ChatRoomInvitePage />,
      },
      {
        path: "/chat/:roomId",
        element: <h1>입장한 채팅방</h1>,
      },
    ],
    { initialEntries: ["/chat/invites/test-token"] },
  );

  render(<RouterProvider router={router} />);
  return router;
}

beforeEach(() => {
  inviteApi.getChatRoomInvite.mockResolvedValue({
    roomName: "스터디방",
    expiresAt: "2026-09-27T12:00:00",
  });
  inviteApi.joinChatRoomByInvite.mockResolvedValue({ roomId: 42 });
});

afterEach(() => {
  cleanup();
  vi.clearAllMocks();
});

describe("ChatRoomInvitePage", () => {
  it("shows the invited room and a join button", async () => {
    renderInvitePage();

    expect(await screen.findByRole("heading", { name: "스터디방" })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "참여하기" })).toBeEnabled();
  });

  it("joins with the URL token and moves to the chat room", async () => {
    const user = userEvent.setup();
    const router = renderInvitePage();

    await user.click(await screen.findByRole("button", { name: "참여하기" }));

    expect(inviteApi.joinChatRoomByInvite).toHaveBeenCalledWith("test-token");
    expect(await screen.findByRole("heading", { name: "입장한 채팅방" })).toBeInTheDocument();
    expect(router.state.location.pathname).toBe("/chat/42");
  });

  it("shows an error instead of the join button for an invalid invite", async () => {
    inviteApi.getChatRoomInvite.mockRejectedValue(
      new Error("유효하지 않거나 만료된 초대 링크입니다."),
    );

    renderInvitePage();

    expect(await screen.findByRole("alert")).toHaveTextContent(
      "유효하지 않거나 만료된 초대 링크입니다.",
    );
    expect(screen.queryByRole("button", { name: "참여하기" })).not.toBeInTheDocument();
  });
});
