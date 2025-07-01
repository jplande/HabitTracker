// src/services/notificationService.ts
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

class NotificationService {
    private listeners: Array<(notifications: Notification[]) => void> = [];
    private notifications: Notification[] = [];

    subscribe(listener: (notifications: Notification[]) => void) {
        this.listeners.push(listener);
        return () => {
            this.listeners = this.listeners.filter(l => l !== listener);
        };
    }

    private notify() {
        this.listeners.forEach(listener => listener([...this.notifications]));
    }

    show(notification: Omit<Notification, 'id'>) {
        const id = Date.now().toString() + Math.random().toString(36).substr(2, 9);
        const newNotification: Notification = {
            id,
            duration: 5000,
            ...notification
        };

        this.notifications.push(newNotification);
        this.notify();

        // Auto-dismiss
        if (newNotification.duration && newNotification.duration > 0) {
            setTimeout(() => {
                this.dismiss(id);
            }, newNotification.duration);
        }

        return id;
    }

    dismiss(id: string) {
        this.notifications = this.notifications.filter(n => n.id !== id);
        this.notify();
    }

    dismissAll() {
        this.notifications = [];
        this.notify();
    }

    // Méthodes de convenance
    success(title: string, message?: string, action?: Notification['action']) {
        return this.show({
            type: 'success',
            title,
            message,
            action
        });
    }

    error(title: string, message?: string, action?: Notification['action']) {
        return this.show({
            type: 'error',
            title,
            message,
            duration: 7000, // Plus long pour les erreurs
            action
        });
    }

    warning(title: string, message?: string, action?: Notification['action']) {
        return this.show({
            type: 'warning',
            title,
            message,
            action
        });
    }

    info(title: string, message?: string, action?: Notification['action']) {
        return this.show({
            type: 'info',
            title,
            message,
            action
        });
    }

    // Notifications spécialisées pour l'app
    habitCreated(habitTitle: string) {
        return this.success(
            'Habitude créée !',
            `"${habitTitle}" a été ajoutée à vos habitudes.`
        );
    }

    habitUpdated(habitTitle: string) {
        return this.success(
            'Habitude mise à jour !',
            `"${habitTitle}" a été modifiée avec succès.`
        );
    }

    habitDeleted(habitTitle: string) {
        return this.success(
            'Habitude supprimée',
            `"${habitTitle}" a été supprimée de vos habitudes.`
        );
    }

    progressAdded(habitTitle: string, value: number, unit: string) {
        return this.success(
            'Progrès enregistré !',
            `${value} ${unit} ajouté${value > 1 ? 's' : ''} pour "${habitTitle}".`
        );
    }

    achievementUnlocked(achievementName: string, achievementIcon: string) {
        return this.success(
            'Nouveau badge débloqué !',
            `${achievementIcon} ${achievementName}`,
            {
                label: 'Voir mes badges',
                onClick: () => window.location.href = '/achievements'
            }
        );
    }

    streakMilestone(days: number) {
        const emoji = days >= 30 ? '🏆' : days >= 14 ? '🔥' : '⭐';
        return this.success(
            `Série de ${days} jours !`,
            `${emoji} Félicitations pour votre constance !`
        );
    }

    networkError() {
        return this.error(
            'Problème de connexion',
            'Vérifiez votre connexion internet et réessayez.',
            {
                label: 'Réessayer',
                onClick: () => window.location.reload()
            }
        );
    }

    serverError() {
        return this.error(
            'Erreur du serveur',
            'Une erreur temporaire s\'est produite. Veuillez réessayer plus tard.'
        );
    }

    validationError(message: string) {
        return this.warning(
            'Données invalides',
            message
        );
    }
}

export const notificationService = new NotificationService();

// Hook React pour utiliser les notifications
import { useState, useEffect } from 'react';

export function useNotifications() {
    const [notifications, setNotifications] = useState<Notification[]>([]);

    useEffect(() => {
        const unsubscribe = notificationService.subscribe(setNotifications);
        return unsubscribe;
    }, []);

    return {
        notifications,
        show: notificationService.show.bind(notificationService),
        dismiss: notificationService.dismiss.bind(notificationService),
        dismissAll: notificationService.dismissAll.bind(notificationService),
        success: notificationService.success.bind(notificationService),
        error: notificationService.error.bind(notificationService),
        warning: notificationService.warning.bind(notificationService),
        info: notificationService.info.bind(notificationService),
    };
}