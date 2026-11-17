import { parseJsonBody } from '@/lib/api-helpers';
import { getAuthenticatedUser } from '@/lib/auth';
import { updateProfile } from '@/lib/services';
import { NextRequest, NextResponse } from 'next/server';

export async function PATCH(req: NextRequest) {
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
    const res = updateProfile(user.id, {
      fullName: String(body.fullName ?? ''),
      phone: typeof body.phone === 'string' ? body.phone : '',
    });
    if (!res.ok || !res.data) {
      return NextResponse.json(
        { error: res.error ?? 'Could not update profile.' },
        { status: res.statusCode ?? 400 }
      );
    }
    return NextResponse.json({ user: res.data });
  } catch (err) {
    console.error('[api/profile] Unexpected error:', err);
    return NextResponse.json(
      { error: 'Unable to update profile due to a server error.' },
      { status: 500 }
    );
  }
}
