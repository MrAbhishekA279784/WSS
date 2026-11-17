'use client';

import { useRef, useState } from 'react';
import { motion } from 'motion/react';

interface TiltCardProps {
  children: React.ReactNode;
  className?: string;
  maxTilt?: number;
  onClick?: () => void;
  interactive?: boolean;
}

export default function TiltCard({
  children,
  className = '',
  maxTilt = 3.5,
  onClick,
  interactive = true,
}: TiltCardProps) {
  const cardRef = useRef<HTMLDivElement>(null);
  const [rotateX, setRotateX] = useState(0);
  const [rotateY, setRotateY] = useState(0);
  const [glarePos, setGlarePos] = useState({ x: 50, y: 50, opacity: 0 });

  const handleMouseMove = (e: React.MouseEvent<HTMLDivElement>) => {
    if (!interactive || !cardRef.current) return;
    const rect = cardRef.current.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;

    const centerX = rect.width / 2;
    const centerY = rect.height / 2;

    const normX = (x - centerX) / centerX;
    const normY = (y - centerY) / centerY;

    setRotateX(-normY * maxTilt);
    setRotateY(normX * maxTilt);

    const glareX = (x / rect.width) * 100;
    const glareY = (y / rect.height) * 100;
    setGlarePos({ x: glareX, y: glareY, opacity: 0.12 });
  };

  const handleMouseLeave = () => {
    if (!interactive) return;
    setRotateX(0);
    setRotateY(0);
    setGlarePos((prev) => ({ ...prev, opacity: 0 }));
  };

  const isTilted = rotateX !== 0 || rotateY !== 0;

  return (
    <motion.div
      ref={cardRef}
      suppressHydrationWarning
      onMouseMove={handleMouseMove}
      onMouseLeave={handleMouseLeave}
      onClick={onClick}
      animate={isTilted ? { rotateX, rotateY } : undefined}
      transition={{
        type: 'spring',
        stiffness: 280,
        damping: 24,
      }}
      style={{
        transformStyle: 'preserve-3d',
        perspective: 900,
      }}
      className={`relative overflow-hidden transition-shadow duration-200 ${className}`}
    >
      {/* Subtle 3D dynamic specular sheen */}
      <div
        aria-hidden="true"
        className="pointer-events-none absolute inset-0 z-10 transition-opacity duration-300"
        style={{
          opacity: glarePos.opacity,
          background: `radial-gradient(circle 260px at ${glarePos.x}% ${glarePos.y}%, rgba(245, 182, 169, 0.14), transparent 75%)`,
        }}
      />
      {children}
    </motion.div>
  );
}
