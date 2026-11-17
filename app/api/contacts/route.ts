import { getAuthenticatedUser } from '@/lib/auth';
import { createContact, listContacts } from '@/lib/services';
import { NextRequest, NextResponse } from 'next/server';

export const dynamic = 'force-dynamic';

export async function GET() {
  const user = await getAuthenticatedUser();
  if (!user) {
    return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
  }
  return NextResponse.json({ contacts: listContacts(user.id) });
}

export async function POST(req: NextRequest) {
  const user = await getAuthenticatedUser();
  if (!user) {
    return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
  }
  try {
    const body = await req.json();
    const res = createContact(user.id, {
      name: String(body?.name ?? ''),
      phone: String(body?.phone ?? ''),
      relationship: typeof body?.relationship === 'string' ? body.relationship : '',
    });
    if (!res.ok || !res.data) {
      return NextResponse.json({ error: res.error ?? 'Could not add contact.' }, { status: res.statusCode ?? 400 });
    }
    return NextResponse.json({ contact: res.data }, { status: 201 });
  } catch {
    return NextResponse.json({ error: 'Invalid request body.' }, { status: 400 });
  }
}
