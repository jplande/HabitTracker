import React, { ReactNode } from 'react';

interface CardProps {
    children: ReactNode;
    variant?: 'default' | 'elevated' | 'outlined' | 'filled';
    padding?: 'none' | 'sm' | 'md' | 'lg';
    className?: string;
    onClick?: () => void;
    hover?: boolean;
}

const Card: React.FC<CardProps> = ({
                                       children,
                                       variant = 'default',
                                       padding = 'md',
                                       className = '',
                                       onClick,
                                       hover = false,
                                   }) => {
    const baseClasses = 'rounded-2xl transition-all duration-200';

    const variantClasses = {
        default: 'bg-white border border-neutral-200',
        elevated: 'bg-white shadow-soft',
        outlined: 'bg-white border-2 border-primary-200',
        filled: 'bg-neutral-50 border border-neutral-200',
    };

    const paddingClasses = {
        none: '',
        sm: 'p-4',
        md: 'p-6',
        lg: 'p-8',
    };

    const interactiveClasses = onClick ? 'cursor-pointer' : '';
    const hoverClasses = hover || onClick ? 'hover:shadow-medium hover:-translate-y-1' : '';

    const combinedClasses = `${baseClasses} ${variantClasses[variant]} ${paddingClasses[padding]} ${interactiveClasses} ${hoverClasses} ${className}`;

    return (
        <div className={combinedClasses} onClick={onClick}>
            {children}
        </div>
    );
};

// Sous-composants pour une meilleure organisation
export const CardHeader: React.FC<{ children: ReactNode; className?: string }> = ({
                                                                                      children,
                                                                                      className = '',
                                                                                  }) => (
    <div className={`mb-4 ${className}`}>
        {children}
    </div>
);

export const CardBody: React.FC<{ children: ReactNode; className?: string }> = ({
                                                                                    children,
                                                                                    className = '',
                                                                                }) => (
    <div className={className}>
        {children}
    </div>
);

export const CardFooter: React.FC<{ children: ReactNode; className?: string }> = ({
                                                                                      children,
                                                                                      className = '',
                                                                                  }) => (
    <div className={`mt-4 pt-4 border-t border-neutral-200 ${className}`}>
        {children}
    </div>
);

export default Card;