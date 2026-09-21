import { useEffect, useState } from "react";
import { Link } from "react-router";
import { getMyChatRooms, type ChatRoomResponse } from "../api/chat";
import "./ChatRoomListPage.css";

export default function ChatRoomListPage() {
  const [rooms, setRooms] = useState<ChatRoomResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let active = true;

    async function loadRooms() {
      try {
        const roomList = await getMyChatRooms();
        if (active) setRooms(roomList);
      } catch (reason) {
        if (!active) return;
        setError(reason instanceof Error ? reason.message : "채팅방 목록을 불러오지 못했습니다.");
      } finally {
        if (active) setLoading(false);
      }
    }

    void loadRooms();

    return () => {
      active = false;
    };
  }, []);

  return (
    <main className="page chat-room-list-page">
      <section className="chat-room-list-card card">
        <header className="section-heading">
          <div>
            <h1>채팅방</h1>

          </div>
          <div className="chat-room-heading-actions">
            {!loading && <strong>{rooms.length}개</strong>}
            <Link
              className="button primary chat-room-create-button"
              to="/chat/new"
            >
              + 채팅방 만들기
            </Link>
          </div>
        </header>

        {error && <p className="alert error" role="alert">{error}</p>}

        {loading ? (
          <p className="status-message">채팅방을 불러오는 중입니다.</p>
        ) : rooms.length === 0 ? (
          <p className="empty-state">참여 중인 채팅방이 없습니다.</p>
        ) : (
          <div className="chat-room-list">
            {rooms.map((room) => (
              <Link className="chat-room-list-item" key={room.id} to={`/chat/${room.id}`}>
                <span>
                  <strong>{room.name}</strong>
                  <small>채팅방 #{room.id}</small>
                </span>
                <span aria-hidden="true">입장하기 →</span>
              </Link>
            ))}
          </div>
        )}
      </section>
    </main>
  );
}
