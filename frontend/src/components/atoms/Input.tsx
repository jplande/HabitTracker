import React, { InputHTMLAttributes, ReactNode, forwardRef } from 'react';
import { AlertCircle } from 'lucide-react';

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
    label?: string;
    error?: string;
    hint?: string;
    icon?: ReactNode;
    variant?: 'default' | 'filled' | 'underlined';
    fullWidth?: boolean;
}

const Input = forwardRef<HTMLInputElement, InputProps>(
    ({
         label,
         error,
         hint,
         icon,
         variant = 'default',
         fullWidth = false,
         className = '',
         ...props
     }, ref) => {
        const baseInputClasses = 'transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-primary-500 disabled:opacity-50 disabled:cursor-not-allowed';

        const variantClasses = {
            default: 'border border-neutral-300 rounded-xl px-4 py-3 bg-white hover:border-primary-400 focus:border-primary-500',
            filled: 'border-0 rounded-xl px-4 py-3 bg-neutral-100 hover:bg-neutral-50 focus:bg-white',
            underlined: 'border-0 border-b-2 border-neutral-300 rounded-none px-0 py-3 bg-transparent hover:border-primary-400 focus:border-primary-500',
        };

        const widthClass = fullWidth ? 'w-full' : '';
        const errorClass = error ? 'border-danger-500 focus:border-danger-500 focus:ring-danger-500' : '';

        const inputClasses = `${baseInputClasses} ${variantClasses[variant]} ${widthClass} ${errorClass} ${className}`;

        return (
            <div className={`${fullWidth ? 'w-full' : ''}`}>
                {label && (
                    <label className="block text-sm font-medium text-neutral-700 mb-2">
                        {label}
                    </label>
                )}

                <div className="relative">
                    {icon && (
                        <div className="absolute left-3 top-1/2 transform -translate-y-1/2 text-neutral-500">
                            {icon}
                        </div>
                    )}

                    <input
                        ref={ref}
                        className={`${inputClasses} ${icon ? 'pl-10' : ''}`}
                        {...props}
                    />

                    {error && (
                        <div className="absolute right-3 top-1/2 transform -translate-y-1/2 text-danger-500">
                            <AlertCircle className="h-5 w-5" />
                        </div>
                    )}
                </div>

                {error && (
                    <p className="mt-2 text-sm text-danger-600 flex items-center gap-1">
                        <AlertCircle className="h-4 w-4" />
                        {error}
                    </p>
                )}

                {hint && !error && (
                    <p className="mt-2 text-sm text-neutral-500">
                        {hint}
                    </p>
                )}
            </div>
        );
    }
);

Input.displayName = 'Input';

export default Input;