import { getAuthenticatedUser } from '@/lib/auth';
import { cancelSOS, deleteSOSRecord } from '@/lib/services';
import { NextRequest, NextResponse } from 'next/server';

export async function PATCH(
  _req: NextRequest,
  context: { params: Promise<{ id: string }> }
) {
  const user = await getAuthenticatedUser();
  if (!user) {
    return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
  }
  const { id } = await context.params;
  const sosId = Number(id);
  if (!Number.isInteger(sosId) || sosId <= 0) {
    return NextResponse.json({ error: 'Invalid SOS ID.' }, { status: 400 });
  }
  const res = cancelSOS(user.id, sosId);
  if (!res.ok || !res.data) {
    return NextResponse.json({ error: res.error ?? 'Could not cancel SOS.' }, { status: res.statusCode ?? 404 });
  }
  return NextResponse.json({ sos: res.data });
}

export async function DELETE(
  _req: NextRequest,
  context: { params: Promise<{ id: string }> }
) {
  const user = await getAuthenticatedUser();
  if (!user) {
    return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
  }
  const { id } = await context.params;
  const sosId = Number(id);
  if (!Number.isInteger(sosId) || sosId <= 0) {
    return NextResponse.json({ error: 'Invalid SOS ID.' }, { status: 400 });
  }
  const res = deleteSOSRecord(user.id, sosId);
  if (!res.ok) {
    return NextResponse.json({ error: res.error ?? 'Not found.' }, { status: res.statusCode ?? 404 });
  }
  return NextResponse.json({ ok: true });
}
