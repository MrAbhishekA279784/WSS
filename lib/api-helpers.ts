import { NextRequest, NextResponse } from 'next/server';

export async function parseJsonBody(
  req: NextRequest
): Promise<{ ok: true; body: Record<string, unknown> } | { ok: false; response: NextResponse }> {
  try {
    const parsed = await req.json();
    if (!parsed || typeof parsed !== 'object' || Array.isArray(parsed)) {
      return {
        ok: false,
        response: NextResponse.json({ error: 'Invalid request body.' }, { status: 400 }),
      };
    }
    return { ok: true, body: parsed as Record<string, unknown> };
  } catch {
    return {
      ok: false,
      response: NextResponse.json({ error: 'Invalid request body.' }, { status: 400 }),
    };
  }
}
