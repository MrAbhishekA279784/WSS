import { getAuthenticatedUser } from '@/lib/auth';
import { updateProfile } from '@/lib/services';
import { NextRequest, NextResponse } from 'next/server';

export async function PATCH(req: NextRequest) {
  const user = await getAuthenticatedUser();
  if (!user) {
    return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
  }
  try {
    const body = await req.json();
    const res = updateProfile(user.id, {
      fullName: String(body?.fullName ?? ''),
      phone: typeof body?.phone === 'string' ? body.phone : '',
    });
    if (!res.ok || !res.data) {
      return NextResponse.json({ error: res.error ?? 'Could not update profile.' }, { status: res.statusCode ?? 400 });
    }
    return NextResponse.json({ user: res.data });
  } catch {
    return NextResponse.json({ error: 'Invalid request body.' }, { status: 400 });
  }
}
