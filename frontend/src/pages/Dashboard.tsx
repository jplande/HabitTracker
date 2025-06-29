import React, { useState, useEffect } from 'react';
import { Plus, Target, TrendingUp, Calendar, Trophy } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import MainLayout from '../components/templates/MainLayout';
import Card, { CardHeader, CardBody } from '../components/atoms/Card';
import Button from '../components/atoms/Button';
import Badge from '../components/atoms/Badge';
import HabitCard from '../components/molecules/HabitCard';
import { useAuth } from '../contexts/AuthContext';
import { habitService } from '../services/habitService';
import { Habit } from '../types';

interface DashboardStats {
    totalHabits: number;
    activeHabits: number;
    completedToday: number;
    currentStreak: number;
    weeklyProgress: number;
}

const Dashboard: React.FC = () => {
    const { user } = useAuth();
    const navigate = useNavigate();

    const [stats, setStats] = useState<DashboardStats>({
        totalHabits: 0,
        activeHabits: 0,
        completedToday: 0,
        currentStreak: 0,
        weeklyProgress: 0,
    });

    const [recentHabits, setRecentHabits] = useState<Habit[]>([]);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        loadDashboardData();
    }, []);

    const loadDashboardData = async () => {
        try {
            setIsLoading(true);

            // Charger les habitudes récentes avec gestion d'erreur
            try {
                const habitsResponse = await habitService.getHabits({
                    page: 0,
                    size: 6,
                    active: true
                });

                // Vérifier que la réponse est valide
                if (habitsResponse && habitsResponse.content && Array.isArray(habitsResponse.content)) {
                    setRecentHabits(habitsResponse.content);

                    // Calculer les statistiques
                    const totalHabits = habitsResponse.totalElements || 0;
                    const activeHabits = habitsResponse.content.filter(h => h.isActive).length;

                    setStats({
                        totalHabits,
                        activeHabits,
                        completedToday: Math.floor(activeHabits * 0.7), // Simulation
                        currentStreak: 12, // Simulation
                        weeklyProgress: 85, // Simulation
                    });
                } else {
                    console.warn('Réponse API invalide:', habitsResponse);
                    // Valeurs par défaut si l'API ne répond pas correctement
                    setRecentHabits([]);
                    setStats({
                        totalHabits: 0,
                        activeHabits: 0,
                        completedToday: 0,
                        currentStreak: 0,
                        weeklyProgress: 0,
                    });
                }
            } catch (apiError) {
                console.warn('API non disponible, utilisation de données factices:', apiError);

                // Données factices pour le développement
                const fakeHabits = [
                    {
                        id: 1,
                        userId: 1,
                        title: "Course à pied",
                        description: "30 minutes de course quotidienne",
                        category: "SPORT" as const,
                        unit: "minutes",
                        frequency: "DAILY" as const,
                        targetValue: 30,
                        isActive: true,
                        createdAt: "2024-01-01T00:00:00Z",
                        currentStreak: 7,
                        averageCompletion: 85
                    },
                    {
                        id: 2,
                        userId: 1,
                        title: "Lecture",
                        description: "Lire 20 pages par jour",
                        category: "EDUCATION" as const,
                        unit: "pages",
                        frequency: "DAILY" as const,
                        targetValue: 20,
                        isActive: true,
                        createdAt: "2024-01-01T00:00:00Z",
                        currentStreak: 12,
                        averageCompletion: 90
                    }
                ];

                setRecentHabits(fakeHabits);
                setStats({
                    totalHabits: fakeHabits.length,
                    activeHabits: fakeHabits.filter(h => h.isActive).length,
                    completedToday: 1,
                    currentStreak: 12,
                    weeklyProgress: 85,
                });
            }

        } catch (error) {
            console.error('Erreur lors du chargement du dashboard:', error);
            // Valeurs par défaut en cas d'erreur
            setRecentHabits([]);
            setStats({
                totalHabits: 0,
                activeHabits: 0,
                completedToday: 0,
                currentStreak: 0,
                weeklyProgress: 0,
            });
        } finally {
            setIsLoading(false);
        }
    };

    const statsCards = [
        {
            title: 'Habitudes actives',
            value: stats.activeHabits,
            total: stats.totalHabits,
            icon: Target,
            color: 'primary',
            description: 'habitudes en cours',
        },
        {
            title: 'Complétées aujourd\'hui',
            value: stats.completedToday,
            total: stats.activeHabits,
            icon: Calendar,
            color: 'success',
            description: 'objectifs atteints',
        },
        {
            title: 'Série actuelle',
            value: stats.currentStreak,
            icon: TrendingUp,
            color: 'accent',
            description: 'jours consécutifs',
        },
        {
            title: 'Progression hebdo',
            value: `${stats.weeklyProgress}%`,
            icon: Trophy,
            color: 'secondary',
            description: 'de réussite',
        },
    ];

    const getGreeting = () => {
        const hour = new Date().getHours();
        if (hour < 12) return 'Bonjour';
        if (hour < 18) return 'Bon après-midi';
        return 'Bonsoir';
    };

    const getMotivationalMessage = () => {
        const messages = [
            'Continuez sur cette belle lancée ! 🚀',
            'Chaque petit pas compte 💪',
            'Vous êtes sur la bonne voie ! ⭐',
            'Excellent travail aujourd\'hui ! 🎯',
            'Votre persévérance paie ! 🌟',
        ];
        return messages[Math.floor(Math.random() * messages.length)];
    };

    return (
        <MainLayout
            title={`${getGreeting()}, ${user?.firstName || user?.username} !`}
            subtitle={getMotivationalMessage()}
            actions={
                <Button
                    variant="primary"
                    onClick={() => navigate('/habits/new')}
                    icon={<Plus className="h-5 w-5" />}
                >
                    Nouvelle habitude
                </Button>
            }
        >
            <div className="space-y-8">
                {/* Statistiques principales */}
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                    {statsCards.map((stat, index) => (
                        <Card key={index} variant="elevated" hover>
                            <CardBody>
                                <div className="flex items-center justify-between">
                                    <div>
                                        <p className="text-sm font-medium text-neutral-600 mb-1">
                                            {stat.title}
                                        </p>
                                        <div className="flex items-baseline gap-2">
                      <span className="text-2xl font-bold text-neutral-900">
                        {stat.value}
                      </span>
                                            {stat.total && (
                                                <span className="text-sm text-neutral-500">
                          / {stat.total}
                        </span>
                                            )}
                                        </div>
                                        <p className="text-xs text-neutral-500 mt-1">
                                            {stat.description}
                                        </p>
                                    </div>
                                    <div className={`
                    w-12 h-12 rounded-2xl flex items-center justify-center
                    ${stat.color === 'primary' ? 'bg-primary-100' : ''}
                    ${stat.color === 'success' ? 'bg-success-100' : ''}
                    ${stat.color === 'accent' ? 'bg-accent-100' : ''}
                    ${stat.color === 'secondary' ? 'bg-secondary-100' : ''}
                  `}>
                                        <stat.icon className={`
                      h-6 w-6
                      ${stat.color === 'primary' ? 'text-primary-600' : ''}
                      ${stat.color === 'success' ? 'text-success-600' : ''}
                      ${stat.color === 'accent' ? 'text-accent-600' : ''}
                      ${stat.color === 'secondary' ? 'text-secondary-600' : ''}
                    `} />
                                    </div>
                                </div>

                                {/* Barre de progression pour les stats avec total */}
                                {stat.total && typeof stat.value === 'number' && (
                                    <div className="mt-4">
                                        <div className="w-full bg-neutral-200 rounded-full h-2">
                                            <div
                                                className={`
                          h-2 rounded-full transition-all duration-300
                          ${stat.color === 'primary' ? 'bg-primary-500' : ''}
                          ${stat.color === 'success' ? 'bg-success-500' : ''}
                          ${stat.color === 'accent' ? 'bg-accent-500' : ''}
                          ${stat.color === 'secondary' ? 'bg-secondary-500' : ''}
                        `}
                                                style={{
                                                    width: `${Math.min((stat.value / stat.total) * 100, 100)}%`
                                                }}
                                            />
                                        </div>
                                    </div>
                                )}
                            </CardBody>
                        </Card>
                    ))}
                </div>

                {/* Message de progression */}
                <Card variant="filled" className="bg-gradient-to-r from-primary-50 to-accent-50 border-primary-200">
                    <CardBody>
                        <div className="flex items-center gap-4">
                            <div className="w-16 h-16 bg-gradient-to-br from-primary-500 to-accent-500 rounded-2xl flex items-center justify-center">
                                <Trophy className="h-8 w-8 text-white" />
                            </div>
                            <div className="flex-1">
                                <h3 className="font-semibold text-lg text-neutral-900 mb-1">
                                    Excellente progression ! 🎉
                                </h3>
                                <p className="text-neutral-600">
                                    Vous avez maintenu {stats.currentStreak} jours de suite.
                                    Continuez ainsi pour débloquer de nouveaux badges !
                                </p>
                            </div>
                            <Button variant="outline" onClick={() => navigate('/achievements')}>
                                Voir mes badges
                            </Button>
                        </div>
                    </CardBody>
                </Card>

                {/* Habitudes récentes */}
                <div>
                    <div className="flex items-center justify-between mb-6">
                        <div>
                            <h2 className="text-xl font-semibold text-neutral-900 mb-1">
                                Vos habitudes
                            </h2>
                            <p className="text-neutral-600">
                                {(!recentHabits || recentHabits.length === 0)
                                    ? 'Aucune habitude créée pour le moment'
                                    : `${recentHabits.length} habitude${recentHabits.length > 1 ? 's' : ''} active${recentHabits.length > 1 ? 's' : ''}`
                                }
                            </p>
                        </div>
                        <Button
                            variant="outline"
                            onClick={() => navigate('/habits')}
                        >
                            Voir toutes
                        </Button>
                    </div>

                    {isLoading ? (
                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                            {[...Array(6)].map((_, i) => (
                                <Card key={i} variant="elevated" className="animate-pulse">
                                    <CardBody>
                                        <div className="h-4 bg-neutral-200 rounded mb-3"></div>
                                        <div className="h-3 bg-neutral-200 rounded mb-2"></div>
                                        <div className="h-3 bg-neutral-200 rounded w-2/3"></div>
                                    </CardBody>
                                </Card>
                            ))}
                        </div>
                    ) : (!recentHabits || recentHabits.length === 0) ? (
                        <Card variant="outlined" className="border-dashed">
                            <CardBody className="text-center py-12">
                                <Target className="h-16 w-16 text-neutral-300 mx-auto mb-4" />
                                <h3 className="text-lg font-medium text-neutral-900 mb-2">
                                    Aucune habitude créée
                                </h3>
                                <p className="text-neutral-600 mb-6">
                                    Commencez votre parcours en créant votre première habitude !
                                </p>
                                <Button
                                    variant="primary"
                                    onClick={() => navigate('/habits/new')}
                                    icon={<Plus className="h-5 w-5" />}
                                >
                                    Créer ma première habitude
                                </Button>
                            </CardBody>
                        </Card>
                    ) : (
                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                            {recentHabits && recentHabits.map((habit) => (
                                <HabitCard
                                    key={habit.id}
                                    habit={habit}
                                    onProgress={() => navigate(`/habits/${habit.id}/progress`)}
                                    onEdit={() => navigate(`/habits/${habit.id}/edit`)}
                                />
                            ))}
                        </div>
                    )}
                </div>

                {/* Actions rapides */}
                <Card variant="elevated">
                    <CardHeader>
                        <h3 className="text-lg font-semibold text-neutral-900">
                            Actions rapides
                        </h3>
                    </CardHeader>
                    <CardBody>
                        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                            <Button
                                variant="outline"
                                size="lg"
                                onClick={() => navigate('/habits/new')}
                                className="justify-start"
                                icon={<Plus className="h-5 w-5" />}
                            >
                                Nouvelle habitude
                            </Button>
                            <Button
                                variant="outline"
                                size="lg"
                                onClick={() => navigate('/progress')}
                                className="justify-start"
                                icon={<TrendingUp className="h-5 w-5" />}
                            >
                                Voir mes progrès
                            </Button>
                            <Button
                                variant="outline"
                                size="lg"
                                onClick={() => navigate('/achievements')}
                                className="justify-start"
                                icon={<Trophy className="h-5 w-5" />}
                            >
                                Mes badges
                            </Button>
                        </div>
                    </CardBody>
                </Card>
            </div>
        </MainLayout>
    );
};

export default Dashboard;