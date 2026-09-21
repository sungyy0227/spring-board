import { Link, NavLink, Outlet, useNavigate } from "react-router";
import { logout } from "../api/auth";
import { useAuth } from "../auth/useAuth";

export default function AppLayout() {
  const navigate = useNavigate();
  const { user, isLoading, refreshUser } = useAuth();

  async function handleLogout() {
    await logout();
    await refreshUser();
    navigate("/");
  }

  return (
    <div className="app-shell">
      <header className="site-header">
        <Link className="brand" to="/" aria-label="메인으로 이동">
          <svg className="home-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
            <path d="m3 10 9-7 9 7v10a1 1 0 0 1-1 1h-5v-7H9v7H4a1 1 0 0 1-1-1Z" />
          </svg>
        </Link>
        <nav className="site-nav" aria-label="주요 메뉴">
          <NavLink to="/" end>게시판</NavLink>
          {!isLoading && user?.authenticated ? (
            <>
              <NavLink to="/chat">채팅</NavLink>
              {user.admin && <NavLink to="/admin">관리자</NavLink>}
              <NavLink className="account-link" to="/mypage">{user.nickname} 님</NavLink>
              <button className="link-button" type="button" onClick={() => void handleLogout()}>
                로그아웃
              </button>
            </>
          ) : (
            !isLoading && <NavLink className="account-link" to="/login">로그인</NavLink>
          )}
        </nav>
      </header>
      <Outlet />
      <footer className="site-footer">
        <span>Spring Board Project</span>
        <div className="footer-links">
          <a href="mailto:sungyy0227@naver.com">문의하기</a>
          <span aria-hidden="true">|</span>
          <a className="github-link" href="https://github.com/sungyy0227/spring-board" target="_blank" rel="noopener noreferrer" aria-label="GitHub 프로젝트 열기">
            <img src="/github-mark.svg" width="24" height="24" alt="" aria-hidden="true" />
          </a>
        </div>
      </footer>
    </div>
  );
}
