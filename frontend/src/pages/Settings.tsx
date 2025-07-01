// src/pages/Settings.tsx
import React, { useState } from 'react';
import { Bell, Shield, Palette, Download, Upload, RefreshCw } from 'lucide-react';
import MainLayout from '../components/templates/MainLayout';
import Card, { CardHeader, CardBody } from '../components/atoms/Card';
import Button from '../components/atoms/Button';
import { useAuth } from '../contexts/AuthContext';

interface NotificationSettings {
    emailNotifications: boolean;
    pushNotifications: boolean;
    weeklyReport: boolean;
    achievementAlerts: boolean;
}

interface PrivacySettings {
    profileVisibility: 'public' | 'private';
    dataSharing: boolean;
    analytics: boolean;
}

const Settings: React.FC = () => {
    const { user } = useAuth();

    const [notifications, setNotifications] = useState<NotificationSettings>({
        emailNotifications: true,
        pushNotifications: false,
        weeklyReport: true,
        achievementAlerts: true
    });

    const [privacy, setPrivacy] = useState<PrivacySettings>({
        profileVisibility: 'private',
        dataSharing: false,
        analytics: true
    });

    const [isLoading, setIsLoading] = useState(false);

    const handleNotificationChange = (key: keyof NotificationSettings) => {
        setNotifications(prev => ({
            ...prev,
            [key]: !prev[key]
        }));
    };

    const handlePrivacyChange = (key: keyof PrivacySettings, value: any) => {
        setPrivacy(prev => ({
            ...prev,
            [key]: value
        }));
    };

    const handleSaveSettings = async () => {
        setIsLoading(true);
        try {
            // TODO: Appeler l'API pour sauvegarder les paramètres
            console.log('Sauvegarde des paramètres:', { notifications, privacy });

            // Simuler un délai d'API
            await new Promise(resolve => setTimeout(resolve, 1000));

            alert('Paramètres sauvegardés avec succès !');
        } catch (error) {
            console.error('Erreur lors de la sauvegarde:', error);
            alert('Erreur lors de la sauvegarde des paramètres');
        } finally {
            setIsLoading(false);
        }
    };

    const handleExportData = () => {
        // TODO: Implémenter l'export des données
        console.log('Export des données demandé');
        alert('L\'export des données sera bientôt disponible !');
    };

    const handleImportData = () => {
        // TODO: Implémenter l'import des données
        console.log('Import des données demandé');
        alert('L\'import des données sera bientôt disponible !');
    };

    return (
        <MainLayout
            title="Paramètres"
            subtitle="Configurez votre expérience HabitTracker"
            actions={
                <Button
                    variant="primary"
                    onClick={handleSaveSettings}
                    isLoading={isLoading}
                    icon={<RefreshCw className="h-5 w-5" />}
                >
                    Sauvegarder
                </Button>
            }
        >
            <div className="max-w-2xl space-y-6">
                {/* Notifications */}
                <Card variant="elevated">
                    <CardHeader>
                        <div className="flex items-center gap-3">
                            <Bell className="h-6 w-6 text-primary-500" />
                            <h2 className="text-xl font-semibold text-neutral-900">
                                Notifications
                            </h2>
                        </div>
                    </CardHeader>
                    <CardBody>
                        <div className="space-y-4">
                            <div className="flex items-center justify-between">
                                <div>
                                    <h3 className="font-medium text-neutral-900">Notifications par email</h3>
                                    <p className="text-sm text-neutral-600">Recevez des emails pour les mises à jour importantes</p>
                                </div>
                                <label className="relative inline-flex items-center cursor-pointer">
                                    <input
                                        type="checkbox"
                                        checked={notifications.emailNotifications}
                                        onChange={() => handleNotificationChange('emailNotifications')}
                                        className="sr-only peer"
                                    />
                                    <div className="w-11 h-6 bg-neutral-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-primary-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-neutral-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary-600"></div>
                                </label>
                            </div>

                            <div className="flex items-center justify-between">
                                <div>
                                    <h3 className="font-medium text-neutral-900">Notifications push</h3>
                                    <p className="text-sm text-neutral-600">Recevez des rappels sur votre appareil</p>
                                </div>
                                <label className="relative inline-flex items-center cursor-pointer">
                                    <input
                                        type="checkbox"
                                        checked={notifications.pushNotifications}
                                        onChange={() => handleNotificationChange('pushNotifications')}
                                        className="sr-only peer"
                                    />
                                    <div className="w-11 h-6 bg-neutral-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-primary-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-neutral-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary-600"></div>
                                </label>
                            </div>

                            <div className="flex items-center justify-between">
                                <div>
                                    <h3 className="font-medium text-neutral-900">Rapport hebdomadaire</h3>
                                    <p className="text-sm text-neutral-600">Résumé de vos progrès chaque semaine</p>
                                </div>
                                <label className="relative inline-flex items-center cursor-pointer">
                                    <input
                                        type="checkbox"
                                        checked={notifications.weeklyReport}
                                        onChange={() => handleNotificationChange('weeklyReport')}
                                        className="sr-only peer"
                                    />
                                    <div className="w-11 h-6 bg-neutral-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-primary-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-neutral-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary-600"></div>
                                </label>
                            </div>

                            <div className="flex items-center justify-between">
                                <div>
                                    <h3 className="font-medium text-neutral-900">Alertes de badges</h3>
                                    <p className="text-sm text-neutral-600">Soyez notifié quand vous débloquez un nouveau badge</p>
                                </div>
                                <label className="relative inline-flex items-center cursor-pointer">
                                    <input
                                        type="checkbox"
                                        checked={notifications.achievementAlerts}
                                        onChange={() => handleNotificationChange('achievementAlerts')}
                                        className="sr-only peer"
                                    />
                                    <div className="w-11 h-6 bg-neutral-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-primary-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-neutral-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary-600"></div>
                                </label>
                            </div>
                        </div>
                    </CardBody>
                </Card>

                {/* Confidentialité */}
                <Card variant="elevated">
                    <CardHeader>
                        <div className="flex items-center gap-3">
                            <Shield className="h-6 w-6 text-primary-500" />
                            <h2 className="text-xl font-semibold text-neutral-900">
                                Confidentialité
                            </h2>
                        </div>
                    </CardHeader>
                    <CardBody>
                        <div className="space-y-6">
                            <div>
                                <h3 className="font-medium text-neutral-900 mb-2">Visibilité du profil</h3>
                                <div className="space-y-2">
                                    <label className="flex items-center gap-3 cursor-pointer">
                                        <input
                                            type="radio"
                                            name="profileVisibility"
                                            value="public"
                                            checked={privacy.profileVisibility === 'public'}
                                            onChange={(e) => handlePrivacyChange('profileVisibility', e.target.value)}
                                            className="w-4 h-4 text-primary-600"
                                        />
                                        <div>
                                            <span className="font-medium text-neutral-900">Public</span>
                                            <p className="text-sm text-neutral-600">Votre profil est visible par tous les utilisateurs</p>
                                        </div>
                                    </label>
                                    <label className="flex items-center gap-3 cursor-pointer">
                                        <input
                                            type="radio"
                                            name="profileVisibility"
                                            value="private"
                                            checked={privacy.profileVisibility === 'private'}
                                            onChange={(e) => handlePrivacyChange('profileVisibility', e.target.value)}
                                            className="w-4 h-4 text-primary-600"
                                        />
                                        <div>
                                            <span className="font-medium text-neutral-900">Privé</span>
                                            <p className="text-sm text-neutral-600">Seuls vous pouvez voir votre profil</p>
                                        </div>
                                    </label>
                                </div>
                            </div>

                            <div className="flex items-center justify-between">
                                <div>
                                    <h3 className="font-medium text-neutral-900">Partage de données</h3>
                                    <p className="text-sm text-neutral-600">Autoriser le partage anonyme de vos données pour améliorer l'app</p>
                                </div>
                                <label className="relative inline-flex items-center cursor-pointer">
                                    <input
                                        type="checkbox"
                                        checked={privacy.dataSharing}
                                        onChange={() => handlePrivacyChange('dataSharing', !privacy.dataSharing)}
                                        className="sr-only peer"
                                    />
                                    <div className="w-11 h-6 bg-neutral-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-primary-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-neutral-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary-600"></div>
                                </label>
                            </div>

                            <div className="flex items-center justify-between">
                                <div>
                                    <h3 className="font-medium text-neutral-900">Analytics</h3>
                                    <p className="text-sm text-neutral-600">Nous aider à améliorer l'application avec des données d'usage</p>
                                </div>
                                <label className="relative inline-flex items-center cursor-pointer">
                                    <input
                                        type="checkbox"
                                        checked={privacy.analytics}
                                        onChange={() => handlePrivacyChange('analytics', !privacy.analytics)}
                                        className="sr-only peer"
                                    />
                                    <div className="w-11 h-6 bg-neutral-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-primary-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-neutral-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary-600"></div>
                                </label>
                            </div>
                        </div>
                    </CardBody>
                </Card>

                {/* Apparence */}
                <Card variant="elevated">
                    <CardHeader>
                        <div className="flex items-center gap-3">
                            <Palette className="h-6 w-6 text-primary-500" />
                            <h2 className="text-xl font-semibold text-neutral-900">
                                Apparence
                            </h2>
                        </div>
                    </CardHeader>
                    <CardBody>
                        <div className="text-center py-8">
                            <Palette className="h-16 w-16 text-neutral-300 mx-auto mb-4" />
                            <h3 className="text-lg font-medium text-neutral-900 mb-2">
                                Bientôt disponible
                            </h3>
                            <p className="text-neutral-600">
                                Les options de thème et de personnalisation arriveront prochainement !
                            </p>
                        </div>
                    </CardBody>
                </Card>

                {/* Données */}
                <Card variant="elevated">
                    <CardHeader>
                        <div className="flex items-center gap-3">
                            <Download className="h-6 w-6 text-primary-500" />
                            <h2 className="text-xl font-semibold text-neutral-900">
                                Gestion des données
                            </h2>
                        </div>
                    </CardHeader>
                    <CardBody>
                        <div className="space-y-4">
                            <div className="flex items-center justify-between">
                                <div>
                                    <h3 className="font-medium text-neutral-900">Exporter mes données</h3>
                                    <p className="text-sm text-neutral-600">Téléchargez toutes vos données en format JSON</p>
                                </div>
                                <Button
                                    variant="outline"
                                    size="sm"
                                    onClick={handleExportData}
                                    icon={<Download className="h-4 w-4" />}
                                >
                                    Exporter
                                </Button>
                            </div>

                            <div className="flex items-center justify-between">
                                <div>
                                    <h3 className="font-medium text-neutral-900">Importer des données</h3>
                                    <p className="text-sm text-neutral-600">Restaurez vos données depuis une sauvegarde</p>
                                </div>
                                <Button
                                    variant="outline"
                                    size="sm"
                                    onClick={handleImportData}
                                    icon={<Upload className="h-4 w-4" />}
                                >
                                    Importer
                                </Button>
                            </div>
                        </div>
                    </CardBody>
                </Card>

                {/* Informations sur l'application */}
                <Card variant="filled" className="bg-neutral-50">
                    <CardBody>
                        <div className="text-center">
                            <h3 className="font-medium text-neutral-900 mb-2">
                                HabitTracker v1.0.0
                            </h3>
                            <p className="text-sm text-neutral-600 mb-4">
                                Construisez de meilleures habitudes, un jour à la fois.
                            </p>
                            <div className="flex justify-center gap-4 text-sm text-neutral-500">
                                <button className="hover:text-primary-600 transition-colors">
                                    Conditions d'utilisation
                                </button>
                                <button className="hover:text-primary-600 transition-colors">
                                    Politique de confidentialité
                                </button>
                                <button className="hover:text-primary-600 transition-colors">
                                    Support
                                </button>
                            </div>
                        </div>
                    </CardBody>
                </Card>
            </div>
        </MainLayout>
    );
};

export default Settings;