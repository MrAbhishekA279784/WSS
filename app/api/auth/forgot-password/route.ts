import { resetPassword } from '@/lib/services';
import { NextRequest, NextResponse } from 'next/server';

export async function POST(req: NextRequest) {
  try {
    const body = await req.json();
    const res = resetPassword({
      email: String(body?.email ?? ''),
      fullName: String(body?.fullName ?? ''),
      newPassword: String(body?.newPassword ?? ''),
      confirmPassword: String(body?.confirmPassword ?? ''),
    });
    if (!res.ok || !res.data) {
      return NextResponse.json({ error: res.error ?? 'Reset failed.' }, { status: res.statusCode ?? 400 });
    }
    return NextResponse.json(res.data);
  } catch {
    return NextResponse.json({ error: 'Invalid request body.' }, { status: 400 });
  }
}
