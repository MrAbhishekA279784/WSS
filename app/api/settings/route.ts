import { parseJsonBody } from '@/lib/api-helpers';
import { getAuthenticatedUser } from '@/lib/auth';
import { updateSettings } from '@/lib/services';
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
    const res = updateSettings(user.id, {
      confirmBeforeSos:
        typeof body.confirmBeforeSos === 'boolean' ? body.confirmBeforeSos : undefined,
      includeLocationInSos:
        typeof body.includeLocationInSos === 'boolean' ? body.includeLocationInSos : undefined,
      simulatedLat:
        typeof body.simulatedLat === 'number' ? body.simulatedLat : undefined,
      simulatedLng:
        typeof body.simulatedLng === 'number' ? body.simulatedLng : undefined,
      simulatedAddress:
        typeof body.simulatedAddress === 'string' ? body.simulatedAddress : undefined,
    });
    if (!res.ok || !res.data) {
      return NextResponse.json(
        { error: res.error ?? 'Could not update settings.' },
        { status: res.statusCode ?? 400 }
      );
    }
    return NextResponse.json({ settings: res.data });
  } catch (err) {
    console.error('[api/settings] Unexpected error:', err);
    return NextResponse.json(
      { error: 'Unable to update settings due to a server error.' },
      { status: 500 }
    );
  }
}
