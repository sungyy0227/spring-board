import { useState, type FormEvent } from "react";
import { Link, useNavigate } from "react-router";
import { signup, SignupValidationException, type SignupValidationError } from "../api/members";

export default function SignupPage() {
  const navigate = useNavigate();
  const [loginId, setLoginId] = useState(""); const [password, setPassword] = useState(""); const [nickname, setNickname] = useState("");
  const [errors, setErrors] = useState<SignupValidationError[]>([]); const [generalError, setGeneralError] = useState<string | null>(null);
  async function handleSubmit(event: FormEvent) {
    event.preventDefault(); setErrors([]); setGeneralError(null);
    try { await signup({ loginId, password, nickname }); navigate("/login", { state: { successMessage: "회원가입이 완료되었습니다." } }); }
    catch (reason) { if (reason instanceof SignupValidationException) setErrors(reason.errors); else setGeneralError(reason instanceof Error ? reason.message : "회원가입에 실패했습니다."); }
  }
  const fieldErrors = (field: SignupValidationError["fieldName"]) => errors.filter((error) => error.fieldName === field);
  return <main className="page auth-page"><section className="auth-card card">
    <div className="auth-heading"><p className="eyebrow">CREATE ACCOUNT</p><h1>회원가입</h1><p>간단한 정보로 계정을 만들고 게시판에 참여해 보세요.</p></div>
    {generalError && <p className="alert error">{generalError}</p>}
    <form className="stack-form" onSubmit={handleSubmit}>
      <label><span>아이디</span><input value={loginId} onChange={(e) => setLoginId(e.target.value)} placeholder="영문·숫자 5~20자" autoComplete="username" /></label>
      {fieldErrors("loginId").map((error) => <p className="field-error" key={error.errorCode}>{error.errorMessage}</p>)}
      <label><span>비밀번호</span><input type="password" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="영문·숫자 6~20자" autoComplete="new-password" /></label>
      {fieldErrors("password").map((error) => <p className="field-error" key={error.errorCode}>{error.errorMessage}</p>)}
      <label><span>닉네임</span><input value={nickname} onChange={(e) => setNickname(e.target.value)} placeholder="한글·영문·숫자·_ 2~12자" /></label>
      {fieldErrors("nickname").map((error) => <p className="field-error" key={error.errorCode}>{error.errorMessage}</p>)}
      <button className="button primary wide">회원가입</button>
    </form><p className="auth-switch">이미 계정이 있으신가요? <Link to="/login">로그인</Link></p>
  </section></main>;
}
