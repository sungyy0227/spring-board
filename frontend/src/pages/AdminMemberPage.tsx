import { useEffect, useState } from "react";
import { Link, useParams } from "react-router";
import { changeAdminRole, getMember } from "../api/admin";
import type { MemberActivity } from "../api/members";
import MemberActivityView from "../components/MemberActivityView";

export default function AdminMemberPage() {
  const memberId = Number(useParams().memberId); const [data, setData] = useState<MemberActivity | null>(null); const [error, setError] = useState<string | null>(null);
  async function load() { try { setData(await getMember(memberId)); setError(null); } catch (reason) { setError(reason instanceof Error ? reason.message : "회원 정보를 불러오지 못했습니다."); } }
  useEffect(() => {
    let active = true;
    getMember(memberId)
      .then((member) => { if (active) { setData(member); setError(null); } })
      .catch((reason: unknown) => { if (active) setError(reason instanceof Error ? reason.message : "회원 정보를 불러오지 못했습니다."); });
    return () => { active = false; };
  }, [memberId]);
  async function handleRoleChange() { if (!data) return; try { await changeAdminRole(memberId, data.member.role !== "ADMIN"); await load(); } catch (reason) { setError(reason instanceof Error ? reason.message : "권한을 변경하지 못했습니다."); } }
  if (!data) return <main className="page"><p className={error ? "alert error" : "status-message"}>{error ?? "회원 정보를 불러오는 중입니다."}</p></main>;
  return <main className="page account-page"><section className="hero-panel"><div><h1>{data.member.nickname} 회원</h1></div><div className="form-actions"><span className="badge">{data.member.role}</span><button className={data.member.role === "ADMIN" ? "button secondary" : "button primary"} onClick={() => void handleRoleChange()}>{data.member.role === "ADMIN" ? "ADMIN 권한 해제" : "ADMIN 권한 부여"}</button></div></section>{error && <p className="alert error">{error}</p>}<MemberActivityView data={data} adminView /><div className="page-actions"><Link className="button secondary" to="/admin">관리자 센터로 돌아가기</Link></div></main>;
}
