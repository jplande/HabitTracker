import React, { ReactNode } from 'react';
import Navigation from '../molecules/Navigation';

interface MainLayoutProps {
    children: ReactNode;
    title?: string;
    subtitle?: string;
    actions?: ReactNode;
}

const MainLayout: React.FC<MainLayoutProps> = ({
                                                   children,
                                                   title,
                                                   subtitle,
                                                   actions
                                               }) => {
    return (
        <div className="flex h-screen bg-neutral-50">
            {/* Navigation sidebar */}
            <Navigation />

            {/* Main content */}
            <main className="flex-1 overflow-hidden">
                <div className="h-full flex flex-col">
                    {/* Header */}
                    {(title || actions) && (
                        <header className="bg-white border-b border-neutral-200 px-6 py-6 md:px-8">
                            <div className="flex items-center justify-between">
                                <div>
                                    {title && (
                                        <h1 className="text-2xl md:text-3xl font-bold text-neutral-900 mb-1">
                                            {title}
                                        </h1>
                                    )}
                                    {subtitle && (
                                        <p className="text-neutral-600">{subtitle}</p>
                                    )}
                                </div>
                                {actions && (
                                    <div className="flex items-center gap-3">
                                        {actions}
                                    </div>
                                )}
                            </div>
                        </header>
                    )}

                    {/* Content area */}
                    <div className="flex-1 overflow-y-auto">
                        <div className="p-6 md:p-8 pb-20 md:pb-8">
                            {children}
                        </div>
                    </div>
                </div>
            </main>
        </div>
    );
};

export default MainLayout;