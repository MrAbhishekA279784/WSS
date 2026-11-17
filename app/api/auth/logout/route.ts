import { clearSessionCookieOptions, getSessionToken, SESSION_COOKIE_NAME } from '@/lib/auth';
import { deleteSession } from '@/lib/services';
import { NextRequest, NextResponse } from 'next/server';

export async function POST(req: NextRequest) {
  const token = await getSessionToken();
  deleteSession(token);

  const response = NextResponse.json({ ok: true });
  response.cookies.set(SESSION_COOKIE_NAME, '', clearSessionCookieOptions(req));
  return response;
}
