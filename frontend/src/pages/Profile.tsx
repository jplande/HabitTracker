import React from 'react';
import { useAuth } from '../contexts/AuthContext';
import MainLayout from '../components/templates/MainLayout';
import Card, { CardBody, CardHeader } from '../components/atoms/Card';
import { User } from 'lucide-react';
import Button from '../components/atoms/Button';
import { useNavigate } from 'react-router-dom';
import { habitService, Habit } from '../services/habitService';
import { useState, useEffect } from 'react';
const Profile: React.FC = () => {
    const { user } = useAuth();
    const navigate = useNavigate();



    const [habits, setHabits] = useState<Habit[]>([]);
    const [loadingHabits, setLoadingHabits] = useState(true);

    useEffect(() => {
        const fetchHabits = async () => {
            try {
                const response = await habitService.getHabits({ page: 0, size: 100 });
                setHabits(response.content || []);
            } catch (error) {
                console.error('Erreur lors du chargement des habitudes :', error);
            } finally {
                setLoadingHabits(false);
            }
        };

        fetchHabits();
    }, []);

    const globalSuccessRate = habits.length > 0
        ? Math.round(
            habits.reduce((acc, habit) => acc + (habit.averageCompletion ?? 0), 0) / habits.length
        )
        : 0;

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

                        {/* Statistiques  */}
                        <div className="pt-4 border-t space-y-1">
                            {loadingHabits ? (
                                <div className="text-sm text-neutral-500">Chargement des habitudes...</div>
                            ) : habits.length === 0 ? (
                                <div className="text-sm italic text-neutral-500">Aucune habitude suivie pour l'instant.</div>
                            ) : (
                                <>
                                    <div className="flex">
                                        <span className="font-semibold mr-2">Nombre d'habitudes :</span>
                                        <span>{habits.length}</span>
                                    </div>
                                    <div className="flex">
                                        <span className="font-semibold mr-2">Taux de réussite global :</span>
                                        <span>{globalSuccessRate}%</span>
                                    </div>
                                </>
                            )}
                        </div>

                        {/* Actions */}
                        <div className="flex gap-4 pt-4 border-t">
                            <Button onClick={() => navigate('/habits')}>Mes habitudes</Button>
                            <Button variant="ghost" onClick={() => navigate('/settings')}>Paramètres</Button>
                        </div>
                    </CardBody>

                </Card>
            </div>
        </MainLayout>
    );
};

export default Profile;
