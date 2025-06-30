import React from 'react';
import { useAuth } from '../contexts/AuthContext';
import MainLayout from '../components/templates/MainLayout';
import Card, { CardBody, CardHeader } from '../components/atoms/Card';
import { User } from 'lucide-react';

const Profile: React.FC = () => {
    const { user } = useAuth();

    if (!user) {
        return (
            <MainLayout title="Profil">
                <div className="text-center text-neutral-600">Chargement du profil...</div>
            </MainLayout>
        );
    }

    return (
        <MainLayout title="Mon profil">
            <div className="flex justify-center">
                <Card variant="elevated" padding="lg" className="max-w-xl w-full">
                    <CardHeader>
                        <div className="flex flex-col items-center gap-4 text-center">
                            <div className="w-20 h-20 bg-primary-100 text-primary-600 rounded-full flex items-center justify-center">
                                <User className="h-10 w-10" />
                            </div>
                            <h2 className="text-xl font-semibold text-neutral-900">
                                {user.firstName} {user.lastName}
                            </h2>
                        </div>
                    </CardHeader>
                    <CardBody className="mt-6 space-y-2">
                        <div className="flex">
                            <span className="font-semibold text-neutral-700 mr-2">Nom d'utilisateur :</span>
                            <span className="text-neutral-800">{user.username}</span>
                        </div>
                        <div className="flex">
                            <span className="font-semibold text-neutral-700 mr-2">Prénom :</span>
                            <span className="text-neutral-800">{user.firstName}</span>
                        </div>
                        <div className="flex">
                            <span className="font-semibold text-neutral-700 mr-2">Nom :</span>
                            <span className="text-neutral-800">{user.lastName}</span>
                        </div>
                        <div className="flex">
                            <span className="font-semibold text-neutral-700 mr-2">Email :</span>
                            <span className="text-neutral-800">{user.email}</span>
                        </div>
                    </CardBody>

                </Card>
            </div>
        </MainLayout>
    );
};

export default Profile;
