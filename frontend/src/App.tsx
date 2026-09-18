import { Route, Routes } from "react-router";
import { lazy, Suspense } from "react";
import AppLayout from "./components/AppLayout";
import { RequireAdmin, RequireAuth } from "./components/ProtectedRoute";
import "./App.css";

const AdminMemberPage = lazy(() => import("./pages/AdminMemberPage"));
const AdminPage = lazy(() => import("./pages/AdminPage"));
const LoginPage = lazy(() => import("./pages/LoginPage"));
const MyPage = lazy(() => import("./pages/MyPage"));
const NotFoundPage = lazy(() => import("./pages/NotFoundPage"));
const PostCreatePage = lazy(() => import("./pages/PostCreatePage"));
const PostDetailPage = lazy(() => import("./pages/PostDetailPage"));
const PostEditPage = lazy(() => import("./pages/PostEditPage"));
const PostListPage = lazy(() => import("./pages/PostListPage"));
const SignupPage = lazy(() => import("./pages/SignupPage"));
const WithdrawPage = lazy(() => import("./pages/WithdrawPage"));

export default function App() {
  return (
    <Suspense fallback={<p className="status-message">화면을 불러오는 중입니다.</p>}>
    <Routes>
      <Route element={<AppLayout />}>
        <Route index element={<PostListPage />} />
        <Route path="posts" element={<PostListPage />} />
        <Route path="posts/new" element={<PostCreatePage />} />
        <Route path="posts/:postId" element={<PostDetailPage />} />
        <Route path="posts/:postId/edit" element={<PostEditPage />} />
        <Route path="login" element={<LoginPage />} />
        <Route path="signup" element={<SignupPage />} />

        <Route element={<RequireAuth />}>
          <Route path="mypage" element={<MyPage />} />
          <Route path="mypage/withdraw" element={<WithdrawPage />} />
        </Route>

        <Route element={<RequireAdmin />}>
          <Route path="admin" element={<AdminPage />} />
          <Route path="admin/members/:memberId" element={<AdminMemberPage />} />
        </Route>

        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
    </Suspense>
  );
}
