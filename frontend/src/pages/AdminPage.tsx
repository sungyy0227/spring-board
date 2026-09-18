import { useEffect, useState, type FormEvent } from "react";
import { Link } from "react-router";
import { getAdminRuntime, resetBoard, resetDevData, searchMember } from "../api/admin";
import type { MemberProfile } from "../api/members";

export default function AdminPage() {
  const [devMode, setDevMode] = useState(false); const [keyword, setKeyword] = useState(""); const [member, setMember] = useState<MemberProfile | null>(null);
  const [confirmation, setConfirmation] = useState(""); const [message, setMessage] = useState<string | null>(null); const [error, setError] = useState<string | null>(null);
  useEffect(() => { getAdminRuntime().then((data) => setDevMode(data.devMode)).catch((reason: unknown) => setError(reason instanceof Error ? reason.message : "관리자 정보를 불러오지 못했습니다.")); }, []);
  async function handleSearch(mode: "loginId" | "nickname") { setError(null); setMember(null); try { setMember(await searchMember(keyword, mode)); } catch (reason) { setError(reason instanceof Error ? reason.message : "회원을 찾지 못했습니다."); } }
  async function handleBoardReset(event: FormEvent) { event.preventDefault(); if (!window.confirm("게시판의 모든 글, 댓글, 이미지를 삭제하시겠습니까?")) return; try { const result = await resetBoard(confirmation); setMessage(result.message); setError(null); setConfirmation(""); } catch (reason) { setError(reason instanceof Error ? reason.message : "초기화에 실패했습니다."); } }
  async function handleDevReset(target: "posts" | "members") { if (!window.confirm(`개발용 ${target === "posts" ? "게시글" : "회원"} 데이터를 초기화하시겠습니까?`)) return; try { const result = await resetDevData(target); setMessage(result.message); setError(null); } catch (reason) { setError(reason instanceof Error ? reason.message : "초기화에 실패했습니다."); } }
  return <main className="page account-page">
    <section className="hero-panel"><div><p className="eyebrow">ADMIN CONSOLE</p><h1>관리자 센터</h1><p>회원을 조회하고 게시판 데이터를 관리할 수 있습니다.</p></div><span className="badge">ADMIN</span></section>
    {message && <p className="alert success">{message}</p>}{error && <p className="alert error">{error}</p>}
    <section className="card"><div className="section-heading"><div><h2>회원 조회</h2><p>로그인 아이디 또는 닉네임으로 회원을 검색하세요.</p></div></div>
      <form className="search-form" onSubmit={(event) => { event.preventDefault(); void handleSearch("loginId"); }}><input value={keyword} onChange={(e) => setKeyword(e.target.value)} placeholder="아이디 또는 닉네임" /><button className="button primary">아이디 조회</button><button className="button secondary" type="button" onClick={() => void handleSearch("nickname")}>닉네임 조회</button></form>
      {member ? <div className="search-result"><strong>{member.nickname}</strong><span>{member.loginId}</span><Link className="button secondary" to={`/admin/members/${member.id}`}>상세보기</Link></div> : <p className="empty-state">검색 결과가 여기에 표시됩니다.</p>}
    </section>
    <section className="card danger-zone"><p className="eyebrow danger-text">DANGER ZONE</p><h2>게시판 데이터 리셋</h2><p>회원 정보는 유지하고 이미지, 댓글 및 게시글을 모두 삭제합니다.</p><form className="search-form" onSubmit={handleBoardReset}><input value={confirmation} onChange={(e) => setConfirmation(e.target.value)} placeholder="RESET BOARD" required /><button className="button danger">Board Data Reset</button></form></section>
    {devMode && <section className="card danger-zone"><p className="eyebrow danger-text">DEV ONLY</p><h2>개발 데이터 초기화</h2><div className="form-actions"><button className="button secondary" onClick={() => void handleDevReset("posts")}>Dev Post Reset</button><button className="button danger" onClick={() => void handleDevReset("members")}>Dev Member Reset</button></div></section>}
  </main>;
}
