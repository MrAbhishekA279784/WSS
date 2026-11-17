import { parseJsonBody } from '@/lib/api-helpers';
import { getAuthenticatedUser } from '@/lib/auth';
import { changePassword } from '@/lib/services';
import { NextRequest, NextResponse } from 'next/server';

export async function POST(req: NextRequest) {
  const user = await getAuthenticatedUser();
  if (!user) {
    return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
  }
  const parsed = await parseJsonBody(req);
  if (!parsed.ok) {
    return parsed.response;
  }
  const { body } = parsed;

  try {
    const res = changePassword(user.id, {
      currentPassword: String(body.currentPassword ?? ''),
      newPassword: String(body.newPassword ?? ''),
      confirmPassword: String(body.confirmPassword ?? ''),
    });
    if (!res.ok || !res.data) {
      return NextResponse.json(
        { error: res.error ?? 'Could not change password.' },
        { status: res.statusCode ?? 400 }
      );
    }
    return NextResponse.json(res.data);
  } catch (err) {
    console.error('[api/profile/password] Unexpected error:', err);
    return NextResponse.json(
      { error: 'Unable to change password due to a server error.' },
      { status: 500 }
    );
  }
}
