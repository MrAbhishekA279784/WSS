import { cookies, headers } from 'next/headers';
import type { NextRequest } from 'next/server';
import { getUserBySessionToken } from './services';
import type { PublicUser } from './types';

export const SESSION_COOKIE_NAME = 'wss_session';

export async function getSessionToken(): Promise<string | undefined> {
  const cookieStore = await cookies();
  const cookieToken = cookieStore.get(SESSION_COOKIE_NAME)?.value;
  if (cookieToken) {
    return cookieToken;
  }

  const headerStore = await headers();
  const authHeader = headerStore.get('authorization');
  if (authHeader && authHeader.toLowerCase().startsWith('bearer ')) {
    const bearer = authHeader.slice(7).trim();
    return bearer || undefined;
  }

  return undefined;
}

export async function getAuthenticatedUser(): Promise<PublicUser | null> {
  const token = await getSessionToken();
  return getUserBySessionToken(token);
}

function isHttpsRequest(req?: NextRequest): boolean {
  if (req) {
    const forwardedProto = req.headers.get('x-forwarded-proto')?.split(',')[0]?.trim().toLowerCase();
    if (forwardedProto) {
      return forwardedProto === 'https';
    }
    return req.nextUrl.protocol === 'https:';
  }
  return process.env.NODE_ENV === 'production' && process.env.ALLOW_INSECURE_COOKIE !== 'true';
}

export function sessionCookieOptions(req?: NextRequest) {
  const https = isHttpsRequest(req);
  if (https) {
    return {
      httpOnly: true,
      sameSite: 'none' as const,
      secure: true,
      partitioned: true,
      path: '/',
      maxAge: 7 * 24 * 60 * 60,
    };
  }
  return {
    httpOnly: true,
    sameSite: 'lax' as const,
    secure: false,
    path: '/',
    maxAge: 7 * 24 * 60 * 60,
  };
}

export function clearSessionCookieOptions(req?: NextRequest) {
  const https = isHttpsRequest(req);
  if (https) {
    return {
      httpOnly: true,
      sameSite: 'none' as const,
      secure: true,
      partitioned: true,
      path: '/',
      maxAge: 0,
    };
  }
  return {
    httpOnly: true,
    sameSite: 'lax' as const,
    secure: false,
    path: '/',
    maxAge: 0,
  };
}
