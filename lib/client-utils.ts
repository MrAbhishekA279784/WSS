import type { LocationMode } from './types';

export function buildClientGoogleMapsUrl(lat: number, lng: number): string {
  return `https://www.google.com/maps?q=${lat.toFixed(6)},${lng.toFixed(6)}`;
}

export function formatClientCoordinates(lat: number, lng: number): string {
  const latDir = lat >= 0 ? 'N' : 'S';
  const lngDir = lng >= 0 ? 'E' : 'W';
  return `${Math.abs(lat).toFixed(4)}° ${latDir}, ${Math.abs(lng).toFixed(4)}° ${lngDir}`;
}

const MONTH_NAMES = [
  'Jan',
  'Feb',
  'Mar',
  'Apr',
  'May',
  'Jun',
  'Jul',
  'Aug',
  'Sep',
  'Oct',
  'Nov',
  'Dec',
] as const;

export function formatDisplayDateTime(isoOrSqliteDate: string | null | undefined): string {
  if (!isoOrSqliteDate) {
    return '—';
  }
  // SQLite datetime('now') produces 'YYYY-MM-DD HH:MM:SS' in UTC
  const normalized = isoOrSqliteDate.includes('T')
    ? (isoOrSqliteDate.endsWith('Z') ? isoOrSqliteDate : `${isoOrSqliteDate}Z`)
    : `${isoOrSqliteDate.replace(' ', 'T')}Z`;
  const parsed = new Date(normalized);
  if (Number.isNaN(parsed.getTime())) {
    return isoOrSqliteDate;
  }
  const month = MONTH_NAMES[parsed.getUTCMonth()];
  const day = String(parsed.getUTCDate()).padStart(2, '0');
  const year = parsed.getUTCFullYear();
  let hours = parsed.getUTCHours();
  const ampm = hours >= 12 ? 'PM' : 'AM';
  hours = hours % 12;
  hours = hours === 0 ? 12 : hours;
  const strHours = String(hours).padStart(2, '0');
  const minutes = String(parsed.getUTCMinutes()).padStart(2, '0');
  return `${month} ${day}, ${year}, ${strHours}:${minutes} ${ampm}`;
}

export function buildSmsHref(
  phone: string,
  senderName: string,
  lat: number,
  lng: number,
  label: string,
  mode: LocationMode
): string {
  const mapUrl = buildClientGoogleMapsUrl(lat, lng);
  const body = `EMERGENCY SOS from ${senderName}. Location (${mode}): ${label} (${formatClientCoordinates(lat, lng)}). Map: ${mapUrl}`;
  return `sms:${encodeURIComponent(phone)}?body=${encodeURIComponent(body)}`;
}

const SESSION_TOKEN_STORAGE_KEY = 'wss_session_token';

export function getStoredSessionToken(): string | null {
  if (typeof window === 'undefined') {
    return null;
  }
  try {
    return window.sessionStorage.getItem(SESSION_TOKEN_STORAGE_KEY);
  } catch {
    return null;
  }
}

export function storeSessionToken(token: string | undefined | null): void {
  if (typeof window === 'undefined' || !token) {
    return;
  }
  try {
    window.sessionStorage.setItem(SESSION_TOKEN_STORAGE_KEY, token);
  } catch {
    // ignore restricted storage contexts
  }
}

export function clearSessionToken(): void {
  if (typeof window === 'undefined') {
    return;
  }
  try {
    window.sessionStorage.removeItem(SESSION_TOKEN_STORAGE_KEY);
  } catch {
    // ignore restricted storage contexts
  }
}

export async function apiFetch(input: RequestInfo | URL, init?: RequestInit): Promise<Response> {
  const headers = new Headers(init?.headers);
  const token = getStoredSessionToken();
  if (token && !headers.has('Authorization')) {
    headers.set('Authorization', `Bearer ${token}`);
  }
  return fetch(input, {
    ...init,
    credentials: init?.credentials ?? 'include',
    headers,
  });
}
