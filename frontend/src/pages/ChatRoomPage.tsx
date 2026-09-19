import { Client, type IMessage } from "@stomp/stompjs";
import { useEffect, useRef, useState, type FormEvent } from "react";
import { Link, useParams } from "react-router";
import {
  createChatRoomInvite,
  getActiveChatRoomInvite,
  getChatMessages,
  getChatRoom,
  rotateChatRoomInvite,
  type ChatRoomInviteResponse,
  type ChatMessageResponse,
  type ChatRoomResponse,
} from "../api/chat";
import { useAuth } from "../auth/useAuth";
import "./ChatPage.css";

interface ChatMessage extends ChatMessageResponse {
  clientKey: number;
}

type ConnectionStatus = "idle" | "connecting" | "connected" | "disconnected";

function getWebSocketUrl() {
  const configuredUrl = import.meta.env.VITE_WEBSOCKET_URL;
  if (configuredUrl) return configuredUrl;

  const protocol = window.location.protocol === "https:" ? "wss:" : "ws:";
  return `${protocol}//${window.location.host}/ws`;
}

function parseMessage(message: IMessage): ChatMessageResponse {
  const parsed: unknown = JSON.parse(message.body);

  if (
    typeof parsed !== "object" ||
    parsed === null ||
    typeof (parsed as ChatMessageResponse).senderId !== "number" ||
    typeof (parsed as ChatMessageResponse).senderNickname !== "string" ||
    typeof (parsed as ChatMessageResponse).content !== "string" ||
    typeof (parsed as ChatMessageResponse).sentAt !== "string"
  ) {
    throw new Error("서버에서 올바르지 않은 채팅 메시지를 받았습니다.");
  }

  return parsed as ChatMessageResponse;
}

function formatMessageTime(value: string) {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "";

  return new Intl.DateTimeFormat("ko-KR", {
    hour: "2-digit",
    minute: "2-digit",
    hour12: false,
  }).format(date);
}

export default function ChatRoomPage() {
  const { roomId: roomIdParam } = useParams();
  const parsedRoomId = Number(roomIdParam);
  const roomId = Number.isSafeInteger(parsedRoomId) && parsedRoomId > 0 ? parsedRoomId : null;
  const { user } = useAuth();
  const clientRef = useRef<Client | null>(null);
  const messageSequence = useRef(0);
  const messageListRef = useRef<HTMLDivElement | null>(null);
  const [room, setRoom] = useState<ChatRoomResponse | null>(null);
  const [roomReady, setRoomReady] = useState(false);
  const [loading, setLoading] = useState(true);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [content, setContent] = useState("");
  const [status, setStatus] = useState<ConnectionStatus>("idle");
  const [error, setError] = useState<string | null>(null);
  const [invite, setInvite] = useState<ChatRoomInviteResponse | null>(null);
  const [inviteError, setInviteError] = useState<string | null>(null);
  const [isLoadingInvite, setIsLoadingInvite] = useState(false);
  const [isRotatingInvite, setIsRotatingInvite] = useState(false);
  const [inviteCopied, setInviteCopied] = useState(false);

  useEffect(() => {
    let active = true;

    async function loadRoom() {
      if (roomId === null) {
        setError("올바르지 않은 채팅방 주소입니다.");
        setLoading(false);
        return;
      }

      try {
        const [roomResponse, messageList] = await Promise.all([
          getChatRoom(roomId),
          getChatMessages(roomId),
        ]);
        if (!active) return;

        const initialMessages = messageList.map((message) => {
          messageSequence.current += 1;
          return { ...message, clientKey: messageSequence.current };
        });

        setRoom(roomResponse);
        setMessages(initialMessages);
        setStatus("connecting");
        setRoomReady(true);
      } catch (reason) {
        if (!active) return;
        setError(reason instanceof Error ? reason.message : "채팅방을 불러오지 못했습니다.");
      } finally {
        if (active) setLoading(false);
      }
    }

    void loadRoom();

    return () => {
      active = false;
    };
  }, [roomId]);

  useEffect(() => {
    if (roomId === null || !roomReady) return;

    let active = true;
    const client = new Client({
      brokerURL: getWebSocketUrl(),
      reconnectDelay: 3_000,
      heartbeatIncoming: 10_000,
      heartbeatOutgoing: 10_000,
    });

    client.onConnect = () => {
      if (!active) return;
      setStatus("connected");
      setError(null);

      client.subscribe(`/topic/chat/rooms/${roomId}`, (message) => {
        if (!active) return;

        try {
          const received = parseMessage(message);
          messageSequence.current += 1;
          setMessages((current) => [
            ...current,
            { ...received, clientKey: messageSequence.current },
          ]);
        } catch (reason) {
          setError(reason instanceof Error ? reason.message : "채팅 메시지를 읽지 못했습니다.");
        }
      });
    };

    client.onStompError = (frame) => {
      if (!active) return;
      setStatus("disconnected");
      setError(frame.headers.message ?? "채팅 서버에서 오류가 발생했습니다.");
    };

    client.onWebSocketClose = () => {
      if (!active) return;
      setStatus("connecting");
      setError("채팅 연결이 끊어졌습니다. 다시 연결하고 있습니다.");
    };

    client.onWebSocketError = () => {
      if (!active) return;
      setStatus("disconnected");
      setError("채팅 서버에 연결하지 못했습니다.");
    };

    clientRef.current = client;
    client.activate();

    return () => {
      active = false;
      if (clientRef.current === client) clientRef.current = null;
      void client.deactivate();
    };
  }, [roomId, roomReady]);

  useEffect(() => {
    const messageList = messageListRef.current;
    if (messageList === null) return;

    messageList.scrollTo({
      top: messageList.scrollHeight,
      behavior: "smooth",
    });
  }, [messages]);

  async function handleOpenInvite() {
    if (roomId === null) return;

    setIsLoadingInvite(true);
    setInviteError(null);
    setInviteCopied(false);

    try {
      const activeInvite = await getActiveChatRoomInvite(roomId);
      setInvite(activeInvite ?? await createChatRoomInvite(roomId));
    } catch (reason) {
      setInviteError(reason instanceof Error ? reason.message : "초대 링크를 불러오지 못했습니다.");
    } finally {
      setIsLoadingInvite(false);
    }
  }

  async function handleRotateInvite() {
    if (roomId === null) return;

    setIsRotatingInvite(true);
    setInviteError(null);
    setInviteCopied(false);

    try {
      setInvite(await rotateChatRoomInvite(roomId));
    } catch (reason) {
      setInviteError(reason instanceof Error ? reason.message : "초대 링크를 새로 만들지 못했습니다.");
    } finally {
      setIsRotatingInvite(false);
    }
  }

  async function handleCopyInvite() {
    if (invite === null) return;

    const inviteUrl = new URL(invite.inviteUrl, window.location.origin).toString();
    try {
      await navigator.clipboard.writeText(inviteUrl);
      setInviteCopied(true);
      setInviteError(null);
    } catch {
      setInviteError("초대 링크를 복사하지 못했습니다.");
    }
  }

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const messageContent = content.trim();
    const client = clientRef.current;

    if (!messageContent || !client?.connected || roomId === null) return;

    client.publish({
      destination: `/app/chat/rooms/${roomId}/send`,
      body: JSON.stringify({ content: messageContent }),
    });
    setContent("");
  }

  const statusLabel = status === "connected"
    ? "연결됨"
    : status === "connecting"
      ? "연결 중"
      : "연결 끊김";

  if (loading) {
    return <main className="page"><p className="status-message">채팅방을 불러오는 중입니다.</p></main>;
  }

  if (room === null) {
    return (
      <main className="page narrow-page">
        <section className="card error-page">
          <h1>채팅방을 열 수 없습니다</h1>
          <p>{error ?? "채팅방 정보를 찾지 못했습니다."}</p>
          <Link className="button secondary" to="/chat">채팅방 목록</Link>
        </section>
      </main>
    );
  }

  return (
    <main className="page chat-page">
      <section className="chat-card card">
        <header className="chat-heading">
          <div>
            <p className="eyebrow">LIVE CHAT</p>
            <h1>{room.name}</h1>
            <Link className="chat-back-link" to="/chat">← 채팅방 목록</Link>
          </div>
          <div className="chat-heading-actions">
            <span className={`chat-status ${status}`}>
              <span aria-hidden="true" />
              {statusLabel}
            </span>
            <button
              className="button secondary chat-invite-button"
              type="button"
              disabled={isLoadingInvite}
              onClick={() => void handleOpenInvite()}
            >
              {isLoadingInvite ? "확인 중..." : "초대 링크"}
            </button>
          </div>
        </header>

        {error && <p className="alert error" role="alert">{error}</p>}

        {inviteError && <p className="alert error chat-invite-alert" role="alert">{inviteError}</p>}

        {invite && (
          <div className="chat-invite-panel" aria-live="polite">
            <div className="chat-invite-meta">
              <strong>초대 링크</strong>
              <small>{new Date(invite.expiresAt).toLocaleString("ko-KR")}까지 유효</small>
            </div>
            <input
              aria-label="생성된 초대 링크"
              value={new URL(invite.inviteUrl, window.location.origin).toString()}
              readOnly
            />
            <div className="chat-invite-actions">
              <button className="button secondary" type="button" onClick={() => void handleCopyInvite()}>
                {inviteCopied ? "복사됨" : "복사"}
              </button>
              <button
                className="button secondary"
                type="button"
                disabled={isRotatingInvite}
                onClick={() => void handleRotateInvite()}
              >
                {isRotatingInvite ? "생성 중..." : "새 링크 만들기"}
              </button>
            </div>
          </div>
        )}

        <div className="chat-message-list" ref={messageListRef} aria-live="polite">
          {messages.length === 0 && (
            <p className="chat-empty">
              {status === "connected" ? "첫 번째 메시지를 보내보세요." : "채팅 서버에 연결하고 있습니다."}
            </p>
          )}
          {messages.map((message) => {
            const isMine = message.senderId === user?.id;
            return (
              <article className={`chat-message ${isMine ? "mine" : ""}`} key={message.clientKey}>
                {!isMine && <strong>{message.senderNickname}</strong>}
                <div className="chat-bubble-row">
                  <p>{message.content}</p>
                  <time dateTime={message.sentAt}>{formatMessageTime(message.sentAt)}</time>
                </div>
              </article>
            );
          })}
        </div>

        <form className="chat-form" onSubmit={handleSubmit}>
          <label className="sr-only" htmlFor="chat-content">메시지</label>
          <input
            id="chat-content"
            value={content}
            onChange={(event) => setContent(event.target.value)}
            placeholder={status === "connected" ? "메시지를 입력하세요" : "서버 연결을 기다리고 있습니다"}
            maxLength={500}
            autoComplete="off"
            disabled={status !== "connected"}
          />
          <button className="button primary" type="submit" disabled={status !== "connected" || !content.trim()}>
            전송
          </button>
        </form>
      </section>
    </main>
  );
}
