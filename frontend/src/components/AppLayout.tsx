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
        <Link className="brand" to="/">
          <span className="brand-mark" aria-hidden="true" />
          Spring Board
        </Link>
        <nav className="site-nav" aria-label="주요 메뉴">
          <NavLink to="/" end>게시글</NavLink>
          <NavLink to="/posts/new">글쓰기</NavLink>
          {!isLoading && user?.authenticated ? (
            <>
              <NavLink to="/chat">채팅</NavLink>
              {user.admin && <NavLink to="/admin">관리자</NavLink>}
              <NavLink to="/mypage">{user.nickname} 님</NavLink>
              <button className="link-button" type="button" onClick={() => void handleLogout()}>
                로그아웃
              </button>
            </>
          ) : (
            !isLoading && <NavLink to="/login">로그인</NavLink>
          )}
        </nav>
      </header>
      <Outlet />
      <footer className="site-footer">
        <span>Spring Board Project</span>
        <span>Spring Boot · React · TypeScript · JSON API</span>
      </footer>
    </div>
  );
}
