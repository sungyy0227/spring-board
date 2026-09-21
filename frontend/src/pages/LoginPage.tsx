import { useState, type FormEvent } from "react";
import { Link, Navigate, useLocation, useNavigate } from "react-router";
import { login } from "../api/auth";
import { useAuth } from "../auth/useAuth";

export default function LoginPage() {
  const { user, refreshUser } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [loginId, setLoginId] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  if (user?.authenticated) return <Navigate to="/" replace />;

  async function handleSubmit(event: FormEvent) {
    event.preventDefault(); setSubmitting(true); setError(null);
    try { await login(loginId, password); await refreshUser(); navigate("/"); }
    catch (reason) { setError(reason instanceof Error ? reason.message : "로그인에 실패했습니다."); }
    finally { setSubmitting(false); }
  }

  const successMessage = (location.state as { successMessage?: string } | null)?.successMessage;
  return <main className="page auth-page"><section className="auth-card card">
    <div className="auth-heading"><h1>로그인</h1></div>
    {successMessage && <p className="alert success">{successMessage}</p>}{error && <p className="alert error">{error}</p>}
    <form className="stack-form" onSubmit={handleSubmit}>
      <label><span>아이디</span><input value={loginId} onChange={(e) => setLoginId(e.target.value)} autoComplete="username" required /></label>
      <label><span>비밀번호</span><input type="password" value={password} onChange={(e) => setPassword(e.target.value)} autoComplete="current-password" required /></label>
      <button className="button primary wide" disabled={submitting}>{submitting ? "로그인 중..." : "로그인"}</button>
    </form>
    <p className="auth-switch">아직 계정이 없으신가요? <Link to="/signup">회원가입</Link></p>
  </section></main>;
}
