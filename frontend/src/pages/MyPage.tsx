import { useEffect, useState } from "react";
import { Link } from "react-router";
import { getMyPage, type MemberActivity } from "../api/members";
import MemberActivityView from "../components/MemberActivityView";

export default function MyPage() {
  const [data, setData] = useState<MemberActivity | null>(null); const [error, setError] = useState<string | null>(null);
  useEffect(() => { getMyPage().then(setData).catch((reason: unknown) => setError(reason instanceof Error ? reason.message : "회원 정보를 불러오지 못했습니다.")); }, []);
  if (!data) return <main className="page"><p className={error ? "alert error" : "status-message"}>{error ?? "마이페이지를 불러오는 중입니다."}</p></main>;
  return <main className="page account-page"><section className="hero-panel"><div><p className="eyebrow">MY ACCOUNT</p><h1>마이페이지</h1><p>{data.member.nickname} 님의 활동을 한곳에서 확인하세요.</p></div><div className="activity-summary"><strong>{data.posts.length}<small>작성한 글</small></strong><strong>{data.comments.length}<small>작성한 댓글</small></strong></div></section>
    <MemberActivityView data={data} /><section className="card account-danger-row"><div><strong>계정 관리</strong><p>더 이상 서비스를 이용하지 않는다면 계정을 탈퇴할 수 있습니다.</p></div><Link className="text-danger" to="/mypage/withdraw">회원탈퇴</Link></section>
  </main>;
}
