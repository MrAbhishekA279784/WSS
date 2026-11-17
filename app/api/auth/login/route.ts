import { parseJsonBody } from '@/lib/api-helpers';
import { SESSION_COOKIE_NAME, sessionCookieOptions } from '@/lib/auth';
import { getWorkspaceBundle, loginUser } from '@/lib/services';
import { NextRequest, NextResponse } from 'next/server';

export async function POST(req: NextRequest) {
  const parsed = await parseJsonBody(req);
  if (!parsed.ok) {
    return parsed.response;
  }
  const { body } = parsed;

  try {
    const res = loginUser({
      email: String(body.email ?? ''),
      password: String(body.password ?? ''),
    });

    if (!res.ok || !res.data) {
      return NextResponse.json(
        { error: res.error ?? 'Login failed.' },
        { status: res.statusCode ?? 401 }
      );
    }

    const bundleRes = getWorkspaceBundle(res.data.user.id);
    const response = NextResponse.json(
      {
        user: res.data.user,
        token: res.data.token,
        bundle: bundleRes.ok && bundleRes.data ? bundleRes.data : null,
      },
      { status: 200 }
    );
    response.cookies.set(SESSION_COOKIE_NAME, res.data.token, sessionCookieOptions(req));
    return response;
  } catch (err) {
    console.error('[api/auth/login] Unexpected error:', err);
    return NextResponse.json(
      { error: 'Unable to sign in due to a server error. Please try again.' },
      { status: 500 }
    );
  }
}
