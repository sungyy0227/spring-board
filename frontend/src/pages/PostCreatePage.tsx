import { useRef, useState, type FormEvent } from "react";
import { Link, useNavigate } from "react-router";
import { createPost, type PostCreateRequest } from "../api/posts";
import PostEditor, { type PostEditorHandle } from "../components/PostEditor";
import "./PostCreatePage.css";
import { useAuth } from "../auth/useAuth";

function PostCreatePage() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const editorRef = useRef<PostEditorHandle>(null);
  const [title, setTitle] = useState("");
  const [poster, setPoster] = useState("");
  const [guestPassword, setGuestPassword] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setErrorMessage(null);

    if (editorRef.current?.isUploading()) {
      setErrorMessage("이미지 업로드가 끝난 후 작성해주세요.");
      return;
    }

    setIsSubmitting(true);

    const request: PostCreateRequest = {
      title,
      content: editorRef.current?.getContent() ?? "",
      poster,
      guestPassword,
      imageIds: editorRef.current?.getUsedImageIds() ?? [],
    };

    try {
      const response = await createPost(request);
      navigate(`/posts/${response.postId}`);
    } catch (error) {
      if (error instanceof Error) {
        setErrorMessage(error.message);
      } else {
        setErrorMessage("알 수 없는 오류가 발생했습니다.");
      }
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <main className="post-create-page">
      <div className="post-create-heading">
        <div>
          <p className="eyebrow">NEW POST</p>
          <h1>새 글 작성</h1>
        </div>
        <Link className="back-link" to="/">
          목록으로
        </Link>
      </div>

      <form className="post-create-form" onSubmit={handleSubmit}>
        <label>
          <span>제목</span>
          <input
            type="text"
            value={title}
            onChange={(event) => setTitle(event.target.value)}
            placeholder="제목을 입력하세요"
            required
          />
        </label>

        <div className="editor-field">
          <span>내용</span>
          <PostEditor ref={editorRef} />
        </div>

        {!user?.authenticated && <fieldset>
          <legend>비회원 작성 정보</legend>
          <div className="guest-fields">
            <label>
              <span>작성자</span>
              <input
                type="text"
                value={poster}
                onChange={(event) => setPoster(event.target.value)}
                placeholder="비회원 작성자명"
              />
            </label>

            <label>
              <span>비밀번호</span>
              <input
                type="password"
                value={guestPassword}
                onChange={(event) => setGuestPassword(event.target.value)}
                placeholder="수정·삭제에 사용할 비밀번호"
                autoComplete="new-password"
              />
            </label>
          </div>
        </fieldset>}

        {user?.authenticated && <div className="readonly-field">작성자: {user.nickname} 님</div>}

        {errorMessage !== null && (
          <p className="form-error" role="alert" aria-live="polite">
            {errorMessage}
          </p>
        )}

        <div className="form-actions">
          <Link className="button secondary" to="/">
            취소
          </Link>
          <button className="button primary" type="submit" disabled={isSubmitting}>
            {isSubmitting ? "작성 중..." : "작성하기"}
          </button>
        </div>
      </form>
    </main>
  );
}

export default PostCreatePage;
