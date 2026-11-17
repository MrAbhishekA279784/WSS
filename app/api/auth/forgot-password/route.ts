import { parseJsonBody } from '@/lib/api-helpers';
import { resetPassword } from '@/lib/services';
import { NextRequest, NextResponse } from 'next/server';

export async function POST(req: NextRequest) {
  const parsed = await parseJsonBody(req);
  if (!parsed.ok) {
    return parsed.response;
  }
  const { body } = parsed;

  try {
    const res = resetPassword({
      email: String(body.email ?? ''),
      fullName: String(body.fullName ?? ''),
      newPassword: String(body.newPassword ?? ''),
      confirmPassword: String(body.confirmPassword ?? ''),
    });
    if (!res.ok || !res.data) {
      return NextResponse.json(
        { error: res.error ?? 'Reset failed.' },
        { status: res.statusCode ?? 400 }
      );
    }
    return NextResponse.json(res.data);
  } catch (err) {
    console.error('[api/auth/forgot-password] Unexpected error:', err);
    return NextResponse.json(
      { error: 'Unable to reset password due to a server error.' },
      { status: 500 }
    );
  }
}
