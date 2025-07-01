// src/pages/Dashboard.tsx - Version finale corrigée
import React, { useEffect, useState } from 'react';
import { Plus, Target, TrendingUp, Calendar, Trophy, RefreshCw } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import MainLayout from '../components/templates/MainLayout';
import Card, { CardHeader, CardBody } from '../components/atoms/Card';
import Button from '../components/atoms/Button';
import Badge from '../components/atoms/Badge';
import HabitCard from '../components/molecules/HabitCard';
import { useAuth } from '../contexts/AuthContext';
import { habitService, Habit } from '../services/habitService';
import { statisticsService } from '../services/statisticsService';
import { progressService } from '../services/progressService';
import { achievementService } from '../services/achievementService';

interface DashboardStats {
    totalHabits: number;
    activeHabits: number;
    completedToday: number;
    currentStreak: number;
    weeklyProgress: number;
}

interface DashboardData {
    habits: Habit[];
    stats: DashboardStats;
    recentAchievements: any[];
    todayProgress: any[];
}

interface StatCard {
    title: string;
    value: number | string;
    total?: number;
    icon: React.ComponentType<any>;
    color: 'primary' | 'success' | 'accent' | 'secondary';
    description: string;
}

const Dashboard: React.FC = () => {
    const { user } = useAuth();
    const navigate = useNavigate();

    // États locaux
    const [dashboardData, setDashboardData] = useState<DashboardData | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [isRefreshing, setIsRefreshing] = useState(false);
    const [progressLoading, setProgressLoading] = useState<{ [habitId: number]: boolean }>({});

    useEffect(() => {
        if (user?.id) {
            loadDashboardData();
        }
    }, [user?.id]);

    // Fonction utilitaire pour gérer les classes CSS des couleurs
    const getColorClasses = (color: StatCard['color'], type: 'bg' | 'text' | 'bg-light') => {
        const colorMap = {
            'primary': {
                'bg': 'bg-primary-500',
                'text': 'text-primary-600',
                'bg-light': 'bg-primary-100'
            },
            'success': {
                'bg': 'bg-success-500',
                'text': 'text-success-600',
                'bg-light': 'bg-success-100'
            },
            'accent': {
                'bg': 'bg-accent-500',
                'text': 'text-accent-600',
                'bg-light': 'bg-accent-100'
            },
            'secondary': {
                'bg': 'bg-secondary-500',
                'text': 'text-secondary-600',
                'bg-light': 'bg-secondary-100'
            }
        };

        return colorMap[color][type];
    };

    const loadDashboardData = async (showRefreshing = false) => {
        if (!user?.id) return;

        try {
            if (showRefreshing) {
                setIsRefreshing(true);
            } else {
                setIsLoading(true);
            }
            setError(null);

            // Charger toutes les données en parallèle
            const [
                habitsResponse,
                todayProgress,
                recentAchievements,
                userStatistics
            ] = await Promise.allSettled([
                habitService.getHabits({ page: 0, size: 6, active: true }),
                progressService.getTodayProgress(user.id),
                achievementService.getRecentAchievements(user.id, 7),
                statisticsService.getUserStatistics(user.id, 30).catch(() => null)
            ]);

            // Traiter les résultats
            const habits = habitsResponse.status === 'fulfilled' ? habitsResponse.value.content || [] : [];
            const todayProgressData = todayProgress.status === 'fulfilled' ? todayProgress.value || [] : [];
            const achievementsData = recentAchievements.status === 'fulfilled' ? recentAchievements.value || [] : [];
            const statsData = userStatistics.status === 'fulfilled' ? userStatistics.value : null;

            // Calculer les statistiques
            let stats: DashboardStats;
            if (statsData) {
                stats = {
                    totalHabits: statsData.habitCount || 0,
                    activeHabits: statsData.activeHabits || 0,
                    completedToday: todayProgressData.length,
                    currentStreak: statsData.streakCount || 0,
                    weeklyProgress: Math.round(statsData.completionRate || 0)
                };
            } else {
                // Fallback : calculer les stats à partir des habitudes
                const totalHabits = habits.length;
                const activeHabits = habits.filter(h => h.isActive).length;

                stats = {
                    totalHabits,
                    activeHabits,
                    completedToday: todayProgressData.length,
                    currentStreak: 0,
                    weeklyProgress: activeHabits > 0 ? Math.round((todayProgressData.length / activeHabits) * 100) : 0
                };
            }

            setDashboardData({
                habits,
                stats,
                recentAchievements: achievementsData,
                todayProgress: todayProgressData
            });

        } catch (error: any) {
            console.error('Erreur lors du chargement du dashboard:', error);
            setError(error.message || 'Impossible de charger les données du dashboard');
        } finally {
            setIsLoading(false);
            setIsRefreshing(false);
        }
    };

    const handleHabitProgress = async (habit: Habit) => {
        if (!user?.id) return;

        try {
            setProgressLoading(prev => ({ ...prev, [habit.id]: true }));

            const today = new Date().toISOString().split('T')[0];
            const defaultValue = habit.targetValue || 1;

            await progressService.createProgress(habit.id, {
                date: today,
                value: defaultValue,
                note: `Progrès ajouté depuis le dashboard`
            });

            // Vérifier les nouveaux achievements
            const achievementCheck = await achievementService.checkAchievements({
                userId: user.id,
                habitId: habit.id,
                triggerType: 'PROGRESS_ADDED'
            });

            // Notifier les nouveaux badges
            if (achievementCheck.newAchievementsUnlocked > 0) {
                console.log('🎉 Nouveaux badges débloqués:', achievementCheck.newAchievements);
                // Ici on pourrait déclencher une notification
            }

            // Recharger les données
            await loadDashboardData(true);

        } catch (error: any) {
            console.error('Erreur lors de l\'ajout du progrès:', error);
            setError(`Impossible d'ajouter le progrès pour "${habit.title}"`);
        } finally {
            setProgressLoading(prev => ({ ...prev, [habit.id]: false }));
        }
    };

    const handleRefresh = () => {
        loadDashboardData(true);
    };

    const getGreeting = () => {
        const hour = new Date().getHours();
        if (hour < 12) return 'Bonjour';
        if (hour < 18) return 'Bon après-midi';
        return 'Bonsoir';
    };

    const getMotivationalMessage = () => {
        if (!dashboardData) return 'Chaque petit pas compte ! 🚀';

        const { stats, recentAchievements } = dashboardData;

        if (stats.completedToday >= stats.activeHabits && stats.activeHabits > 0) {
            return 'Tous vos objectifs du jour sont atteints ! 🎉';
        }
        if (stats.currentStreak >= 7) {
            return `Incroyable série de ${stats.currentStreak} jours ! 🔥`;
        }
        if (stats.weeklyProgress >= 80) {
            return 'Excellente semaine, continuez ainsi ! 💪';
        }
        if (recentAchievements.length > 0) {
            return 'Félicitations pour vos nouveaux badges ! 🏆';
        }
        return 'Chaque petit pas compte ! 🚀';
    };

    const statsCards: StatCard[] = [
        {
            title: 'Habitudes actives',
            value: dashboardData?.stats.activeHabits || 0,
            total: dashboardData?.stats.totalHabits || 0,
            icon: Target,
            color: 'primary',
            description: 'habitudes en cours',
        },
        {
            title: 'Complétées aujourd\'hui',
            value: dashboardData?.stats.completedToday || 0,
            total: dashboardData?.stats.activeHabits || 0,
            icon: Calendar,
            color: 'success',
            description: 'objectifs atteints',
        },
        {
            title: 'Série actuelle',
            value: dashboardData?.stats.currentStreak || 0,
            icon: TrendingUp,
            color: 'accent',
            description: 'jours consécutifs',
        },
        {
            title: 'Progression hebdo',
            value: `${dashboardData?.stats.weeklyProgress || 0}%`,
            icon: Trophy,
            color: 'secondary',
            description: 'de réussite',
        },
    ];

    // Gestion des erreurs
    if (error && !dashboardData) {
        return (
            <MainLayout title="Tableau de bord">
                <Card variant="outlined" className="border-danger-200">
                    <CardBody className="text-center py-12">
                        <div className="text-6xl mb-4">⚠️</div>
                        <h3 className="text-lg font-medium text-danger-900 mb-2">
                            Erreur de chargement
                        </h3>
                        <p className="text-danger-600 mb-6">{error}</p>
                        <div className="flex gap-3 justify-center">
                            <Button
                                variant="primary"
                                onClick={() => loadDashboardData()}
                                isLoading={isLoading}
                                icon={<RefreshCw className="h-4 w-4" />}
                            >
                                Réessayer
                            </Button>
                            <Button
                                variant="outline"
                                onClick={() => navigate('/habits')}
                            >
                                Aller aux habitudes
                            </Button>
                        </div>
                    </CardBody>
                </Card>
            </MainLayout>
        );
    }

    return (
        <MainLayout
            title={`${getGreeting()}, ${user?.firstName || user?.username} !`}
            subtitle={getMotivationalMessage()}
            actions={
                <div className="flex gap-3">
                    <Button
                        variant="ghost"
                        onClick={handleRefresh}
                        isLoading={isRefreshing}
                        icon={<RefreshCw className="h-4 w-4" />}
                    >
                        {!isRefreshing && 'Actualiser'}
                    </Button>
                    <Button
                        variant="primary"
                        onClick={() => navigate('/habits')}
                        icon={<Plus className="h-5 w-5" />}
                    >
                        Nouvelle habitude
                    </Button>
                </div>
            }
        >
            <div className="space-y-8">
                {/* Affichage des erreurs non critiques */}
                {error && dashboardData && (
                    <div className="bg-warning-50 border border-warning-200 rounded-xl p-4 flex items-center justify-between">
                        <p className="text-warning-700 text-sm">{error}</p>
                        <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => setError(null)}
                            icon={<RefreshCw className="h-4 w-4" />}
                        >
                            Ignorer
                        </Button>
                    </div>
                )}

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
                                                {isLoading ? (
                                                    <div className="w-8 h-8 bg-neutral-200 rounded animate-pulse"></div>
                                                ) : (
                                                    stat.value
                                                )}
                                            </span>
                                            {stat.total !== undefined && !isLoading && (
                                                <span className="text-sm text-neutral-500">
                                                    / {stat.total}
                                                </span>
                                            )}
                                        </div>
                                        <p className="text-xs text-neutral-500 mt-1">
                                            {stat.description}
                                        </p>
                                    </div>
                                    <div className={`w-12 h-12 rounded-2xl flex items-center justify-center ${getColorClasses(stat.color, 'bg-light')}`}>
                                        <stat.icon className={`h-6 w-6 ${getColorClasses(stat.color, 'text')}`} />
                                    </div>
                                </div>

                                {/* Barre de progression pour les stats avec total */}
                                {stat.total !== undefined && typeof stat.value === 'number' && !isLoading && (
                                    <div className="mt-4">
                                        <div className="w-full bg-neutral-200 rounded-full h-2">
                                            <div
                                                className={`h-2 rounded-full transition-all duration-300 ${getColorClasses(stat.color, 'bg')}`}
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

                {/* Badges récents */}
                {dashboardData?.recentAchievements && dashboardData.recentAchievements.length > 0 && (
                    <Card variant="filled" className="bg-gradient-to-r from-primary-50 to-accent-50 border-primary-200">
                        <CardBody>
                            <div className="flex items-center gap-4">
                                <div className="w-16 h-16 bg-gradient-to-br from-primary-500 to-accent-500 rounded-2xl flex items-center justify-center">
                                    <Trophy className="h-8 w-8 text-white" />
                                </div>
                                <div className="flex-1">
                                    <h3 className="font-semibold text-lg text-neutral-900 mb-1">
                                        Nouveaux badges débloqués ! 🎉
                                    </h3>
                                    <p className="text-neutral-600 mb-2">
                                        Vous avez obtenu {dashboardData.recentAchievements.length} nouveau{dashboardData.recentAchievements.length > 1 ? 'x' : ''} badge{dashboardData.recentAchievements.length > 1 ? 's' : ''} cette semaine.
                                    </p>
                                    <div className="flex flex-wrap gap-2">
                                        {dashboardData.recentAchievements.slice(0, 3).map((achievement) => (
                                            <Badge key={achievement.id} variant="primary" size="sm">
                                                {achievement.icon} {achievement.name}
                                            </Badge>
                                        ))}
                                        {dashboardData.recentAchievements.length > 3 && (
                                            <Badge variant="neutral" size="sm">
                                                +{dashboardData.recentAchievements.length - 3} autre{dashboardData.recentAchievements.length - 3 > 1 ? 's' : ''}
                                            </Badge>
                                        )}
                                    </div>
                                </div>
                                <Button variant="outline" onClick={() => navigate('/achievements')}>
                                    Voir tous mes badges
                                </Button>
                            </div>
                        </CardBody>
                    </Card>
                )}

                {/* Message de progression */}
                {dashboardData?.stats.currentStreak && dashboardData.stats.currentStreak > 0 && (
                    <Card variant="filled" className="bg-gradient-to-r from-success-50 to-primary-50 border-success-200">
                        <CardBody>
                            <div className="flex items-center gap-4">
                                <div className="w-16 h-16 bg-gradient-to-br from-success-500 to-primary-500 rounded-2xl flex items-center justify-center">
                                    <TrendingUp className="h-8 w-8 text-white" />
                                </div>
                                <div className="flex-1">
                                    <h3 className="font-semibold text-lg text-neutral-900 mb-1">
                                        Excellente série ! 🔥
                                    </h3>
                                    <p className="text-neutral-600">
                                        Vous avez maintenu vos habitudes pendant {dashboardData.stats.currentStreak} jour{dashboardData.stats.currentStreak > 1 ? 's' : ''} consécutif{dashboardData.stats.currentStreak > 1 ? 's' : ''}.
                                        Continuez ainsi pour atteindre de nouveaux records !
                                    </p>
                                </div>
                                <Button variant="outline" onClick={() => navigate('/progress')}>
                                    Voir ma progression
                                </Button>
                            </div>
                        </CardBody>
                    </Card>
                )}

                {/* Habitudes récentes */}
                <div>
                    <div className="flex items-center justify-between mb-6">
                        <div>
                            <h2 className="text-xl font-semibold text-neutral-900 mb-1">
                                Vos habitudes actives
                            </h2>
                            <p className="text-neutral-600">
                                {isLoading
                                    ? 'Chargement...'
                                    : !dashboardData?.habits || dashboardData.habits.length === 0
                                        ? 'Aucune habitude active pour le moment'
                                        : `${dashboardData.habits.length} habitude${dashboardData.habits.length > 1 ? 's' : ''} active${dashboardData.habits.length > 1 ? 's' : ''}`
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
                    ) : !dashboardData?.habits || dashboardData.habits.length === 0 ? (
                        <Card variant="outlined" className="border-dashed">
                            <CardBody className="text-center py-12">
                                <Target className="h-16 w-16 text-neutral-300 mx-auto mb-4" />
                                <h3 className="text-lg font-medium text-neutral-900 mb-2">
                                    Aucune habitude active
                                </h3>
                                <p className="text-neutral-600 mb-6">
                                    Commencez votre parcours en créant votre première habitude !
                                </p>
                                <Button
                                    variant="primary"
                                    onClick={() => navigate('/habits')}
                                    icon={<Plus className="h-5 w-5" />}
                                >
                                    Créer ma première habitude
                                </Button>
                            </CardBody>
                        </Card>
                    ) : (
                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                            {dashboardData.habits.map((habit) => (
                                <div key={habit.id} className="relative">
                                    <HabitCard
                                        habit={habit}
                                        onProgress={handleHabitProgress}
                                        onEdit={() => navigate('/habits')}
                                    />
                                    {progressLoading[habit.id] && (
                                        <div className="absolute inset-0 bg-white bg-opacity-75 rounded-2xl flex items-center justify-center">
                                            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
                                        </div>
                                    )}
                                </div>
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
                                onClick={() => navigate('/habits')}
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