import { useEffect, useState, type FormEvent } from "react";
import { Link, useSearchParams } from "react-router";
import { getPosts, type PostPageResponse } from "../api/posts";
import { formatDate } from "../utils/date";

export default function PostListPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const page = Math.max(Number(searchParams.get("page")) || 1, 1);
  const keyword = searchParams.get("keyword") ?? undefined;
  const type = searchParams.get("type") ?? "title_content";
  const [query, setQuery] = useState(keyword ?? "");
  const [searchType, setSearchType] = useState(type);
  const [data, setData] = useState<PostPageResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let active = true;
    getPosts({ page, type, keyword })
      .then((result) => { if (active) { setData(result); setError(null); } })
      .catch((reason: unknown) => active && setError(reason instanceof Error ? reason.message : "오류가 발생했습니다."));
    return () => { active = false; };
  }, [page, type, keyword]);

  function handleSearch(event: FormEvent) {
    event.preventDefault();
    setSearchParams({ page: "1", type: searchType, keyword: query });
  }

  const pageBlockStart = Math.floor((page - 1) / 10) * 10 + 1;
  const pageBlockEnd = data ? Math.min(pageBlockStart + 9, data.totalPages) : 0;
  const pages = pageBlockEnd >= pageBlockStart
    ? Array.from({ length: pageBlockEnd - pageBlockStart + 1 }, (_, index) => pageBlockStart + index)
    : [];

  function moveToPage(pageNumber: number) {
    setSearchParams({
      page: String(pageNumber),
      ...(keyword !== undefined ? { type, keyword } : {}),
    });
  }

  return (
    <main className="page home-page">
      <section className="board-section">
        <div className="section-heading">
          <div>
            <h1>자유 게시판</h1>
            {keyword && <p>'{keyword}' 검색 결과</p>}
          </div>
          <form className="search-form" onSubmit={handleSearch}>
            <select value={searchType} onChange={(event) => setSearchType(event.target.value)} aria-label="검색 범위">
              <option value="title_content">제목+내용</option>
              <option value="title">제목</option>
              <option value="content">내용</option>
              <option value="poster">작성자</option>
            </select>
            <input value={query} onChange={(event) => setQuery(event.target.value)} type="search" placeholder="두 글자 이상 입력하세요" />
            <button className="button primary" type="submit">검색</button>
          </form>
        </div>

        {error && <p className="alert error" role="alert">{error}</p>}
        {!data && !error ? <p className="status-message">게시글을 불러오는 중입니다.</p> : (
          <div className="table-wrap">
            <table className="data-table">
              <thead><tr><th>번호</th><th>제목</th><th>작성자</th><th>날짜</th><th>조회수</th></tr></thead>
              <tbody>
                {data?.content.length === 0 && <tr><td colSpan={5}>{keyword ? "검색 결과가 없습니다." : "아직 등록된 게시글이 없습니다."}</td></tr>}
                {data?.content.map((post) => (
                  <tr key={post.id}>
                    <td>{post.id}</td>
                    <td className="title-cell"><Link to={`/posts/${post.id}`}>{post.title}</Link></td>
                    <td>{post.poster}</td><td>{formatDate(post.createdAt)}</td><td>{post.viewCount}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        <div className="board-actions">
          <nav className="pagination" aria-label="게시글 페이지 이동">
            {pageBlockStart > 1 && <button type="button" onClick={() => moveToPage(pageBlockStart - 1)}>이전</button>}
            {pages.map((pageNumber) => (
              <button key={pageNumber} type="button" className={pageNumber === page ? "active" : ""}
                onClick={() => moveToPage(pageNumber)}>
                {pageNumber}
              </button>
            ))}
            {data && pageBlockEnd < data.totalPages && <button type="button" onClick={() => moveToPage(pageBlockEnd + 1)}>다음</button>}
          </nav>
          <Link className="button primary" to="/posts/new"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><path d="m16 3 5 5-12 12-6 1 1-6Z M14 5l5 5" /></svg>글쓰기</Link>
        </div>
      </section>
    </main>
  );
}
