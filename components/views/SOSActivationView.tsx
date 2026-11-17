'use client';

import { useEffect, useRef, useState } from 'react';
import { motion } from 'motion/react';
import {
  AlertCircle,
  Check,
  CheckCircle2,
  Copy,
  Loader2,
  MessageSquare,
  Phone,
  Siren,
} from 'lucide-react';
import TiltCard from '@/components/TiltCard';
import { triggerHaptic } from '@/lib/haptics';
import {
  apiFetch,
  buildClientGoogleMapsUrl,
  buildSmsHref,
  formatClientCoordinates,
  safeJson,
} from '@/lib/client-utils';
import type { LocationMode, SOSRecord, WorkspaceBundle } from '@/lib/types';

interface SOSActivationViewProps {
  bundle: WorkspaceBundle;
  onSOSUpdated: () => Promise<void>;
  onDone: () => void;
}

type StepState = 'pending' | 'active' | 'done' | 'warn';

export default function SOSActivationView({
  bundle,
  onSOSUpdated,
  onDone,
}: SOSActivationViewProps) {
  const [step1, setStep1] = useState<StepState>('active');
  const [step1Text, setStep1Text] = useState('Capturing current location...');
  const [step2, setStep2] = useState<StepState>('pending');
  const [step2Text, setStep2Text] = useState('Notifying emergency contacts...');
  const [step3, setStep3] = useState<StepState>('pending');
  const [step3Text, setStep3Text] = useState('Saving SOS record to database...');

  const [createdSOS, setCreatedSOS] = useState<SOSRecord | null>(null);
  const [cancelling, setCancelling] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');
  const [copiedMsg, setCopiedMsg] = useState(false);
  const startedRef = useRef(false);

  useEffect(() => {
    if (startedRef.current) return;
    startedRef.current = true;

    const runPipeline = async () => {
      // Step 1: Capture browser GPS if permitted, else use saved/simulated preset
      let lat = bundle.currentLocation.latitude;
      let lng = bundle.currentLocation.longitude;
      let address = bundle.currentLocation.address;
      let locationType: LocationMode = bundle.currentLocation.locationType;

      if (typeof navigator !== 'undefined' && navigator.geolocation) {
        const geo = await new Promise<{
          ok: boolean;
          lat?: number;
          lng?: number;
        }>((resolve) => {
          navigator.geolocation.getCurrentPosition(
            (pos) =>
              resolve({
                ok: true,
                lat: pos.coords.latitude,
                lng: pos.coords.longitude,
              }),
            () => resolve({ ok: false }),
            { enableHighAccuracy: true, timeout: 2200, maximumAge: 10000 }
          );
        });
        if (geo.ok && typeof geo.lat === 'number' && typeof geo.lng === 'number') {
          lat = geo.lat;
          lng = geo.lng;
          address = 'Live Browser Geolocation';
          locationType = 'GPS';
        }
      }

      setStep1('done');
      setStep1Text(`Location captured (${locationType}) · ${formatClientCoordinates(lat, lng)}`);
      setStep2('active');
      setStep3('active');

      try {
        const res = await apiFetch('/api/sos', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ latitude: lat, longitude: lng, address, locationType }),
        });
        const data = await safeJson(res);
        if (!res.ok || !data.sos) {
          setStep2('warn');
          setStep2Text(data.error ?? 'Could not dispatch SOS.');
          setStep3('warn');
          setStep3Text('Failed to save SOS record.');
          setErrorMsg(data.error ?? 'SOS activation failed.');
          return;
        }

        const sos: SOSRecord = data.sos;
        setCreatedSOS(sos);

        if (sos.deliveryStatus === 'SENT') {
          setStep2('done');
          setStep2Text(sos.deliveryNote);
        } else {
          setStep2('warn');
          setStep2Text(
            sos.deliveryStatus === 'NO_CONTACTS'
              ? 'No trusted contacts saved yet'
              : 'External SMS gateway not configured — use manual SMS/Call below'
          );
        }

        setStep3('done');
        setStep3Text('SOS record saved to database');
        await onSOSUpdated();
      } catch {
        setStep2('warn');
        setStep3('warn');
        setErrorMsg('Network error while activating SOS.');
      }
    };

    void runPipeline();
  }, [bundle.currentLocation, onSOSUpdated]);

  const handleCancelSOS = async () => {
    triggerHaptic('sos_cancel');
    if (!createdSOS) {
      onDone();
      return;
    }
    setCancelling(true);
    try {
      const res = await apiFetch(`/api/sos/${createdSOS.id}`, { method: 'PATCH' });
      const data = await safeJson(res);
      if (res.ok && data.sos) {
        setCreatedSOS(data.sos);
        await onSOSUpdated();
      }
    } finally {
      setCancelling(false);
    }
  };

  const handleCopyAlert = async () => {
    triggerHaptic('confirm');
    const lat = createdSOS?.latitude ?? bundle.currentLocation.latitude;
    const lng = createdSOS?.longitude ?? bundle.currentLocation.longitude;
    const label = createdSOS?.locationLabel ?? bundle.currentLocation.address;
    const mode = createdSOS?.locationType ?? bundle.currentLocation.locationType;
    const text = `EMERGENCY SOS from ${bundle.user.fullName}. Location (${mode}): ${label} (${formatClientCoordinates(lat, lng)}). Map: ${buildClientGoogleMapsUrl(lat, lng)}`;
    try {
      await navigator.clipboard.writeText(text);
      setCopiedMsg(true);
      setTimeout(() => setCopiedMsg(false), 2000);
    } catch {
      // ignore clipboard restrictions
    }
  };

  const renderStepIcon = (state: StepState) => {
    if (state === 'done') {
      return <CheckCircle2 className="h-5 w-5 shrink-0 text-[#8DBD87]" />;
    }
    if (state === 'warn') {
      return <AlertCircle className="h-5 w-5 shrink-0 text-[#F5B6A9]" />;
    }
    if (state === 'active') {
      return <Loader2 className="h-5 w-5 shrink-0 animate-spin text-[#F5B6A9]" />;
    }
    return <div className="h-5 w-5 shrink-0 rounded-full border border-[#393333]" />;
  };

  const isCancelled = createdSOS?.status === 'Cancelled';

  return (
    <div className="mx-auto max-w-4xl space-y-5">
      <div className="rounded-2xl border border-[#393333] bg-[#111111] p-6 sm:p-8">
        <div className="flex flex-col items-center text-center">
          <h1 className="text-2xl font-bold tracking-tight text-[#F5F2F0] sm:text-3xl">
            {isCancelled ? 'SOS Alert Cancelled' : 'Sending SOS'}
          </h1>
          <p className="mt-1 text-sm text-[#A5A0A0]">
            {isCancelled
              ? 'This alert has been marked Cancelled in your SOS history.'
              : 'Executing location capture, contact alert pipeline, and database persistence.'}
          </p>

          {/* Concentric Siren Emblem with Pulse Animation */}
          <div
            suppressHydrationWarning
            className={`relative my-5 flex h-40 w-40 items-center justify-center rounded-full border border-[#393333] bg-[#161616] shadow-[0_0_35px_rgba(236,169,156,0.2)] ${
              isCancelled ? '' : 'animate-pulse'
            }`}
          >
            <div className="flex h-28 w-28 items-center justify-center rounded-full border border-[#ECA99C]/35 bg-[#1C1C1C]">
              <div className="flex h-18 w-18 items-center justify-center rounded-full border border-[#ECA99C]/65 bg-[#251D1B] text-[#F5B6A9]">
                <Siren className="h-8 w-8" />
              </div>
            </div>
          </div>

          {/* 3-Step Execution List */}
          <div className="w-full max-w-lg space-y-3 text-left">
            <div className="flex items-center gap-3 rounded-xl border border-[#393333] bg-[#1C1C1C] px-4 py-3">
              {renderStepIcon(step1)}
              <span className="text-sm text-[#F5F2F0]">{step1Text}</span>
            </div>
            <div className="flex items-center gap-3 rounded-xl border border-[#393333] bg-[#1C1C1C] px-4 py-3">
              {renderStepIcon(step2)}
              <span className="text-sm text-[#F5F2F0]">{step2Text}</span>
            </div>
            <div className="flex items-center gap-3 rounded-xl border border-[#393333] bg-[#1C1C1C] px-4 py-3">
              {renderStepIcon(step3)}
              <span className="text-sm text-[#F5F2F0]">{step3Text}</span>
            </div>
          </div>

          {createdSOS ? (
            <p className="mt-4 max-w-lg text-xs leading-relaxed text-[#A5A0A0]">
              {createdSOS.deliveryNote}
            </p>
          ) : null}

          {errorMsg ? (
            <p role="alert" className="mt-4 text-xs text-[#D96B70]">
              {errorMsg}
            </p>
          ) : null}

          <div className="mt-8 flex flex-wrap items-center justify-center gap-3">
            {!isCancelled ? (
              <motion.button
                whileTap={{ scale: 0.94 }}
                type="button"
                disabled={cancelling}
                onClick={handleCancelSOS}
                className="h-11 rounded-full border border-[#393333] bg-[#1C1C1C] px-7 text-sm font-medium text-[#F5F2F0] transition-colors hover:border-[#ECA99C]/60 hover:bg-[#252525] disabled:opacity-60 whitespace-nowrap shrink-0"
              >
                {cancelling ? 'Cancelling...' : 'Cancel SOS'}
              </motion.button>
            ) : null}

            <motion.button
              whileTap={{ scale: 0.94 }}
              type="button"
              onClick={handleCopyAlert}
              className="inline-flex h-11 items-center gap-2 rounded-full border border-[#ECA99C] bg-transparent px-6 text-sm font-medium text-[#F5F2F0] transition-colors hover:bg-[#252525] whitespace-nowrap shrink-0"
            >
              {copiedMsg ? (
                <>
                  <Check className="h-4 w-4 text-[#8DBD87]" />
                  Alert Copied
                </>
              ) : (
                <>
                  <Copy className="h-4 w-4 text-[#F5B6A9]" />
                  Copy Alert Message
                </>
              )}
            </motion.button>

            <motion.button
              whileTap={{ scale: 0.94 }}
              type="button"
              onClick={() => {
                triggerHaptic('confirm');
                onDone();
              }}
              className="h-11 rounded-full bg-[#F5B6A9] px-7 text-sm font-semibold text-[#111111] transition-colors hover:bg-[#ECA99C] whitespace-nowrap shrink-0"
            >
              Done
            </motion.button>
          </div>
        </div>
      </div>

      {/* Direct Manual Contact Actions (always real & honest) */}
      {bundle.contacts.length > 0 ? (
        <section className="rounded-2xl border border-[#393333] bg-[#1C1C1C] p-6">
          <h2 className="text-base font-semibold text-[#F5F2F0]">
            Direct Contact Actions
          </h2>
          <p className="mt-1 text-xs text-[#A5A0A0]">
            Trigger an immediate device call or pre-filled SMS message to your saved contacts.
          </p>

          <div className="mt-4 grid grid-cols-1 gap-3 sm:grid-cols-2">
            {bundle.contacts.map((contact) => {
              const lat = createdSOS?.latitude ?? bundle.currentLocation.latitude;
              const lng = createdSOS?.longitude ?? bundle.currentLocation.longitude;
              const label = createdSOS?.locationLabel ?? bundle.currentLocation.address;
              const mode = createdSOS?.locationType ?? bundle.currentLocation.locationType;
              return (
                <div
                  key={contact.id}
                  className="flex items-center justify-between gap-3 rounded-xl border border-[#393333] bg-[#111111] p-4"
                >
                  <div className="min-w-0">
                    <p className="truncate text-sm font-semibold text-[#F5F2F0]">
                      {contact.name}
                    </p>
                    <p className="mt-0.5 font-mono text-xs text-[#A5A0A0] tabular-nums">
                      {contact.phone}
                    </p>
                  </div>
                  <div className="flex shrink-0 items-center gap-2">
                    <a
                      href={buildSmsHref(contact.phone, bundle.user.fullName, lat, lng, label, mode)}
                      className="inline-flex h-9 items-center gap-1.5 rounded-full border border-[#393333] bg-[#1C1C1C] px-3 text-xs font-medium text-[#F5F2F0] hover:border-[#ECA99C]"
                    >
                      <MessageSquare className="h-3.5 w-3.5 text-[#F5B6A9]" />
                      SMS
                    </a>
                    <a
                      href={`tel:${contact.phone}`}
                      className="inline-flex h-9 items-center gap-1.5 rounded-full bg-[#F5B6A9] px-3 text-xs font-semibold text-[#111111] hover:bg-[#ECA99C]"
                    >
                      <Phone className="h-3.5 w-3.5" />
                      Call
                    </a>
                  </div>
                </div>
              );
            })}
          </div>
        </section>
      ) : null}
    </div>
  );
}
