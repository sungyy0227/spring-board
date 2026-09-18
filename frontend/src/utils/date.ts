export function formatDate(value: string, withYear = false) {
  const date = new Date(value);
  return new Intl.DateTimeFormat("ko-KR", {
    ...(withYear ? { year: "numeric" } : {}),
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
    hour12: false,
  }).format(date);
}
