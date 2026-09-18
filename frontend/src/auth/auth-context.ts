import { createContext } from "react";
import type { CurrentUser } from "../api/auth";

export interface AuthContextValue {
  user: CurrentUser | null;
  isLoading: boolean;
  refreshUser(): Promise<void>;
}

export const AuthContext = createContext<AuthContextValue | null>(null);
