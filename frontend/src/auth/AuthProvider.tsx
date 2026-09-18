import { useCallback, useEffect, useMemo, useState, type ReactNode } from "react";
import { getCurrentUser, type CurrentUser } from "../api/auth";
import { AuthContext } from "./auth-context";
import { setUnauthorizedHandler } from "../api/apiClient";

export default function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<CurrentUser | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  const refreshUser = useCallback(async () => {
    try {
      setUser(await getCurrentUser());
    } catch {
      setUser({ authenticated: false, id: null, loginId: null, nickname: null, role: null, admin: false });
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    let active = true;
    getCurrentUser()
      .then((currentUser) => { if (active) setUser(currentUser); })
      .catch(() => {
        if (active) setUser({ authenticated: false, id: null, loginId: null, nickname: null, role: null, admin: false });
      })
      .finally(() => { if (active) setIsLoading(false); });
    return () => { active = false; };
  }, []);

  useEffect(() => {
    setUnauthorizedHandler(() => {
      setUser({
        authenticated: false,
        id: null,
        loginId: null,
        nickname: null,
        role: null,
        admin: false,
      });
    });

    return () => {
      setUnauthorizedHandler(null);
    };
  }, []);

  const value = useMemo(() => ({ user, isLoading, refreshUser }), [user, isLoading, refreshUser]);
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
