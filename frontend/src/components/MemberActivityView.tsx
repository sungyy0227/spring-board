import { Link } from "react-router";
import type { MemberActivity } from "../api/members";

export default function MemberActivityView({ data, adminView = false }: { data: MemberActivity; adminView?: boolean }) {
  return <>
    <section className="card">
      <div className="section-heading"><div><h2>회원 정보</h2><p>계정의 기본 정보와 현재 권한입니다.</p></div></div>
      <dl className="profile-grid">
        {adminView && <div><dt>회원 번호</dt><dd>{data.member.id}</dd></div>}
        <div><dt>로그인 아이디</dt><dd>{data.member.loginId}</dd></div><div><dt>닉네임</dt><dd>{data.member.nickname}</dd></div>
        {adminView && <><div><dt>권한</dt><dd>{data.member.role}</dd></div><div><dt>상태</dt><dd>{data.member.status}</dd></div></>}
      </dl>
    </section>
    <section className="card"><div className="section-heading"><div><h2>작성한 글</h2><p>{adminView ? "이 회원이 작성한 게시글입니다." : "내가 등록한 게시글을 확인할 수 있습니다."}</p></div><span className="badge">{data.posts.length}개</span></div>
      {data.posts.length === 0 ? <p className="empty-state">작성한 글이 없습니다.</p> : <div className="table-wrap"><table className="data-table"><thead><tr><th>번호</th><th>제목</th><th>조회수</th></tr></thead><tbody>{data.posts.map((post) => <tr key={post.id}><td>{post.id}</td><td className="title-cell"><Link to={`/posts/${post.id}`}>{post.title}</Link></td><td>{post.viewCount}</td></tr>)}</tbody></table></div>}
    </section>
    <section className="card"><div className="section-heading"><div><h2>작성한 댓글</h2><p>{adminView ? "이 회원이 남긴 댓글입니다." : "내가 남긴 댓글과 원문을 함께 확인할 수 있습니다."}</p></div><span className="badge">{data.comments.length}개</span></div>
      {data.comments.length === 0 ? <p className="empty-state">작성한 댓글이 없습니다.</p> : <div className="table-wrap"><table className="data-table"><thead><tr><th>번호</th><th>댓글 내용</th><th>작성 글</th></tr></thead><tbody>{data.comments.map((comment) => <tr key={comment.id}><td>{comment.id}</td><td>{comment.content}</td><td className="title-cell"><Link to={`/posts/${comment.postId}`}>{comment.postTitle}</Link></td></tr>)}</tbody></table></div>}
    </section>
  </>;
}
