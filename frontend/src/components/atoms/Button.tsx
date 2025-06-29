import React, { ButtonHTMLAttributes, ReactNode } from 'react';
import { Loader2 } from 'lucide-react';

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
    variant?: 'primary' | 'secondary' | 'outline' | 'ghost' | 'danger';
    size?: 'sm' | 'md' | 'lg';
    isLoading?: boolean;
    icon?: ReactNode;
    children: ReactNode;
}

const Button: React.FC<ButtonProps> = ({
                                           variant = 'primary',
                                           size = 'md',
                                           isLoading = false,
                                           icon,
                                           children,
                                           className = '',
                                           disabled,
                                           ...props
                                       }) => {
    const baseClasses = 'inline-flex items-center justify-center font-medium rounded-xl transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed';

    const variantClasses = {
        primary: 'bg-primary-500 hover:bg-primary-600 text-white shadow-soft hover:shadow-medium focus:ring-primary-500',
        secondary: 'bg-secondary-500 hover:bg-secondary-600 text-white shadow-soft hover:shadow-medium focus:ring-secondary-500',
        outline: 'border-2 border-primary-500 text-primary-600 hover:bg-primary-50 focus:ring-primary-500',
        ghost: 'text-primary-600 hover:bg-primary-50 focus:ring-primary-500',
        danger: 'bg-danger-500 hover:bg-danger-600 text-white shadow-soft hover:shadow-medium focus:ring-danger-500',
    };

    const sizeClasses = {
        sm: 'px-3 py-2 text-sm gap-2',
        md: 'px-4 py-2.5 text-base gap-2',
        lg: 'px-6 py-3 text-lg gap-3',
    };

    const combinedClasses = `${baseClasses} ${variantClasses[variant]} ${sizeClasses[size]} ${className}`;

    return (
        <button
            className={combinedClasses}
            disabled={disabled || isLoading}
            {...props}
        >
            {isLoading ? (
                <Loader2 className="h-4 w-4 animate-spin" />
            ) : icon ? (
                icon
            ) : null}
            {children}
        </button>
    );
};

export default Button;