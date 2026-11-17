import { getAuthenticatedUser } from '@/lib/auth';
import { getWorkspaceBundle } from '@/lib/services';
import { NextResponse } from 'next/server';

export const dynamic = 'force-dynamic';

export async function GET() {
  const user = await getAuthenticatedUser();
  if (!user) {
    return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
  }
  const bundle = getWorkspaceBundle(user.id);
  if (!bundle.ok || !bundle.data) {
    return NextResponse.json({ error: bundle.error ?? 'User not found.' }, { status: bundle.statusCode ?? 404 });
  }
  return NextResponse.json(bundle.data);
}
