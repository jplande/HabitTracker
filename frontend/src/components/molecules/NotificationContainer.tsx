// src/components/molecules/NotificationContainer.tsx
import React from 'react';
import { X, CheckCircle, AlertCircle, AlertTriangle, Info } from 'lucide-react';
import { useNotifications, Notification, NotificationType } from '../../services/notificationService';
import Button from '../atoms/Button';

const NotificationItem: React.FC<{
    notification: Notification;
    onDismiss: (id: string) => void;
}> = ({ notification, onDismiss }) => {
    const getIcon = (type: NotificationType) => {
        switch (type) {
            case 'success':
                return <CheckCircle className="h-5 w-5 text-success-600" />;
            case 'error':
                return <AlertCircle className="h-5 w-5 text-danger-600" />;
            case 'warning':
                return <AlertTriangle className="h-5 w-5 text-warning-600" />;
            case 'info':
                return <Info className="h-5 w-5 text-primary-600" />;
            default:
                return <Info className="h-5 w-5 text-neutral-600" />;
        }
    };

    const getBackgroundColor = (type: NotificationType) => {
        switch (type) {
            case 'success':
                return 'bg-success-50 border-success-200';
            case 'error':
                return 'bg-danger-50 border-danger-200';
            case 'warning':
                return 'bg-warning-50 border-warning-200';
            case 'info':
                return 'bg-primary-50 border-primary-200';
            default:
                return 'bg-neutral-50 border-neutral-200';
        }
    };

    const getTextColor = (type: NotificationType) => {
        switch (type) {
            case 'success':
                return 'text-success-900';
            case 'error':
                return 'text-danger-900';
            case 'warning':
                return 'text-warning-900';
            case 'info':
                return 'text-primary-900';
            default:
                return 'text-neutral-900';
        }
    };

    return (
        <div className={`
            ${getBackgroundColor(notification.type)}
            border rounded-xl p-4 shadow-medium backdrop-blur-sm
            animate-slide-in max-w-sm w-full
        `}>
            <div className="flex items-start gap-3">
                <div className="flex-shrink-0 mt-0.5">
                    {getIcon(notification.type)}
                </div>

                <div className="flex-1 min-w-0">
                    <h4 className={`font-semibold text-sm ${getTextColor(notification.type)}`}>
                        {notification.title}
                    </h4>
                    {notification.message && (
                        <p className={`text-sm mt-1 ${getTextColor(notification.type)} opacity-80`}>
                            {notification.message}
                        </p>
                    )}

                    {notification.action && (
                        <div className="mt-3">
                            <Button
                                variant="outline"
                                size="sm"
                                onClick={notification.action.onClick}
                                className="text-xs"
                            >
                                {notification.action.label}
                            </Button>
                        </div>
                    )}
                </div>

                <button
                    onClick={() => onDismiss(notification.id)}
                    className={`
                        flex-shrink-0 rounded-lg p-1 hover:bg-black hover:bg-opacity-10 
                        transition-colors duration-200 ${getTextColor(notification.type)} opacity-60 hover:opacity-100
                    `}
                >
                    <X className="h-4 w-4" />
                </button>
            </div>
        </div>
    );
};

const NotificationContainer: React.FC = () => {
    const { notifications, dismiss } = useNotifications();

    if (notifications.length === 0) {
        return null;
    }

    return (
        <div className="fixed top-4 right-4 z-50 flex flex-col gap-3 pointer-events-none">
            {notifications.map(notification => (
                <div key={notification.id} className="pointer-events-auto">
                    <NotificationItem
                        notification={notification}
                        onDismiss={dismiss}
                    />
                </div>
            ))}
        </div>
    );
};

export default NotificationContainer;