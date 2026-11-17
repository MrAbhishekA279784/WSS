import { getAuthenticatedUser } from '@/lib/auth';
import { deleteContact, updateContact } from '@/lib/services';
import { NextRequest, NextResponse } from 'next/server';

export async function PATCH(
  req: NextRequest,
  context: { params: Promise<{ id: string }> }
) {
  const user = await getAuthenticatedUser();
  if (!user) {
    return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
  }
  const { id } = await context.params;
  const contactId = Number(id);
  if (!Number.isInteger(contactId) || contactId <= 0) {
    return NextResponse.json({ error: 'Invalid contact ID.' }, { status: 400 });
  }
  try {
    const body = await req.json();
    const res = updateContact(user.id, contactId, {
      name: String(body?.name ?? ''),
      phone: String(body?.phone ?? ''),
      relationship: typeof body?.relationship === 'string' ? body.relationship : '',
    });
    if (!res.ok || !res.data) {
      return NextResponse.json({ error: res.error ?? 'Could not update contact.' }, { status: res.statusCode ?? 400 });
    }
    return NextResponse.json({ contact: res.data });
  } catch {
    return NextResponse.json({ error: 'Invalid request body.' }, { status: 400 });
  }
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
  const contactId = Number(id);
  if (!Number.isInteger(contactId) || contactId <= 0) {
    return NextResponse.json({ error: 'Invalid contact ID.' }, { status: 400 });
  }
  const res = deleteContact(user.id, contactId);
  if (!res.ok) {
    return NextResponse.json({ error: res.error ?? 'Not found.' }, { status: res.statusCode ?? 404 });
  }
  return NextResponse.json({ ok: true });
}
