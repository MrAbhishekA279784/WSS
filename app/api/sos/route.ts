import { getAuthenticatedUser } from '@/lib/auth';
import { getWorkspaceBundle, triggerSOS } from '@/lib/services';
import type { LocationMode } from '@/lib/types';
import { NextRequest, NextResponse } from 'next/server';

export const dynamic = 'force-dynamic';

export async function GET() {
  const user = await getAuthenticatedUser();
  if (!user) {
    return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
  }
  const bundle = getWorkspaceBundle(user.id);
  return NextResponse.json({ sosHistory: bundle.data?.sosHistory ?? [] });
}

export async function POST(req: NextRequest) {
  const user = await getAuthenticatedUser();
  if (!user) {
    return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
  }
  try {
    const body = await req.json().catch(() => ({}));
    const res = await triggerSOS(user.id, {
      latitude: typeof body?.latitude === 'number' ? body.latitude : undefined,
      longitude: typeof body?.longitude === 'number' ? body.longitude : undefined,
      address: typeof body?.address === 'string' ? body.address : undefined,
      locationType:
        body?.locationType === 'GPS' || body?.locationType === 'MANUAL' || body?.locationType === 'SIMULATED'
          ? (body.locationType as LocationMode)
          : undefined,
    });
    if (!res.ok || !res.data) {
      return NextResponse.json({ error: res.error ?? 'Failed to trigger SOS.' }, { status: res.statusCode ?? 400 });
    }
    return NextResponse.json({ sos: res.data }, { status: 201 });
  } catch {
    return NextResponse.json({ error: 'Invalid request body.' }, { status: 400 });
  }
}
