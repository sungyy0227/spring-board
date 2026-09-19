import { cleanup, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it } from "vitest";
import { MemoryRouter } from "react-router";
import App from "./App";
import { AuthContext } from "./auth/auth-context";

afterEach(cleanup);

describe("chat invite route", () => {
  it("renders the invite acceptance page for a token URL", async () => {
    render(
      <MemoryRouter initialEntries={["/chat/invites/test-token"]}>
        <AuthContext.Provider
          value={{
            user: {
              authenticated: true,
              id: 1,
              loginId: "member1",
              nickname: "회원1",
              role: "USER",
              admin: false,
            },
            isLoading: false,
            refreshUser: async () => undefined,
          }}
        >
          <App />
        </AuthContext.Provider>
      </MemoryRouter>,
    );

    expect(
      await screen.findByRole("heading", { name: "채팅방 초대" }),
    ).toBeInTheDocument();
  });
});
