// src/components/organisms/NotificationProvider.tsx
import React, { createContext, useContext, useState, useCallback, ReactNode } from 'react';
import { CheckCircle, AlertCircle, AlertTriangle, Info, X } from 'lucide-react';

export type NotificationType = 'success' | 'error' | 'warning' | 'info';

export interface Notification {
    id: string;
    type: NotificationType;
    title: string;
    message?: string;
    duration?: number;
    action?: {
        label: string;
        onClick: () => void;
    };
}

interface NotificationContextType {
    notifications: Notification[];
    addNotification: (notification: Omit<Notification, 'id'>) => string;
    removeNotification: (id: string) => void;
    removeAllNotifications: () => void;
    success: (title: string, message?: string) => string;
    error: (title: string, message?: string) => string;
    warning: (title: string, message?: string) => string;
    info: (title: string, message?: string) => string;
}

const NotificationContext = createContext<NotificationContextType | null>(null);

export const useNotifications = () => {
    const context = useContext(NotificationContext);
    if (!context) {
        throw new Error('useNotifications must be used within a NotificationProvider');
    }
    return context;
};

interface NotificationProviderProps {
    children: ReactNode;
}

export const NotificationProvider: React.FC<NotificationProviderProps> = ({ children }) => {
    const [notifications, setNotifications] = useState<Notification[]>([]);

    const generateId = () => {
        return Date.now().toString() + Math.random().toString(36).substr(2, 9);
    };

    const addNotification = useCallback((notification: Omit<Notification, 'id'>) => {
        const id = generateId();
        const newNotification: Notification = {
            id,
            duration: 5000,
            ...notification,
        };

        setNotifications(prev => [...prev, newNotification]);

        // Auto-remove after duration
        if (newNotification.duration && newNotification.duration > 0) {
            setTimeout(() => {
                removeNotification(id);
            }, newNotification.duration);
        }

        return id;
    }, []);

    const removeNotification = useCallback((id: string) => {
        setNotifications(prev => prev.filter(notification => notification.id !== id));
    }, []);

    const removeAllNotifications = useCallback(() => {
        setNotifications([]);
    }, []);

    const success = useCallback((title: string, message?: string) => {
        return addNotification({ type: 'success', title, message });
    }, [addNotification]);

    const error = useCallback((title: string, message?: string) => {
        return addNotification({ type: 'error', title, message, duration: 7000 });
    }, [addNotification]);

    const warning = useCallback((title: string, message?: string) => {
        return addNotification({ type: 'warning', title, message });
    }, [addNotification]);

    const info = useCallback((title: string, message?: string) => {
        return addNotification({ type: 'info', title, message });
    }, [addNotification]);

    const value: NotificationContextType = {
        notifications,
        addNotification,
        removeNotification,
        removeAllNotifications,
        success,
        error,
        warning,
        info,
    };

    return (
        <NotificationContext.Provider value={value}>
            {children}
            <NotificationContainer />
        </NotificationContext.Provider>
    );
};

// Composant pour afficher les notifications
const NotificationContainer: React.FC = () => {
    const { notifications, removeNotification } = useNotifications();

    return (
        <div className="fixed top-4 right-4 z-50 space-y-2 max-w-sm">
            {notifications.map((notification) => (
                <NotificationItem
                    key={notification.id}
                    notification={notification}
                    onRemove={removeNotification}
                />
            ))}
        </div>
    );
};

interface NotificationItemProps {
    notification: Notification;
    onRemove: (id: string) => void;
}

const NotificationItem: React.FC<NotificationItemProps> = ({ notification, onRemove }) => {
    const getIcon = () => {
        switch (notification.type) {
            case 'success':
                return <CheckCircle className="h-5 w-5 text-success-500" />;
            case 'error':
                return <AlertCircle className="h-5 w-5 text-danger-500" />;
            case 'warning':
                return <AlertTriangle className="h-5 w-5 text-warning-500" />;
            case 'info':
                return <Info className="h-5 w-5 text-primary-500" />;
            default:
                return <Info className="h-5 w-5 text-primary-500" />;
        }
    };

    const getColorClasses = () => {
        switch (notification.type) {
            case 'success':
                return 'bg-success-50 border-success-200 text-success-800';
            case 'error':
                return 'bg-danger-50 border-danger-200 text-danger-800';
            case 'warning':
                return 'bg-warning-50 border-warning-200 text-warning-800';
            case 'info':
                return 'bg-primary-50 border-primary-200 text-primary-800';
            default:
                return 'bg-neutral-50 border-neutral-200 text-neutral-800';
        }
    };

    return (
        <div
            className={`
                relative p-4 rounded-xl border shadow-medium transition-all duration-300 transform
                animate-slide-in hover:shadow-strong
                ${getColorClasses()}
            `}
        >
            <div className="flex items-start gap-3">
                <div className="flex-shrink-0">
                    {getIcon()}
                </div>

                <div className="flex-1 min-w-0">
                    <h4 className="text-sm font-medium mb-1">
                        {notification.title}
                    </h4>
                    {notification.message && (
                        <p className="text-sm opacity-90">
                            {notification.message}
                        </p>
                    )}
                    {notification.action && (
                        <button
                            onClick={notification.action.onClick}
                            className="mt-2 text-sm font-medium underline hover:no-underline"
                        >
                            {notification.action.label}
                        </button>
                    )}
                </div>

                <button
                    onClick={() => onRemove(notification.id)}
                    className="flex-shrink-0 p-1 rounded-lg hover:bg-black hover:bg-opacity-10 transition-colors"
                >
                    <X className="h-4 w-4" />
                </button>
            </div>
        </div>
    );
};

// Hook personnalisé pour les notifications d'habitudes
export const useHabitNotifications = () => {
    const { success, error, warning, info } = useNotifications();

    return {
        habitCreated: (habitTitle: string) =>
            success('Habitude créée !', `"${habitTitle}" a été ajoutée à vos habitudes.`),

        habitUpdated: (habitTitle: string) =>
            success('Habitude mise à jour !', `"${habitTitle}" a été modifiée avec succès.`),

        habitDeleted: (habitTitle: string) =>
            success('Habitude supprimée', `"${habitTitle}" a été supprimée de vos habitudes.`),

        progressAdded: (habitTitle: string, value: number, unit: string) =>
            success('Progrès enregistré !', `${value} ${unit} ajouté${value > 1 ? 's' : ''} pour "${habitTitle}".`),

        progressUpdated: (habitTitle: string) =>
            success('Progrès mis à jour !', `Le progrès pour "${habitTitle}" a été modifié.`),

        progressDeleted: (habitTitle: string) =>
            success('Progrès supprimé', `Le progrès pour "${habitTitle}" a été supprimé.`),

        achievementUnlocked: (achievementName: string, achievementIcon: string) =>
            success('Nouveau badge débloqué !', `${achievementIcon} ${achievementName}`),

        streakMilestone: (days: number) => {
            const emoji = days >= 30 ? '🏆' : days >= 14 ? '🔥' : '⭐';
            return success(`Série de ${days} jours !`, `${emoji} Félicitations pour votre constance !`);
        },

        networkError: () =>
            error('Problème de connexion', 'Vérifiez votre connexion internet et réessayez.'),

        serverError: () =>
            error('Erreur du serveur', 'Une erreur temporaire s\'est produite. Veuillez réessayer plus tard.'),

        validationError: (message: string) =>
            warning('Données invalides', message),

        loginSuccess: (username: string) =>
            success('Connexion réussie !', `Bon retour, ${username} !`),

        logoutSuccess: () =>
            info('Déconnexion réussie', 'À bientôt !'),

        profileUpdated: () =>
            success('Profil mis à jour !', 'Vos informations ont été sauvegardées.'),

        settingsSaved: () =>
            success('Paramètres sauvegardés !', 'Vos préférences ont été mises à jour.'),
    };
};

export default NotificationProvider;