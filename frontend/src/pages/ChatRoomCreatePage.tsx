import { useState, type FormEvent } from "react";
import { Link, useNavigate } from "react-router";
import { createChatRoom } from "../api/chat";
import "./ChatRoomCreatePage.css";

export default function ChatRoomCreatePage() {
  const navigate = useNavigate();
  const [roomName, setRoomName] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    const name = roomName.trim();
    if (name.length === 0) {
      setError("채팅방 이름을 입력해주세요.");
      return;
    }

    setIsSubmitting(true);
    setError(null);

    try {
      const room = await createChatRoom({ name });
      navigate(`/chat/${room.id}`);
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "채팅방을 만들지 못했습니다.");
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <main className="page chat-room-create-page">
      <section className="chat-room-create-card card">
        <header className="chat-room-create-heading">
          <div>
            <h1>채팅방 만들기</h1>

          </div>
          <Link className="chat-room-create-back-link" to="/chat">
            ← 채팅방 목록
          </Link>
        </header>

        <form className="chat-room-create-form" onSubmit={handleSubmit}>
          <label htmlFor="chat-room-name">
            <span>채팅방 이름</span>
            <input
              id="chat-room-name"
              type="text"
              value={roomName}
              placeholder="채팅방 이름을 입력하세요"
              autoFocus
              disabled={isSubmitting}
              onChange={(event) => setRoomName(event.target.value)}
            />
          </label>

          {error && <p className="field-error" role="alert">{error}</p>}

          <div className="form-actions">
            <Link className="button secondary" to="/chat">
              취소
            </Link>
            <button className="button primary" type="submit" disabled={isSubmitting}>
              {isSubmitting ? "만드는 중..." : "채팅방 만들기"}
            </button>
          </div>
        </form>
      </section>
    </main>
  );
}
