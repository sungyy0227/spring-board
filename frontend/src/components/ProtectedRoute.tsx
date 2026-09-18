import { Navigate, Outlet } from "react-router";
import { useAuth } from "../auth/useAuth";

export function RequireAuth() {
  const { user, isLoading } = useAuth();
  if (isLoading) return <p className="status-message">로그인 정보를 확인하고 있습니다.</p>;
  if (!user?.authenticated) return <Navigate to="/login" replace />;
  return <Outlet />;
}

export function RequireAdmin() {
  const { user, isLoading } = useAuth();
  if (isLoading) return <p className="status-message">권한을 확인하고 있습니다.</p>;
  if (!user?.authenticated) return <Navigate to="/login" replace />;
  if (!user.admin) return <Navigate to="/" replace />;
  return <Outlet />;
}
