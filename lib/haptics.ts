export type HapticType =
  | 'sos_activate'
  | 'sos_cancel'
  | 'nav'
  | 'success'
  | 'confirm'
  | 'error';

/**
 * Triggers physical haptic vibration using the browser Vibration API where supported.
 * Falls back gracefully on devices or browsers without vibration hardware.
 * Returns true if the vibration was successfully scheduled by navigator.vibrate.
 */
export function triggerHaptic(type: HapticType): boolean {
  if (typeof window === 'undefined' || typeof navigator === 'undefined') {
    return false;
  }
  if (!('vibrate' in navigator) || typeof navigator.vibrate !== 'function') {
    return false;
  }
  try {
    switch (type) {
      case 'sos_activate':
        // Urgent emergency pulse sequence: dot-dot-dash cadence
        return navigator.vibrate([120, 60, 120, 60, 260]);
      case 'sos_cancel':
        // De-escalation double pulse
        return navigator.vibrate([60, 40, 60]);
      case 'nav':
        // Subtle crisp tab switch micro-tap
        return navigator.vibrate(12);
      case 'success':
        // Positive affirmation sequence
        return navigator.vibrate([25, 30, 45]);
      case 'confirm':
        // Solid decisive confirmation tap
        return navigator.vibrate(22);
      case 'error':
        // Alert warning double pulse
        return navigator.vibrate([70, 40, 70]);
      default:
        return false;
    }
  } catch {
    return false;
  }
}
