import type { HTMLAttributes } from 'react';
export function Card({ className = '', ...props }: HTMLAttributes<HTMLDivElement>) { return <section className={`ui-card ${className}`} {...props} />; }