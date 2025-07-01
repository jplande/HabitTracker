// src/pages/Profile.tsx
import React, { useState } from 'react';
import { User, Mail, Calendar, Shield, Edit3, Save, X } from 'lucide-react';
import MainLayout from '../components/templates/MainLayout';
import Card, { CardHeader, CardBody, CardFooter } from '../components/atoms/Card';
import Button from '../components/atoms/Button';
import Input from '../components/atoms/Input';
import Badge from '../components/atoms/Badge';
import { useAuth } from '../contexts/AuthContext';

interface ProfileFormData {
    firstName: string;
    lastName: string;
    email: string;
}

const Profile: React.FC = () => {
    const { user } = useAuth();
    const [isEditing, setIsEditing] = useState(false);
    const [isLoading, setIsLoading] = useState(false);
    const [formData, setFormData] = useState<ProfileFormData>({
        firstName: user?.firstName || '',
        lastName: user?.lastName || '',
        email: user?.email || ''
    });

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsLoading(true);

        try {
            // TODO: Appeler l'API pour mettre à jour le profil
            console.log('Mise à jour du profil:', formData);

            // Simuler un délai d'API
            await new Promise(resolve => setTimeout(resolve, 1000));

            setIsEditing(false);
        } catch (error) {
            console.error('Erreur lors de la mise à jour du profil:', error);
        } finally {
            setIsLoading(false);
        }
    };

    const handleCancel = () => {
        setFormData({
            firstName: user?.firstName || '',
            lastName: user?.lastName || '',
            email: user?.email || ''
        });
        setIsEditing(false);
    };

    const formatDate = (dateString: string) => {
        return new Date(dateString).toLocaleDateString('fr-FR', {
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        });
    };

    const getInitials = (username: string) => {
        return username.charAt(0).toUpperCase();
    };

    return (
        <MainLayout
            title="Mon Profil"
            subtitle="Gérez vos informations personnelles"
            actions={
                !isEditing && (
                    <Button
                        variant="primary"
                        onClick={() => setIsEditing(true)}
                        icon={<Edit3 className="h-5 w-5" />}
                    >
                        Modifier le profil
                    </Button>
                )
            }
        >
            <div className="max-w-2xl space-y-6">
                {/* Photo de profil et informations de base */}
                <Card variant="elevated">
                    <CardBody>
                        <div className="flex items-center gap-6">
                            {/* Avatar */}
                            <div className="w-24 h-24 bg-gradient-to-br from-primary-400 to-accent-500 rounded-full flex items-center justify-center">
                                <span className="text-3xl font-bold text-white">
                                    {user ? getInitials(user.username) : 'U'}
                                </span>
                            </div>

                            {/* Informations de base */}
                            <div className="flex-1">
                                <div className="flex items-center gap-3 mb-2">
                                    <h1 className="text-2xl font-bold text-neutral-900">
                                        {user?.username}
                                    </h1>
                                    <Badge
                                        variant={user?.role === 'ADMIN' ? 'warning' : 'primary'}
                                        size="sm"
                                    >
                                        {user?.role === 'ADMIN' ? 'Administrateur' : 'Utilisateur'}
                                    </Badge>
                                </div>

                                <div className="space-y-1 text-sm text-neutral-600">
                                    <div className="flex items-center gap-2">
                                        <Mail className="h-4 w-4" />
                                        <span>{user?.email}</span>
                                    </div>
                                    <div className="flex items-center gap-2">
                                        <Calendar className="h-4 w-4" />
                                        <span>Membre depuis le {user ? formatDate(user.createdAt) : 'N/A'}</span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </CardBody>
                </Card>

                {/* Formulaire d'édition du profil */}
                <Card variant="elevated">
                    <CardHeader>
                        <div className="flex items-center justify-between">
                            <h2 className="text-xl font-semibold text-neutral-900">
                                Informations personnelles
                            </h2>
                            {isEditing && (
                                <Button
                                    variant="ghost"
                                    size="sm"
                                    onClick={handleCancel}
                                    icon={<X className="h-4 w-4" />}
                                >
                                    Annuler
                                </Button>
                            )}
                        </div>
                    </CardHeader>

                    {isEditing ? (
                        <form onSubmit={handleSubmit}>
                            <CardBody className="space-y-6">
                                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                    <Input
                                        label="Prénom"
                                        value={formData.firstName}
                                        onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                                        placeholder="Votre prénom"
                                        fullWidth
                                    />

                                    <Input
                                        label="Nom"
                                        value={formData.lastName}
                                        onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                                        placeholder="Votre nom"
                                        fullWidth
                                    />
                                </div>

                                <Input
                                    label="Adresse email"
                                    type="email"
                                    value={formData.email}
                                    onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                                    placeholder="votre.email@exemple.com"
                                    fullWidth
                                    required
                                />
                            </CardBody>

                            <CardFooter>
                                <div className="flex justify-end gap-3 w-full">
                                    <Button
                                        type="button"
                                        variant="outline"
                                        onClick={handleCancel}
                                        disabled={isLoading}
                                    >
                                        Annuler
                                    </Button>
                                    <Button
                                        type="submit"
                                        variant="primary"
                                        isLoading={isLoading}
                                        icon={<Save className="h-4 w-4" />}
                                    >
                                        Enregistrer
                                    </Button>
                                </div>
                            </CardFooter>
                        </form>
                    ) : (
                        <CardBody>
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                                <div>
                                    <label className="block text-sm font-medium text-neutral-700 mb-1">
                                        Prénom
                                    </label>
                                    <p className="text-neutral-900">
                                        {user?.firstName || 'Non renseigné'}
                                    </p>
                                </div>

                                <div>
                                    <label className="block text-sm font-medium text-neutral-700 mb-1">
                                        Nom
                                    </label>
                                    <p className="text-neutral-900">
                                        {user?.lastName || 'Non renseigné'}
                                    </p>
                                </div>

                                <div className="md:col-span-2">
                                    <label className="block text-sm font-medium text-neutral-700 mb-1">
                                        Adresse email
                                    </label>
                                    <p className="text-neutral-900">{user?.email}</p>
                                </div>
                            </div>
                        </CardBody>
                    )}
                </Card>

                {/* Informations du compte */}
                <Card variant="elevated">
                    <CardHeader>
                        <h2 className="text-xl font-semibold text-neutral-900">
                            Informations du compte
                        </h2>
                    </CardHeader>
                    <CardBody>
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                            <div>
                                <label className="block text-sm font-medium text-neutral-700 mb-1">
                                    Nom d'utilisateur
                                </label>
                                <p className="text-neutral-900">{user?.username}</p>
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-neutral-700 mb-1">
                                    Rôle
                                </label>
                                <div className="flex items-center gap-2">
                                    <Shield className="h-4 w-4 text-neutral-500" />
                                    <span className="text-neutral-900">
                                        {user?.role === 'ADMIN' ? 'Administrateur' : 'Utilisateur'}
                                    </span>
                                </div>
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-neutral-700 mb-1">
                                    Date de création
                                </label>
                                <p className="text-neutral-900">
                                    {user ? formatDate(user.createdAt) : 'N/A'}
                                </p>
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-neutral-700 mb-1">
                                    ID utilisateur
                                </label>
                                <p className="text-neutral-900 font-mono text-sm">#{user?.id}</p>
                            </div>
                        </div>
                    </CardBody>
                </Card>

                {/* Actions dangereuses */}
                <Card variant="outlined" className="border-danger-200">
                    <CardHeader>
                        <h2 className="text-xl font-semibold text-danger-900">
                            Zone de danger
                        </h2>
                    </CardHeader>
                    <CardBody>
                        <div className="space-y-4">
                            <div>
                                <h3 className="font-medium text-danger-900 mb-1">
                                    Supprimer mon compte
                                </h3>
                                <p className="text-sm text-danger-600 mb-4">
                                    Cette action est irréversible. Toutes vos données seront définitivement supprimées.
                                </p>
                                <Button
                                    variant="danger"
                                    size="sm"
                                    onClick={() => {
                                        if (window.confirm('Êtes-vous sûr de vouloir supprimer votre compte ? Cette action est irréversible.')) {
                                            console.log('Suppression du compte demandée');
                                        }
                                    }}
                                >
                                    Supprimer mon compte
                                </Button>
                            </div>
                        </div>
                    </CardBody>
                </Card>
            </div>
        </MainLayout>
    );
};

export default Profile;

