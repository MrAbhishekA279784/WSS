import AppShell from '@/components/AppShell';
import { getAuthenticatedUser } from '@/lib/auth';
import { getWorkspaceBundle } from '@/lib/services';

export const dynamic = 'force-dynamic';

export default async function HomePage({
  searchParams,
}: {
  searchParams: Promise<{ autostart?: string }>;
}) {
  const params = await searchParams;
  const skipSplash = params?.autostart === '1';

  const user = await getAuthenticatedUser();
  const bundleRes = user ? getWorkspaceBundle(user.id) : null;
  const initialBundle = bundleRes?.ok && bundleRes.data ? bundleRes.data : null;

  return <AppShell initialBundle={initialBundle} skipSplash={skipSplash} />;
}
