import React, { ReactNode } from 'react';

interface BadgeProps {
    children: ReactNode;
    variant?: 'primary' | 'secondary' | 'success' | 'warning' | 'danger' | 'neutral';
    size?: 'sm' | 'md' | 'lg';
    className?: string;
}

const Badge: React.FC<BadgeProps> = ({
                                         children,
                                         variant = 'primary',
                                         size = 'md',
                                         className = ''
                                     }) => {
    const baseClasses = 'inline-flex items-center justify-center font-medium rounded-full';

    const variantClasses = {
        primary: 'bg-primary-100 text-primary-800',
        secondary: 'bg-secondary-100 text-secondary-800',
        success: 'bg-success-100 text-success-800',
        warning: 'bg-warning-100 text-warning-800',
        danger: 'bg-danger-100 text-danger-800',
        neutral: 'bg-neutral-100 text-neutral-800',
    };

    const sizeClasses = {
        sm: 'px-2 py-1 text-xs',
        md: 'px-3 py-1.5 text-sm',
        lg: 'px-4 py-2 text-base',
    };

    const combinedClasses = `${baseClasses} ${variantClasses[variant]} ${sizeClasses[size]} ${className}`;

    return (
        <span className={combinedClasses}>
      {children}
    </span>
    );
};

export default Badge;