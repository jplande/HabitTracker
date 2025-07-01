// src/pages/Progress.tsx
import React, { useState, useEffect } from 'react';
import { Calendar, TrendingUp, Target, BarChart3, Filter, Plus, Edit, Trash2 } from 'lucide-react';
import MainLayout from '../components/templates/MainLayout';
import Card, { CardHeader, CardBody, CardFooter } from '../components/atoms/Card';
import Button from '../components/atoms/Button';
import Input from '../components/atoms/Input';
import Badge from '../components/atoms/Badge';
import { useAuth } from '../contexts/AuthContext';
import { habitService } from '../services/habitService';

import { progressService } from '../services/progressService';

interface LocalProgress {
    id: number;
    userId: number;
    habitId: number;
    date: string;
    value: number;
    note?: string;
    createdAt: string;
    habitTitle?: string;
    habitUnit?: string;
    habitTarget?: number;
    completionPercentage?: number;
    targetReached?: boolean;
}

interface LocalProgressStats {
    habitId: number;
    habitTitle: string;
    habitUnit: string;
    habitTarget?: number;
    startDate: string;
    endDate: string;
    totalDays: number;
    totalEntries: number;
    consecutiveDays: number;
    completionRate: number;
    totalValue: number;
    averageValue: number;
    maxValue: number;
    minValue: number;
    lastValue: number;
    progressTrend: number;
    improvementDetected: boolean;
    daysTargetReached: number;
    targetReachRate: number;
    consistencyScore: number;
    lastEntryDate: string;
    daysSinceLastEntry: number;
}

interface LocalHabit {
    id: number;
    userId: number;
    title: string;
    description?: string;
    category: any;
    unit: string;
    frequency: any;
    targetValue?: number;
    isActive: boolean;
    createdAt: string;
    totalProgress?: number;
    currentStreak?: number;
    averageCompletion?: number;
    lastProgressDate?: string;
}

interface ProgressFormData {
    habitId: number;
    date: string;
    value: number;
    note: string;
}

const Progress: React.FC = () => {
    const { user } = useAuth();

    // État principal
    const [habits, setHabits] = useState<LocalHabit[]>([]);
    const [progressList, setProgressList] = useState<LocalProgress[]>([]);
    const [selectedHabit, setSelectedHabit] = useState<LocalHabit | null>(null);
    const [progressStats, setProgressStats] = useState<LocalProgressStats | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    // États pour les formulaires et modals
    const [showAddProgress, setShowAddProgress] = useState(false);
    const [editingProgress, setEditingProgress] = useState<LocalProgress | null>(null);
    const [formData, setFormData] = useState<ProgressFormData>({
        habitId: 0,
        date: new Date().toISOString().split('T')[0],
        value: 0,
        note: ''
    });

    // États pour les filtres
    const [dateRange, setDateRange] = useState(30);
    const [viewMode, setViewMode] = useState<'list' | 'chart'>('list');

    useEffect(() => {
        if (user?.id) {
            loadData();
        }
    }, [user?.id, dateRange]);

    useEffect(() => {
        if (selectedHabit) {
            loadHabitProgress();
        }
    }, [selectedHabit, dateRange]);

    const loadData = async () => {
        try {
            setIsLoading(true);
            setError(null);

            // Charger les habitudes actives
            const habitsResponse = await habitService.getHabits({
                page: 0,
                size: 100,
                active: true
            });

            setHabits(habitsResponse.content || []);

            // Sélectionner la première habitude par défaut
            if ((habitsResponse.content || []).length > 0 && !selectedHabit) {
                setSelectedHabit((habitsResponse.content || [])[0]);
            }

        } catch (error: any) {
            console.error('Erreur lors du chargement des données:', error);
            setError(error.message || 'Erreur lors du chargement des données');
        } finally {
            setIsLoading(false);
        }
    };

    const loadHabitProgress = async () => {
        if (!selectedHabit) return;

        try {
            setError(null);

            // Charger les progrès de l'habitude
            const progressResponse = await progressService.getHabitProgress(selectedHabit.id, {
                page: 0,
                size: 100
            });

            setProgressList(progressResponse.content || []);

            // Essayer de charger les statistiques, sinon calculer côté client
            try {
                const statsResponse = await progressService.getHabitStatistics(selectedHabit.id, dateRange);
                setProgressStats(statsResponse);
            } catch (statsError) {
                console.warn('Impossible de charger les statistiques du serveur, calcul côté client:', statsError);
                // Fallback : calculer les stats côté client
                const clientStats = progressService.calculateProgressStats
                    ? progressService.calculateProgressStats(progressResponse.content || [], selectedHabit)
                    : null;
                setProgressStats(clientStats);
            }

        } catch (error: any) {
            console.error('Erreur lors du chargement des progrès:', error);
            setError(error.message || 'Erreur lors du chargement des progrès');
        }
    };

    const handleAddProgress = async (e: React.FormEvent) => {
        e.preventDefault();

        try {
            await progressService.createProgress(formData.habitId, {
                date: formData.date,
                value: formData.value,
                note: formData.note
            });

            // Recharger les données
            await loadHabitProgress();

            // Réinitialiser le formulaire
            setFormData({
                habitId: selectedHabit?.id || 0,
                date: new Date().toISOString().split('T')[0],
                value: 0,
                note: ''
            });
            setShowAddProgress(false);

        } catch (error: any) {
            console.error('Erreur lors de l\'ajout du progrès:', error);
            setError(error.message || 'Erreur lors de l\'ajout du progrès');
        }
    };

    const handleEditProgress = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!editingProgress) return;

        try {
            await progressService.updateProgress(editingProgress.id, {
                date: formData.date,
                value: formData.value,
                note: formData.note
            });

            await loadHabitProgress();
            setEditingProgress(null);
            setFormData({
                habitId: selectedHabit?.id || 0,
                date: new Date().toISOString().split('T')[0],
                value: 0,
                note: ''
            });

        } catch (error: any) {
            console.error('Erreur lors de la modification du progrès:', error);
            setError(error.message || 'Erreur lors de la modification du progrès');
        }
    };

    const handleDeleteProgress = async (progressId: number) => {
        if (!window.confirm('Êtes-vous sûr de vouloir supprimer ce progrès ?')) {
            return;
        }

        try {
            await progressService.deleteProgress(progressId);
            await loadHabitProgress();
        } catch (error: any) {
            console.error('Erreur lors de la suppression du progrès:', error);
            setError(error.message || 'Erreur lors de la suppression du progrès');
        }
    };

    const startEditProgress = (progress: LocalProgress) => {
        setEditingProgress(progress);
        setFormData({
            habitId: progress.habitId,
            date: progress.date,
            value: progress.value,
            note: progress.note || ''
        });
    };

    // Composant graphique simple en SVG
    const SimpleChart: React.FC = () => {
        if (!progressList.length) return null;

        const sortedProgress = [...progressList]
            .sort((a, b) => new Date(a.date).getTime() - new Date(b.date).getTime())
            .slice(-14); // Derniers 14 jours

        const maxValue = Math.max(...sortedProgress.map(p => p.value));
        const height = 200;
        const width = 600;
        const padding = 40;

        const points = sortedProgress.map((progress, index) => {
            const x = padding + (index * (width - 2 * padding)) / (sortedProgress.length - 1);
            const y = height - padding - ((progress.value / maxValue) * (height - 2 * padding));
            return `${x},${y}`;
        }).join(' ');

        return (
            <div className="w-full overflow-x-auto">
                <svg width={width} height={height} className="border border-neutral-200 rounded-lg">
                    {/* Grille de fond */}
                    <defs>
                        <pattern id="grid" width="40" height="40" patternUnits="userSpaceOnUse">
                            <path d="M 40 0 L 0 0 0 40" fill="none" stroke="#f3f4f6" strokeWidth="1"/>
                        </pattern>
                    </defs>
                    <rect width="100%" height="100%" fill="url(#grid)" />

                    {/* Ligne de données */}
                    <polyline
                        fill="none"
                        stroke="#d4623a"
                        strokeWidth="3"
                        points={points}
                    />

                    {/* Points de données */}
                    {sortedProgress.map((progress, index) => {
                        const x = padding + (index * (width - 2 * padding)) / (sortedProgress.length - 1);
                        const y = height - padding - ((progress.value / maxValue) * (height - 2 * padding));
                        return (
                            <circle
                                key={progress.id}
                                cx={x}
                                cy={y}
                                r="4"
                                fill="#d4623a"
                                stroke="white"
                                strokeWidth="2"
                            />
                        );
                    })}

                    {/* Étiquettes des axes */}
                    <text x="20" y="20" fontSize="12" fill="#666">
                        {maxValue} {selectedHabit?.unit}
                    </text>
                    <text x="20" y={height - 10} fontSize="12" fill="#666">
                        0
                    </text>
                </svg>

                {/* Légende des dates */}
                <div className="flex justify-between mt-2 px-10 text-xs text-neutral-600">
                    <span>{new Date(sortedProgress[0]?.date).toLocaleDateString('fr-FR', { month: 'short', day: 'numeric' })}</span>
                    <span>{new Date(sortedProgress[sortedProgress.length - 1]?.date).toLocaleDateString('fr-FR', { month: 'short', day: 'numeric' })}</span>
                </div>
            </div>
        );
    };

    const formatDate = (dateString: string) => {
        return new Date(dateString).toLocaleDateString('fr-FR', {
            weekday: 'long',
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        });
    };

    const getDaysSinceLastEntry = () => {
        if (!progressList.length) return null;

        const lastEntry = progressList.reduce((latest, current) =>
            new Date(current.date) > new Date(latest.date) ? current : latest
        );

        const today = new Date();
        const lastDate = new Date(lastEntry.date);
        const diffTime = today.getTime() - lastDate.getTime();
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

        return diffDays;
    };

    if (isLoading) {
        return (
            <MainLayout title="Progression" subtitle="Suivi de vos progrès">
                <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
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
            </MainLayout>
        );
    }

    if (!habits.length) {
        return (
            <MainLayout title="Progression" subtitle="Suivi de vos progrès">
                <Card variant="outlined" className="border-dashed">
                    <CardBody className="text-center py-12">
                        <Target className="h-16 w-16 text-neutral-300 mx-auto mb-4" />
                        <h3 className="text-lg font-medium text-neutral-900 mb-2">
                            Aucune habitude trouvée
                        </h3>
                        <p className="text-neutral-600 mb-6">
                            Créez d'abord des habitudes pour pouvoir suivre vos progrès.
                        </p>
                        <Button
                            variant="primary"
                            onClick={() => window.location.href = '/habits'}
                        >
                            Créer une habitude
                        </Button>
                    </CardBody>
                </Card>
            </MainLayout>
        );
    }

    return (
        <MainLayout
            title="Progression"
            subtitle={selectedHabit ? `Suivi de "${selectedHabit.title}"` : "Suivi de vos progrès"}
            actions={
                <Button
                    variant="primary"
                    onClick={() => {
                        setFormData({
                            habitId: selectedHabit?.id || 0,
                            date: new Date().toISOString().split('T')[0],
                            value: selectedHabit?.targetValue || 1,
                            note: ''
                        });
                        setShowAddProgress(true);
                    }}
                    icon={<Plus className="h-5 w-5" />}
                    disabled={!selectedHabit}
                >
                    Ajouter un progrès
                </Button>
            }
        >
            <div className="space-y-6">
                {/* Erreurs */}
                {error && (
                    <div className="bg-danger-50 border border-danger-200 rounded-xl p-4">
                        <p className="text-danger-700 text-sm">{error}</p>
                        <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => setError(null)}
                            className="mt-2"
                        >
                            Masquer
                        </Button>
                    </div>
                )}

                {/* Sélection d'habitude et filtres */}
                <Card variant="elevated">
                    <CardBody>
                        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                            {/* Sélection d'habitude */}
                            <div>
                                <label className="block text-sm font-medium text-neutral-700 mb-2">
                                    Habitude
                                </label>
                                <select
                                    value={selectedHabit?.id || ''}
                                    onChange={(e) => {
                                        const habit = habits.find(h => h.id === Number(e.target.value));
                                        setSelectedHabit(habit || null);
                                    }}
                                    className="w-full border border-neutral-300 rounded-xl px-4 py-3 transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
                                >
                                    {habits.map(habit => (
                                        <option key={habit.id} value={habit.id}>
                                            {habitService.getCategoryIcon(habit.category)} {habit.title}
                                        </option>
                                    ))}
                                </select>
                            </div>

                            {/* Période */}
                            <div>
                                <label className="block text-sm font-medium text-neutral-700 mb-2">
                                    Période
                                </label>
                                <select
                                    value={dateRange}
                                    onChange={(e) => setDateRange(Number(e.target.value))}
                                    className="w-full border border-neutral-300 rounded-xl px-4 py-3 transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
                                >
                                    <option value={7}>7 derniers jours</option>
                                    <option value={30}>30 derniers jours</option>
                                    <option value={90}>90 derniers jours</option>
                                    <option value={365}>1 an</option>
                                </select>
                            </div>

                            {/* Mode d'affichage */}
                            <div>
                                <label className="block text-sm font-medium text-neutral-700 mb-2">
                                    Affichage
                                </label>
                                <div className="flex rounded-xl border border-neutral-300 overflow-hidden">
                                    <button
                                        onClick={() => setViewMode('list')}
                                        className={`flex-1 px-4 py-3 flex items-center justify-center gap-2 transition-all duration-200 ${
                                            viewMode === 'list'
                                                ? 'bg-primary-500 text-white'
                                                : 'bg-white text-neutral-600 hover:bg-neutral-50'
                                        }`}
                                    >
                                        <Calendar className="h-4 w-4" />
                                        Liste
                                    </button>
                                    <button
                                        onClick={() => setViewMode('chart')}
                                        className={`flex-1 px-4 py-3 flex items-center justify-center gap-2 transition-all duration-200 ${
                                            viewMode === 'chart'
                                                ? 'bg-primary-500 text-white'
                                                : 'bg-white text-neutral-600 hover:bg-neutral-50'
                                        }`}
                                    >
                                        <BarChart3 className="h-4 w-4" />
                                        Graphique
                                    </button>
                                </div>
                            </div>
                        </div>
                    </CardBody>
                </Card>

                {/* Statistiques */}
                {progressStats && (
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                        <Card variant="filled" className="bg-primary-50 border-primary-200">
                            <CardBody>
                                <div className="flex items-center justify-between">
                                    <div>
                                        <p className="text-sm font-medium text-primary-700">Total entries</p>
                                        <p className="text-2xl font-bold text-primary-900">{progressStats.totalEntries}</p>
                                    </div>
                                    <Target className="h-8 w-8 text-primary-500" />
                                </div>
                            </CardBody>
                        </Card>

                        <Card variant="filled" className="bg-success-50 border-success-200">
                            <CardBody>
                                <div className="flex items-center justify-between">
                                    <div>
                                        <p className="text-sm font-medium text-success-700">Série actuelle</p>
                                        <p className="text-2xl font-bold text-success-900">{progressStats.consecutiveDays} jours</p>
                                    </div>
                                    <TrendingUp className="h-8 w-8 text-success-500" />
                                </div>
                            </CardBody>
                        </Card>

                        <Card variant="filled" className="bg-accent-50 border-accent-200">
                            <CardBody>
                                <div className="flex items-center justify-between">
                                    <div>
                                        <p className="text-sm font-medium text-accent-700">Moyenne</p>
                                        <p className="text-2xl font-bold text-accent-900">
                                            {progressStats.averageValue?.toFixed(1)} {selectedHabit?.unit}
                                        </p>
                                    </div>
                                    <BarChart3 className="h-8 w-8 text-accent-500" />
                                </div>
                            </CardBody>
                        </Card>

                        <Card variant="filled" className="bg-warning-50 border-warning-200">
                            <CardBody>
                                <div className="flex items-center justify-between">
                                    <div>
                                        <p className="text-sm font-medium text-warning-700">Taux de réussite</p>
                                        <p className="text-2xl font-bold text-warning-900">{progressStats.completionRate?.toFixed(0)}%</p>
                                    </div>
                                    <Calendar className="h-8 w-8 text-warning-500" />
                                </div>
                            </CardBody>
                        </Card>
                    </div>
                )}

                {/* Contenu principal */}
                {viewMode === 'chart' ? (
                    /* Graphique */
                    <Card variant="elevated">
                        <CardHeader>
                            <h3 className="text-lg font-semibold text-neutral-900">
                                Évolution des progrès (14 derniers jours)
                            </h3>
                        </CardHeader>
                        <CardBody>
                            {progressList.length > 0 ? (
                                <SimpleChart />
                            ) : (
                                <div className="h-80 flex items-center justify-center text-neutral-500">
                                    <div className="text-center">
                                        <BarChart3 className="h-16 w-16 text-neutral-300 mx-auto mb-4" />
                                        <p>Aucune donnée à afficher</p>
                                    </div>
                                </div>
                            )}
                        </CardBody>
                    </Card>
                ) : (
                    /* Liste des progrès */
                    <Card variant="elevated">
                        <CardHeader>
                            <div className="flex items-center justify-between">
                                <h3 className="text-lg font-semibold text-neutral-900">
                                    Historique des progrès
                                </h3>
                                {progressList.length > 0 && (
                                    <Badge variant="neutral" size="sm">
                                        {progressList.length} entrée{progressList.length > 1 ? 's' : ''}
                                    </Badge>
                                )}
                            </div>
                        </CardHeader>
                        <CardBody>
                            {progressList.length > 0 ? (
                                <div className="space-y-3">
                                    {progressList
                                        .sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime())
                                        .map((progress) => (
                                            <div
                                                key={progress.id}
                                                className="flex items-center justify-between p-4 border border-neutral-200 rounded-xl hover:border-primary-200 transition-all duration-200"
                                            >
                                                <div className="flex-1">
                                                    <div className="flex items-center gap-3 mb-1">
                                                        <span className="font-medium text-neutral-900">
                                                            {progress.value} {selectedHabit?.unit}
                                                        </span>
                                                        {selectedHabit?.targetValue && (
                                                            <Badge
                                                                variant={progress.value >= selectedHabit.targetValue ? 'success' : 'warning'}
                                                                size="sm"
                                                            >
                                                                {((progress.value / selectedHabit.targetValue) * 100).toFixed(0)}%
                                                            </Badge>
                                                        )}
                                                    </div>
                                                    <p className="text-sm text-neutral-600">
                                                        {formatDate(progress.date)}
                                                    </p>
                                                    {progress.note && (
                                                        <p className="text-sm text-neutral-500 mt-1 italic">
                                                            "{progress.note}"
                                                        </p>
                                                    )}
                                                </div>
                                                <div className="flex items-center gap-2">
                                                    <Button
                                                        variant="ghost"
                                                        size="sm"
                                                        onClick={() => startEditProgress(progress)}
                                                    >
                                                        <Edit className="h-4 w-4" />
                                                    </Button>
                                                    <Button
                                                        variant="ghost"
                                                        size="sm"
                                                        onClick={() => handleDeleteProgress(progress.id)}
                                                        className="text-danger-600 hover:text-danger-700"
                                                    >
                                                        <Trash2 className="h-4 w-4" />
                                                    </Button>
                                                </div>
                                            </div>
                                        ))}
                                </div>
                            ) : (
                                <div className="text-center py-12">
                                    <Calendar className="h-16 w-16 text-neutral-300 mx-auto mb-4" />
                                    <h3 className="text-lg font-medium text-neutral-900 mb-2">
                                        Aucun progrès enregistré
                                    </h3>
                                    <p className="text-neutral-600 mb-6">
                                        Commencez à enregistrer vos progrès pour suivre votre évolution.
                                    </p>
                                    <Button
                                        variant="primary"
                                        onClick={() => setShowAddProgress(true)}
                                        icon={<Plus className="h-5 w-5" />}
                                    >
                                        Ajouter le premier progrès
                                    </Button>
                                </div>
                            )}
                        </CardBody>
                    </Card>
                )}

                {/* Informations supplémentaires */}
                {getDaysSinceLastEntry() !== null && (
                    <Card variant="filled" className="bg-neutral-50">
                        <CardBody>
                            <div className="flex items-center justify-between">
                                <div>
                                    <h4 className="font-medium text-neutral-900 mb-1">
                                        Dernière entrée
                                    </h4>
                                    <p className="text-neutral-600">
                                        Il y a {getDaysSinceLastEntry()} jour{getDaysSinceLastEntry()! > 1 ? 's' : ''}
                                    </p>
                                </div>
                                {getDaysSinceLastEntry()! > 3 && (
                                    <Button
                                        variant="primary"
                                        size="sm"
                                        onClick={() => setShowAddProgress(true)}
                                    >
                                        Reprendre le suivi
                                    </Button>
                                )}
                            </div>
                        </CardBody>
                    </Card>
                )}
            </div>

            {/* Modal d'ajout/modification de progrès */}
            {(showAddProgress || editingProgress) && (
                <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
                    <Card variant="elevated" className="w-full max-w-md">
                        <CardHeader>
                            <h3 className="text-lg font-semibold text-neutral-900">
                                {editingProgress ? 'Modifier le progrès' : 'Ajouter un progrès'}
                            </h3>
                        </CardHeader>

                        <form onSubmit={editingProgress ? handleEditProgress : handleAddProgress}>
                            <CardBody className="space-y-4">
                                <Input
                                    label="Date"
                                    type="date"
                                    value={formData.date}
                                    onChange={(e) => setFormData({ ...formData, date: e.target.value })}
                                    fullWidth
                                    required
                                />

                                <Input
                                    label={`Valeur (${selectedHabit?.unit})`}
                                    type="number"
                                    step="0.1"
                                    min="0"
                                    value={formData.value || ''}
                                    onChange={(e) => setFormData({ ...formData, value: Number(e.target.value) })}
                                    fullWidth
                                    required
                                />

                                <div>
                                    <label className="block text-sm font-medium text-neutral-700 mb-2">
                                        Note (optionnel)
                                    </label>
                                    <textarea
                                        value={formData.note}
                                        onChange={(e) => setFormData({ ...formData, note: e.target.value })}
                                        className="w-full border border-neutral-300 rounded-xl px-4 py-3 transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-primary-500 resize-none"
                                        rows={3}
                                        placeholder="Ajoutez une note sur ce progrès..."
                                    />
                                </div>
                            </CardBody>

                            <CardFooter>
                                <div className="flex justify-end gap-3 w-full">
                                    <Button
                                        type="button"
                                        variant="outline"
                                        onClick={() => {
                                            setShowAddProgress(false);
                                            setEditingProgress(null);
                                            setFormData({
                                                habitId: selectedHabit?.id || 0,
                                                date: new Date().toISOString().split('T')[0],
                                                value: 0,
                                                note: ''
                                            });
                                        }}
                                    >
                                        Annuler
                                    </Button>
                                    <Button type="submit" variant="primary">
                                        {editingProgress ? 'Modifier' : 'Ajouter'}
                                    </Button>
                                </div>
                            </CardFooter>
                        </form>
                    </Card>
                </div>
            )}
        </MainLayout>
    );
};

export default Progress;