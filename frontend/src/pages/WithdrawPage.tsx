import { useState, type FormEvent } from "react";
import { Link, useNavigate } from "react-router";
import { withdraw } from "../api/members";
import { useAuth } from "../auth/useAuth";

export default function WithdrawPage() {
  const navigate = useNavigate(); const { refreshUser } = useAuth(); const [password, setPassword] = useState(""); const [error, setError] = useState<string | null>(null);
  async function handleSubmit(event: FormEvent) { event.preventDefault(); if (!window.confirm("정말 회원탈퇴하시겠습니까?")) return; try { await withdraw(password); await refreshUser(); navigate("/"); } catch (reason) { setError(reason instanceof Error ? reason.message : "회원탈퇴에 실패했습니다."); } }
  return <main className="page narrow-page"><section className="card withdraw-card"><div className="warning-icon">!</div><p className="eyebrow danger-text">ACCOUNT WITHDRAWAL</p><h1>회원탈퇴</h1><p>탈퇴하기 전에 아래 내용을 꼭 확인해 주세요.</p><ul><li>탈퇴한 계정으로는 더 이상 로그인할 수 없습니다.</li><li>작성한 글과 댓글은 탈퇴 후에도 유지됩니다.</li></ul>{error && <p className="alert error">{error}</p>}<form className="stack-form" onSubmit={handleSubmit}><label><span>비밀번호 확인</span><input type="password" value={password} onChange={(e) => setPassword(e.target.value)} autoComplete="current-password" placeholder="현재 비밀번호" /></label><div className="form-actions"><Link className="button secondary" to="/mypage">취소</Link><button className="button danger">회원탈퇴</button></div></form></section></main>;
}
