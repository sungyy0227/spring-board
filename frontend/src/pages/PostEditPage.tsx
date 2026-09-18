import { useEffect, useRef, useState, type FormEvent } from "react";
import { Link, useNavigate, useParams } from "react-router";
import { getPostForEdit, updatePost, type PostEditResponse } from "../api/posts";
import PostEditor, { type PostEditorHandle } from "../components/PostEditor";

export default function PostEditPage() {
  const postId = Number(useParams().postId);
  const navigate = useNavigate();
  const editorRef = useRef<PostEditorHandle>(null);
  const [post, setPost] = useState<PostEditResponse | null>(null);
  const [title, setTitle] = useState("");
  const [poster, setPoster] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    getPostForEdit(postId).then((data) => { setPost(data); setTitle(data.title); setPoster(data.poster); })
      .catch((reason: unknown) => setError(reason instanceof Error ? reason.message : "게시글을 불러오지 못했습니다."));
  }, [postId]);

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    if (editorRef.current?.isUploading()) { setError("이미지 업로드가 끝난 후 수정해주세요."); return; }
    setSubmitting(true);
    try {
      await updatePost(postId, { title, poster, content: editorRef.current?.getContent() ?? "", imageIds: editorRef.current?.getUsedImageIds() ?? [] });
      navigate(`/posts/${postId}`);
    } catch (reason) { setError(reason instanceof Error ? reason.message : "게시글 수정에 실패했습니다."); }
    finally { setSubmitting(false); }
  }

  if (!post) return <main className="page"><p className={error ? "alert error" : "status-message"}>{error ?? "수정할 게시글을 불러오는 중입니다."}</p></main>;
  return <main className="page editor-page">
    <section className="card">
      <div className="section-heading"><div><p className="eyebrow">EDIT POST</p><h1>게시글 수정</h1><p>작성한 내용을 확인하고 필요한 부분을 수정해 보세요.</p></div></div>
      <form className="stack-form" onSubmit={handleSubmit}>
        <label><span>제목</span><input value={title} onChange={(e) => setTitle(e.target.value)} placeholder="제목을 입력하세요" /></label>
        <label><span>작성자</span>{post.guest ? <input value={poster} onChange={(e) => setPoster(e.target.value)} /> : <div className="readonly-field">{poster} 님</div>}</label>
        <div className="editor-field"><span>내용</span><PostEditor ref={editorRef} initialValue={post.content} /></div>
        {error && <p className="alert error">{error}</p>}
        <div className="form-actions"><Link className="button secondary" to={`/posts/${postId}`}>취소</Link><button className="button primary" disabled={submitting}>{submitting ? "수정 중..." : "수정 완료"}</button></div>
      </form>
    </section>
  </main>;
}
