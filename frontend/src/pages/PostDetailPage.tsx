import { useCallback, useEffect, useState, type FormEvent } from "react";
import { Link, useNavigate, useParams } from "react-router";
import { createComment, deleteComment } from "../api/comments";
import { deletePost, getPost, verifyPostEditAccess, type PostDetailResponse } from "../api/posts";
import { useAuth } from "../auth/useAuth";
import { formatDate } from "../utils/date";

export default function PostDetailPage() {
  const postId = Number(useParams().postId);
  const navigate = useNavigate();
  const { user } = useAuth();
  const [post, setPost] = useState<PostDetailResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [guestEditPassword, setGuestEditPassword] = useState("");
  const [guestDeletePassword, setGuestDeletePassword] = useState("");
  const [commenter, setCommenter] = useState("");
  const [commentContent, setCommentContent] = useState("");
  const [commentPassword, setCommentPassword] = useState("");
  const [deletePasswords, setDeletePasswords] = useState<Record<number, string>>({});

  const loadPost = useCallback(async () => {
    try {
      setPost(await getPost(postId));
      setError(null);
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "게시글을 불러오지 못했습니다.");
    }
  }, [postId]);

  useEffect(() => {
    let active = true;
    getPost(postId)
      .then((data) => { if (active) { setPost(data); setError(null); } })
      .catch((reason: unknown) => { if (active) setError(reason instanceof Error ? reason.message : "게시글을 불러오지 못했습니다."); });
    return () => { active = false; };
  }, [postId]);

  async function handleEdit() {
    try {
      await verifyPostEditAccess(postId, post?.memberId === null ? guestEditPassword : undefined);
      navigate(`/posts/${postId}/edit`);
    } catch (reason) { setError(reason instanceof Error ? reason.message : "수정 권한 확인에 실패했습니다."); }
  }

  async function handleDelete() {
    if (!window.confirm("게시글을 삭제하시겠습니까?")) return;
    try {
      await deletePost(postId, post?.memberId === null ? guestDeletePassword : undefined);
      navigate("/");
    } catch (reason) { setError(reason instanceof Error ? reason.message : "게시글 삭제에 실패했습니다."); }
  }

  async function handleCommentSubmit(event: FormEvent) {
    event.preventDefault();
    try {
      await createComment(postId, { commenter, content: commentContent, guestPassword: commentPassword });
      setCommentContent(""); setCommentPassword("");
      await loadPost();
    } catch (reason) { setError(reason instanceof Error ? reason.message : "댓글 등록에 실패했습니다."); }
  }

  async function handleCommentDelete(commentId: number) {
    try {
      await deleteComment(postId, commentId, deletePasswords[commentId]);
      await loadPost();
    } catch (reason) { setError(reason instanceof Error ? reason.message : "댓글 삭제에 실패했습니다."); }
  }

  if (error && !post) return <main className="page"><p className="alert error">{error}</p><Link to="/">목록으로</Link></main>;
  if (!post) return <main className="page"><p className="status-message">게시글을 불러오는 중입니다.</p></main>;
  const canManageMemberPost = post.memberId !== null && (user?.id === post.memberId || user?.admin);

  return (
    <main className="page post-detail-page">
      {error && <p className="alert error" role="alert">{error}</p>}
      <article className="card post-card">
        <header className="post-heading">
          <p className="eyebrow">POST #{post.id}</p><h1>{post.title}</h1>
          <div className="meta"><strong>{post.poster}</strong>{post.memberWithdrawn && <span className="badge">탈퇴 회원</span>}<span>{formatDate(post.createdAt, true)}</span><span>조회수 {post.viewCount}</span></div>
        </header>
        <div className="article-content" dangerouslySetInnerHTML={{ __html: post.content }} />
        <div className="post-actions">
          <Link className="button secondary" to="/">목록으로</Link>
          {canManageMemberPost && <><button className="button secondary" type="button" onClick={() => void handleEdit()}>수정</button><button className="button danger" type="button" onClick={() => void handleDelete()}>삭제</button></>}
          {post.memberId === null && <div className="guest-actions">
            <input type="password" value={guestEditPassword} onChange={(e) => setGuestEditPassword(e.target.value)} placeholder="수정 비밀번호" />
            <button className="button secondary" type="button" onClick={() => void handleEdit()}>수정</button>
            <input type="password" value={guestDeletePassword} onChange={(e) => setGuestDeletePassword(e.target.value)} placeholder="삭제 비밀번호" />
            <button className="button danger" type="button" onClick={() => void handleDelete()}>삭제</button>
          </div>}
        </div>
      </article>

      <section className="card comments-card">
        <div className="section-heading"><div><h2>댓글</h2></div><span className="badge">{post.comments.length}개</span></div>
        <div className="comment-list">
          {post.comments.length === 0 && <p className="empty-state">첫 번째 댓글을 남겨보세요.</p>}
          {post.comments.map((comment) => {
            const canDelete = comment.memberId === null || user?.id === comment.memberId || user?.admin;
            return <article className="comment-row" key={comment.id}>
              <div><strong>{comment.commenter}</strong>{comment.memberWithdrawn && <span className="badge">탈퇴</span>}<p>{comment.commentContent}</p></div>
              {canDelete && <div className="comment-delete">
                {comment.memberId === null && <input type="password" placeholder="비밀번호" value={deletePasswords[comment.id] ?? ""} onChange={(e) => setDeletePasswords((current) => ({ ...current, [comment.id]: e.target.value }))} />}
                <button className="text-danger" type="button" onClick={() => void handleCommentDelete(comment.id)}>삭제</button>
              </div>}
            </article>;
          })}
        </div>
        <form className="comment-form" onSubmit={handleCommentSubmit}>
          {!user?.authenticated ? <input value={commenter} onChange={(e) => setCommenter(e.target.value)} placeholder="작성자" /> : <strong>{user.nickname} 님</strong>}
          <textarea value={commentContent} onChange={(e) => setCommentContent(e.target.value)} rows={3} placeholder="댓글을 입력하세요" />
          {!user?.authenticated && <input type="password" value={commentPassword} onChange={(e) => setCommentPassword(e.target.value)} placeholder="비밀번호" />}
          <button className="button primary" type="submit">댓글 등록</button>
        </form>
      </section>
    </main>
  );
}
