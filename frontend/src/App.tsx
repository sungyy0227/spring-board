import { Link, Route, Routes } from "react-router";
import { useEffect, useState } from "react";

interface PostSummary {
    id: number;
    title: string;
    poster: string;
    viewCount: number;
    createdAt: string;
}

interface PostPageResponse {
    content: PostSummary[];
    currentPage: number;
    totalPages: number;
    totalElements: number;
}

function PostListPage() {
    const [posts, setPosts] = useState<PostSummary[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [errorMessage, setErrorMessage] = useState<string | null>(null);

    useEffect(() => {
        async function loadPosts() {
            try {
                const response = await fetch("/api/v1/posts?page=1", {
                    credentials: "include",
                });

                if (!response.ok) {
                    throw new Error(`게시글 요청 실패: ${response.status}`);
                }

                const data: PostPageResponse = await response.json();

                setPosts(data.content);
            } catch (error) {
                if (error instanceof Error) {
                    setErrorMessage(error.message);
                } else {
                    setErrorMessage("알 수 없는 오류가 발생했습니다.");
                }
            } finally {
                setIsLoading(false);
            }
        }

        void loadPosts();
    }, []);

    if (isLoading) {
        return <p>게시글을 불러오는 중입니다.</p>;
    }

    if (errorMessage !== null) {
        return <p>{errorMessage}</p>;
    }

    return (
        <main>
            <h1>게시판</h1>

            {posts.length === 0 ? (
                <p>등록된 게시글이 없습니다.</p>
            ) : (
                <ul>
                    {posts.map((post) => (
                        <li key={post.id}>
                            <Link to={`/posts/${post.id}`}>
                                {post.title}
                            </Link>

                            <span> 작성자: {post.poster}</span>
                            <span> 조회수: {post.viewCount}</span>
                        </li>
                    ))}
                </ul>
            )}
        </main>
    );
}


function PostDetailPage() {
  return <h1>게시글 상세</h1>;
}

function App() {
  return (
      <>
        <nav>
          <Link to="/">홈</Link>
          {" | "}
          <Link to="/posts">게시글</Link>
        </nav>

        <Routes>
            <Route path="/" element={<PostListPage />} />
            <Route path="/posts/:postId" element={<PostDetailPage />} />
        </Routes>
      </>
  );
}

export default App;