'use client';

import { useState } from 'react';
import { motion } from 'motion/react';
import {
  Check,
  Crosshair,
  ExternalLink,
  MapPin,
  Navigation,
  Share2,
} from 'lucide-react';
import TiltCard from '@/components/TiltCard';
import { triggerHaptic } from '@/lib/haptics';
import {
  apiFetch,
  buildClientGoogleMapsUrl,
  formatClientCoordinates,
  formatDisplayDateTime,
  safeJson,
} from '@/lib/client-utils';
import type { LocationMode, WorkspaceBundle } from '@/lib/types';

interface LiveLocationViewProps {
  bundle: WorkspaceBundle;
  onRefresh: () => Promise<void>;
}

export default function LiveLocationView({
  bundle,
  onRefresh,
}: LiveLocationViewProps) {
  const [latInput, setLatInput] = useState(String(bundle.currentLocation.latitude));
  const [lngInput, setLngInput] = useState(String(bundle.currentLocation.longitude));
  const [addrInput, setAddrInput] = useState(bundle.currentLocation.address);
  const [saving, setSaving] = useState(false);
  const [gpsLoading, setGpsLoading] = useState(false);
  const [statusMessage, setStatusMessage] = useState('');
  const [errorMessage, setErrorMessage] = useState('');
  const [copiedLink, setCopiedLink] = useState(false);

  const mapsUrl = buildClientGoogleMapsUrl(
    bundle.currentLocation.latitude,
    bundle.currentLocation.longitude
  );

  const persistLocation = async (
    latitude: number,
    longitude: number,
    address: string,
    locationType: LocationMode
  ) => {
    setErrorMessage('');
    setStatusMessage('');
    setSaving(true);
    try {
      const res = await apiFetch('/api/location', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ latitude, longitude, address, locationType }),
      });
      const data = await safeJson(res);
      if (!res.ok) {
        setErrorMessage(data.error ?? 'Could not save location.');
        return false;
      }
      setLatInput(String(latitude));
      setLngInput(String(longitude));
      setAddrInput(address);
      await onRefresh();
      return true;
    } catch {
      setErrorMessage('Network error while saving location.');
      return false;
    } finally {
      setSaving(false);
    }
  };

  const handleUseBrowserGps = () => {
    setErrorMessage('');
    setStatusMessage('');
    if (typeof navigator === 'undefined' || !navigator.geolocation) {
      setErrorMessage('Browser geolocation is unavailable in this environment. Use simulated coordinates below.');
      return;
    }
    setGpsLoading(true);
    navigator.geolocation.getCurrentPosition(
      async (pos) => {
        const lat = Number(pos.coords.latitude.toFixed(6));
        const lng = Number(pos.coords.longitude.toFixed(6));
        const ok = await persistLocation(lat, lng, 'Browser Geolocation Fix', 'GPS');
        if (ok) {
          triggerHaptic('success');
          setStatusMessage('Live GPS coordinates captured and saved to SQLite.');
        } else {
          triggerHaptic('error');
        }
        setGpsLoading(false);
      },
      (err) => {
        triggerHaptic('error');
        setGpsLoading(false);
        setErrorMessage(
          `Geolocation unavailable (${err.message || 'permission denied'}). Simulated coordinates remain active.`
        );
      },
      { enableHighAccuracy: true, timeout: 6000 }
    );
  };

  const handleManualSave = async (e: React.FormEvent) => {
    e.preventDefault();
    const lat = Number(latInput);
    const lng = Number(lngInput);
    if (!Number.isFinite(lat) || !Number.isFinite(lng)) {
      triggerHaptic('error');
      setErrorMessage('Please enter valid numeric latitude and longitude.');
      return;
    }
    const ok = await persistLocation(
      lat,
      lng,
      addrInput.trim() || 'Manual Simulated Coordinates',
      'MANUAL'
    );
    if (ok) {
      triggerHaptic('success');
      setStatusMessage('Simulated coordinates updated and saved to SQLite.');
    } else {
      triggerHaptic('error');
    }
  };

  const handleShareLocation = async () => {
    triggerHaptic('confirm');
    await persistLocation(
      bundle.currentLocation.latitude,
      bundle.currentLocation.longitude,
      bundle.currentLocation.address,
      bundle.currentLocation.locationType
    );
    try {
      await navigator.clipboard.writeText(mapsUrl);
      setCopiedLink(true);
      setStatusMessage('Shareable Google Maps link copied to clipboard.');
      setTimeout(() => setCopiedLink(false), 2500);
    } catch {
      setStatusMessage(`Shareable Google Maps link ready: ${mapsUrl}`);
    }
  };

  return (
    <div className="grid grid-cols-1 gap-6 lg:grid-cols-[1.3fr_1fr]">
      {/* Left Interactive Dark Map Canvas + History */}
      <div className="space-y-6">
        <div className="relative overflow-hidden rounded-2xl border border-[#393333] bg-[#141414]">
          {/* Mode Readout */}
          <div className="absolute top-4 right-4 z-10 rounded-lg border border-[#393333] bg-[#080808]/90 px-3 py-1.5 text-xs text-[#A5A0A0]">
            Location · {bundle.currentLocation.locationType}
          </div>

          {/* Dark Vector Street Grid SVG matching Reference Screen 05 */}
          <div className="relative h-[360px] w-full sm:h-[430px]">
            <svg
              viewBox="0 0 800 500"
              preserveAspectRatio="xMidYMid slice"
              className="h-full w-full"
              aria-label="Dark vector location map"
            >
              <rect width="800" height="500" fill="#141414" />
              <g fill="#1B1B1B">
                <rect x="35" y="25" width="140" height="90" rx="14" />
                <rect x="210" y="20" width="165" height="95" rx="14" />
                <rect x="425" y="30" width="170" height="90" rx="14" />
                <rect x="630" y="25" width="140" height="95" rx="14" />
                <rect x="40" y="155" width="150" height="120" rx="14" />
                <rect x="420" y="160" width="160" height="115" rx="14" />
                <rect x="615" y="155" width="150" height="120" rx="14" />
                <rect x="75" y="315" width="175" height="110" rx="14" />
                <rect x="430" y="320" width="175" height="105" rx="14" />
              </g>
              <g fill="none" stroke="#2D2D2D" strokeWidth="8" strokeLinecap="round">
                <path d="M -20 130 C 210 175, 510 75, 820 120" />
                <path d="M 310 -20 C 340 160, 275 350, 405 520" />
                <path d="M -20 355 C 260 315, 540 400, 820 280" />
              </g>
              <g fill="none" stroke="#232323" strokeWidth="4">
                <line x1="150" y1="0" x2="210" y2="500" />
                <line x1="600" y1="0" x2="545" y2="500" />
              </g>
              {/* Center Radar Pulse Marker */}
              <circle cx="400" cy="240" r="64" fill="#E8A99D" fillOpacity="0.08" />
              <circle cx="400" cy="240" r="34" fill="#ECA99C" fillOpacity="0.16" stroke="#ECA99C" strokeOpacity="0.4" strokeWidth="1.5" />
              <circle cx="400" cy="240" r="10" fill="#F5B6A9" />
            </svg>

            {/* Map Bottom Action Controls */}
            <div className="absolute right-4 bottom-4 flex items-center gap-2.5">
              <button
                type="button"
                onClick={handleUseBrowserGps}
                disabled={gpsLoading}
                className="inline-flex h-10 items-center gap-2 rounded-full border border-[#393333] bg-[#1C1C1C] px-4 text-xs font-medium text-[#F5F2F0] transition-colors hover:border-[#ECA99C] hover:bg-[#252525] disabled:opacity-60 whitespace-nowrap shrink-0"
              >
                <Crosshair className="h-4 w-4 text-[#F5B6A9]" />
                {gpsLoading ? 'Locating...' : 'Use Browser GPS'}
              </button>
            </div>
          </div>

          {/* Current Location Footer Card */}
          <div className="border-t border-[#393333] bg-[#1C1C1C] p-5">
            <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
              <div className="flex items-start gap-3">
                <MapPin className="mt-0.5 h-5 w-5 shrink-0 text-[#F5B6A9]" />
                <div>
                  <p className="text-sm font-semibold text-[#F5F2F0]">
                    Current Location
                  </p>
                  <p className="mt-0.5 font-mono text-sm text-[#F5F2F0] tabular-nums">
                    {formatClientCoordinates(
                      bundle.currentLocation.latitude,
                      bundle.currentLocation.longitude
                    )}
                  </p>
                  <p className="mt-0.5 text-xs text-[#A5A0A0]">
                    {bundle.currentLocation.address}
                  </p>
                </div>
              </div>

              <div className="flex flex-wrap items-center gap-2.5">
                <button
                  type="button"
                  onClick={handleShareLocation}
                  disabled={saving}
                  className="inline-flex h-11 items-center gap-2 rounded-full bg-[#F5B6A9] px-5 text-xs font-semibold text-[#111111] transition-colors hover:bg-[#ECA99C] disabled:opacity-60 whitespace-nowrap shrink-0"
                >
                  {copiedLink ? (
                    <>
                      <Check className="h-4 w-4" />
                      Link Copied
                    </>
                  ) : (
                    <>
                      <Share2 className="h-4 w-4" />
                      Share Location
                    </>
                  )}
                </button>
                <a
                  href={mapsUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="inline-flex h-11 items-center gap-1.5 rounded-full border border-[#ECA99C] px-4 text-xs font-medium text-[#F5F2F0] hover:bg-[#252525] whitespace-nowrap shrink-0"
                >
                  Open Maps
                  <ExternalLink className="h-3.5 w-3.5 text-[#F5B6A9]" />
                </a>
              </div>
            </div>
          </div>
        </div>

        {/* Location History List */}
        <section className="rounded-2xl border border-[#393333] bg-[#1C1C1C] p-5">
          <h2 className="text-sm font-semibold text-[#F5F2F0]">
            Location Snapshots
          </h2>
          {bundle.locationHistory.length === 0 ? (
            <p className="mt-3 text-xs text-[#A5A0A0]">
              No location snapshots logged yet. Share or update coordinates to store a snapshot.
            </p>
          ) : (
            <div className="mt-3 divide-y divide-[#393333]">
              {bundle.locationHistory.slice(0, 6).map((loc) => (
                <div
                  key={loc.id}
                  className="flex flex-col justify-between gap-2 py-3 first:pt-1 last:pb-0 sm:flex-row sm:items-center"
                >
                  <div>
                    <p className="text-sm font-medium text-[#F5F2F0]">
                      {loc.address}
                    </p>
                    <p className="mt-0.5 font-mono text-xs text-[#A5A0A0] tabular-nums">
                      {formatClientCoordinates(loc.latitude, loc.longitude)} · {loc.locationType}
                    </p>
                  </div>
                  <div className="flex items-center gap-3">
                    <span suppressHydrationWarning className="font-mono text-xs text-[#777272] tabular-nums">
                      {formatDisplayDateTime(loc.createdAt)}
                    </span>
                    <a
                      href={buildClientGoogleMapsUrl(loc.latitude, loc.longitude)}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="text-xs font-medium text-[#F5B6A9] hover:text-[#ECA99C] whitespace-nowrap shrink-0"
                    >
                      Map
                    </a>
                  </div>
                </div>
              ))}
            </div>
          )}
        </section>
      </div>

      {/* Right Coordinate Configuration Panel */}
      <aside className="rounded-2xl border border-[#393333] bg-[#1C1C1C] p-6">
        <div className="flex items-center gap-2.5">
          <Navigation className="h-4 w-4 text-[#F5B6A9]" />
          <h2 className="text-base font-semibold text-[#F5F2F0]">
            Update Coordinates
          </h2>
        </div>
        <p className="mt-1.5 text-xs leading-relaxed text-[#A5A0A0]">
          Desktop browsers without GPS hardware or denied location permissions use simulated coordinates. Adjust and save coordinates below.
        </p>

        <form onSubmit={handleManualSave} className="mt-5 space-y-4">
          <div>
            <label htmlFor="loc-lat" className="mb-1.5 block text-xs font-medium text-[#A5A0A0]">
              Latitude (-90 to 90)
            </label>
            <input
              id="loc-lat"
              type="text"
              required
              value={latInput}
              onChange={(e) => setLatInput(e.target.value)}
              className="h-11 w-full rounded-xl border border-[#393333] bg-[#111111] px-3.5 font-mono text-sm text-[#F5F2F0] tabular-nums focus:border-[#ECA99C] focus:outline-none"
            />
          </div>

          <div>
            <label htmlFor="loc-lng" className="mb-1.5 block text-xs font-medium text-[#A5A0A0]">
              Longitude (-180 to 180)
            </label>
            <input
              id="loc-lng"
              type="text"
              required
              value={lngInput}
              onChange={(e) => setLngInput(e.target.value)}
              className="h-11 w-full rounded-xl border border-[#393333] bg-[#111111] px-3.5 font-mono text-sm text-[#F5F2F0] tabular-nums focus:border-[#ECA99C] focus:outline-none"
            />
          </div>

          <div>
            <label htmlFor="loc-label" className="mb-1.5 block text-xs font-medium text-[#A5A0A0]">
              Location Label / Address
            </label>
            <input
              id="loc-label"
              type="text"
              value={addrInput}
              onChange={(e) => setAddrInput(e.target.value)}
              placeholder="e.g. Kandivali (E), Mumbai"
              className="h-11 w-full rounded-xl border border-[#393333] bg-[#111111] px-3.5 text-sm text-[#F5F2F0] placeholder:text-[#777272] focus:border-[#ECA99C] focus:outline-none"
            />
          </div>

          {errorMessage ? (
            <p role="alert" className="rounded-xl border border-[#D96B70]/40 bg-[#D96B70]/10 px-3.5 py-2.5 text-xs text-[#F5B6A9]">
              {errorMessage}
            </p>
          ) : null}

          {statusMessage ? (
            <p className="rounded-xl border border-[#8DBD87]/40 bg-[#8DBD87]/10 px-3.5 py-2.5 text-xs text-[#8DBD87]">
              {statusMessage}
            </p>
          ) : null}

          <button
            type="submit"
            disabled={saving}
            className="h-11 w-full rounded-full bg-[#F5B6A9] px-5 text-sm font-semibold text-[#111111] transition-colors hover:bg-[#ECA99C] disabled:opacity-60 whitespace-nowrap shrink-0"
          >
            {saving ? 'Saving...' : 'Save Location Snapshot'}
          </button>
        </form>
      </aside>
    </div>
  );
}
