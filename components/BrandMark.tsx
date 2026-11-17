import { Shield } from 'lucide-react';

export default function BrandMark({
  size = 'md',
}: {
  size?: 'sm' | 'md' | 'lg';
}) {
  const boxClass =
    size === 'lg'
      ? 'h-16 w-16 rounded-2xl'
      : size === 'sm'
        ? 'h-8 w-8 rounded-lg'
        : 'h-10 w-10 rounded-xl';
  const iconClass =
    size === 'lg' ? 'h-8 w-8' : size === 'sm' ? 'h-4 w-4' : 'h-5 w-5';

  return (
    <div
      className={`inline-flex shrink-0 items-center justify-center border border-[#ECA99C]/45 bg-[#1C1C1C] text-[#F5B6A9] shadow-[0_0_24px_rgba(236,169,156,0.14)] ${boxClass}`}
    >
      <Shield className={iconClass} strokeWidth={1.9} />
    </div>
  );
}
