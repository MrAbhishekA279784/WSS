import { parseJsonBody } from '@/lib/api-helpers';
import { getAuthenticatedUser } from '@/lib/auth';
import { getWorkspaceBundle, saveLocationSnapshot } from '@/lib/services';
import type { LocationMode } from '@/lib/types';
import { NextRequest, NextResponse } from 'next/server';

export const dynamic = 'force-dynamic';

export async function GET() {
  const user = await getAuthenticatedUser();
  if (!user) {
    return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
  }
  const bundle = getWorkspaceBundle(user.id);
  return NextResponse.json({
    currentLocation: bundle.data?.currentLocation ?? null,
    locationHistory: bundle.data?.locationHistory ?? [],
  });
}

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
    const res = saveLocationSnapshot(user.id, {
      latitude: Number(body.latitude),
      longitude: Number(body.longitude),
      address: typeof body.address === 'string' ? body.address : undefined,
      locationType:
        body.locationType === 'GPS' || body.locationType === 'MANUAL' || body.locationType === 'SIMULATED'
          ? (body.locationType as LocationMode)
          : 'SIMULATED',
    });
    if (!res.ok || !res.data) {
      return NextResponse.json(
        { error: res.error ?? 'Failed to save location.' },
        { status: res.statusCode ?? 400 }
      );
    }
    return NextResponse.json({ location: res.data }, { status: 201 });
  } catch (err) {
    console.error('[api/location] Unexpected error:', err);
    return NextResponse.json(
      { error: 'Unable to save location due to a server error.' },
      { status: 500 }
    );
  }
}
