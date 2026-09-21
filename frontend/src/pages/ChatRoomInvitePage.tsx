import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router";
import {
  getChatRoomInvite,
  joinChatRoomByInvite,
  type ChatRoomInvitePreviewResponse,
} from "../api/chat";
import { formatDate } from "../utils/date";
import "./ChatRoomInvitePage.css";

export default function ChatRoomInvitePage() {
  const { token } = useParams();
  const navigate = useNavigate();
  const [invite, setInvite] = useState<ChatRoomInvitePreviewResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isJoining, setIsJoining] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let active = true;

    async function loadInvite() {
      if (!token) {
        setError("올바르지 않은 초대 링크입니다.");
        setIsLoading(false);
        return;
      }

      try {
        const inviteResponse = await getChatRoomInvite(token);
        if (active) setInvite(inviteResponse);
      } catch (reason) {
        if (!active) return;
        setError(
          reason instanceof Error
            ? reason.message
            : "유효하지 않거나 만료된 초대 링크입니다.",
        );
      } finally {
        if (active) setIsLoading(false);
      }
    }

    void loadInvite();
    return () => {
      active = false;
    };
  }, [token]);

  async function handleJoin() {
    if (!token || isJoining) return;

    setIsJoining(true);
    setError(null);

    try {
      const result = await joinChatRoomByInvite(token);
      navigate(`/chat/${result.roomId}`, { replace: true });
    } catch (reason) {
      setError(
        reason instanceof Error ? reason.message : "채팅방에 참여하지 못했습니다.",
      );
      setIsJoining(false);
    }
  }

  return (
    <main className="page chat-room-invite-page">
      <section className="chat-room-invite-card card">
        <h1>채팅방 초대</h1>

        {isLoading && (
          <p className="status-message">초대 정보를 확인하고 있습니다.</p>
        )}

        {!isLoading && invite && (
          <div className="chat-room-invite-content">
            <div className="chat-room-invite-room" aria-hidden="true">#</div>
            <div>
              <p className="chat-room-invite-label">초대받은 채팅방</p>
              <h2>{invite.roomName}</h2>
              <p className="chat-room-invite-expiry">
                초대 만료: {formatDate(invite.expiresAt, true)}
              </p>
            </div>
          </div>
        )}

        {error && <p className="field-error" role="alert">{error}</p>}

        <div className="chat-room-invite-actions">
          <Link className="button secondary" to="/chat">
            채팅방 목록
          </Link>
          {invite && (
            <button
              className="button primary"
              type="button"
              disabled={isJoining}
              onClick={() => void handleJoin()}
            >
              {isJoining ? "참여하는 중..." : "참여하기"}
            </button>
          )}
        </div>
      </section>
    </main>
  );
}
