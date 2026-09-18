import { Link } from "react-router";

export default function NotFoundPage() {
  return <main className="page narrow-page"><section className="card error-page"><p className="eyebrow">404</p><h1>페이지를 찾을 수 없습니다.</h1><p>주소를 다시 확인하거나 게시글 목록으로 돌아가 주세요.</p><Link className="button primary" to="/">게시글 목록</Link></section></main>;
}
