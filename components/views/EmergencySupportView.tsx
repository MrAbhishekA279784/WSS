'use client';

import { useState } from 'react';
import { motion } from 'motion/react';
import {
  Ambulance,
  Check,
  Copy,
  Phone,
  PhoneCall,
  Shield,
  UserCheck,
} from 'lucide-react';
import TiltCard from '@/components/TiltCard';
import { triggerHaptic } from '@/lib/haptics';
import { EMERGENCY_HELPLINES, type EmergencyHelpline } from '@/lib/types';

export default function EmergencySupportView() {
  const [copiedId, setCopiedId] = useState<string | null>(null);

  const renderIcon = (type: EmergencyHelpline['icon']) => {
    switch (type) {
      case 'shield':
        return <Shield className="h-5 w-5" />;
      case 'women':
        return <UserCheck className="h-5 w-5" />;
      case 'ambulance':
        return <Ambulance className="h-5 w-5" />;
      case 'phone':
        return <PhoneCall className="h-5 w-5" />;
    }
  };

  const handleCopy = async (helpline: EmergencyHelpline) => {
    try {
      triggerHaptic('confirm');
      await navigator.clipboard.writeText(helpline.number);
      setCopiedId(helpline.id);
      setTimeout(() => setCopiedId(null), 2000);
    } catch {
      // ignore clipboard restrictions
    }
  };

  return (
    <div className="space-y-6">
      <div className="rounded-2xl border border-[#393333] bg-[#111111] p-5 shadow-inner">
        <p className="text-sm font-medium text-[#F5F2F0]">
          Official Emergency Helplines
        </p>
        <p className="mt-1 text-xs leading-relaxed text-[#A5A0A0]">
          Select Call to invoke your device&apos;s phone dialer (`tel:`), or copy the helpline number to dial manually from any phone.
        </p>
      </div>

      <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
        {EMERGENCY_HELPLINES.map((helpline) => (
          <TiltCard
            key={helpline.id}
            maxTilt={5}
            className="flex flex-col justify-between gap-5 p-6 hover:border-[#ECA99C]/60"
          >
            <div className="flex items-start justify-between gap-4">
              <div className="flex items-start gap-4">
                <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-full border border-[#393333] bg-[#111111] text-[#F5B6A9] shadow-md">
                  {renderIcon(helpline.icon)}
                </div>
                <div>
                  <h2 className="text-base font-semibold text-[#F5F2F0]">
                    {helpline.title}
                  </h2>
                  <p className="mt-1 font-mono text-2xl font-bold tracking-tight text-[#F5B6A9] tabular-nums">
                    {helpline.number}
                  </p>
                  <p className="mt-1.5 text-xs leading-relaxed text-[#A5A0A0]">
                    {helpline.description}
                  </p>
                </div>
              </div>
            </div>

            <div className="flex items-center justify-end gap-2.5 border-t border-[#393333] pt-4">
              <motion.button
                type="button"
                whileTap={{ scale: 0.95 }}
                onClick={() => handleCopy(helpline)}
                className="inline-flex h-10 items-center gap-1.5 rounded-full border border-[#393333] bg-[#111111] px-4 text-xs font-medium text-[#F5F2F0] hover:border-[#ECA99C] whitespace-nowrap shrink-0 transition-colors"
              >
                {copiedId === helpline.id ? (
                  <>
                    <Check className="h-3.5 w-3.5 text-[#8DBD87]" />
                    Copied
                  </>
                ) : (
                  <>
                    <Copy className="h-3.5 w-3.5 text-[#F5B6A9]" />
                    Copy {helpline.number}
                  </>
                )}
              </motion.button>

              <motion.a
                whileTap={{ scale: 0.95 }}
                href={`tel:${helpline.number}`}
                onClick={() => triggerHaptic('confirm')}
                className="inline-flex h-10 items-center gap-1.5 rounded-full bg-[#F5B6A9] px-5 text-xs font-semibold text-[#111111] transition-all hover:bg-[#ECA99C] shadow-sm hover:shadow-[#ECA99C]/20 whitespace-nowrap shrink-0"
              >
                <Phone className="h-3.5 w-3.5" />
                Call {helpline.number}
              </motion.a>
            </div>
          </TiltCard>
        ))}
      </div>
    </div>
  );
}
