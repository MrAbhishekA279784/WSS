import { getAuthenticatedUser } from '@/lib/auth';
import { changePassword } from '@/lib/services';
import { NextRequest, NextResponse } from 'next/server';

export async function POST(req: NextRequest) {
  const user = await getAuthenticatedUser();
  if (!user) {
    return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
  }
  try {
    const body = await req.json();
    const res = changePassword(user.id, {
      currentPassword: String(body?.currentPassword ?? ''),
      newPassword: String(body?.newPassword ?? ''),
      confirmPassword: String(body?.confirmPassword ?? ''),
    });
    if (!res.ok || !res.data) {
      return NextResponse.json({ error: res.error ?? 'Could not change password.' }, { status: res.statusCode ?? 400 });
    }
    return NextResponse.json(res.data);
  } catch {
    return NextResponse.json({ error: 'Invalid request body.' }, { status: 400 });
  }
}
