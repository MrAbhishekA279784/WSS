import { SESSION_COOKIE_NAME, sessionCookieOptions } from '@/lib/auth';
import { getWorkspaceBundle, registerUser } from '@/lib/services';
import { NextRequest, NextResponse } from 'next/server';

export async function POST(req: NextRequest) {
  try {
    const body = await req.json();
    const res = registerUser({
      fullName: String(body?.fullName ?? ''),
      email: String(body?.email ?? ''),
      password: String(body?.password ?? ''),
      confirmPassword: String(body?.confirmPassword ?? ''),
      phone: typeof body?.phone === 'string' ? body.phone : '',
    });

    if (!res.ok || !res.data) {
      return NextResponse.json({ error: res.error ?? 'Registration failed.' }, { status: res.statusCode ?? 400 });
    }

    const bundleRes = getWorkspaceBundle(res.data.user.id);
    const response = NextResponse.json(
      {
        user: res.data.user,
        token: res.data.token,
        bundle: bundleRes.ok && bundleRes.data ? bundleRes.data : null,
      },
      { status: 201 }
    );
    response.cookies.set(SESSION_COOKIE_NAME, res.data.token, sessionCookieOptions(req));
    return response;
  } catch {
    return NextResponse.json({ error: 'Invalid request body.' }, { status: 400 });
  }
}
