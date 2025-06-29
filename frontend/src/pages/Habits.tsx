import React, { useState, useEffect } from 'react';
import { Plus, Search, Filter, Grid, List } from 'lucide-react';
import MainLayout from '../components/templates/MainLayout';
import Card, { CardBody } from '../components/atoms/Card';
import Button from '../components/atoms/Button';
import Input from '../components/atoms/Input';
import Badge from '../components/atoms/Badge';
import HabitCard from '../components/molecules/HabitCard';
import HabitForm from '../components/molecules/HabitForm';
import {
    habitService,
    Habit,
    HabitCategory,
    CreateHabitRequest,
    UpdateHabitRequest
} from '../services/habitService';

const Habits: React.FC = () => {
    const [habits, setHabits] = useState<Habit[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [isCreating, setIsCreating] = useState(false);
    const [editingHabit, setEditingHabit] = useState<Habit | null>(null);
    const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');

    // Filtres et recherche
    const [searchQuery, setSearchQuery] = useState('');
    const [selectedCategory, setSelectedCategory] = useState<HabitCategory | 'ALL'>('ALL');
    const [showActiveOnly, setShowActiveOnly] = useState(false);

    // Pagination
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);

    useEffect(() => {
        loadHabits();
    }, [currentPage, searchQuery, selectedCategory, showActiveOnly]);

    const loadHabits = async () => {
        try {
            setIsLoading(true);

            try {
                const response = await habitService.getHabits({
                    page: currentPage,
                    size: 12,
                    search: searchQuery || undefined,
                    category: selectedCategory !== 'ALL' ? selectedCategory : undefined,
                    active: showActiveOnly ? true : undefined,
                });

                // Vérifier que la réponse est valide
                if (response && response.content && Array.isArray(response.content)) {
                    setHabits(response.content);
                    setTotalPages(response.totalPages || 0);
                    setTotalElements(response.totalElements || 0);
                } else {
                    console.warn('Réponse API invalide:', response);
                    setHabits([]);
                    setTotalPages(0);
                    setTotalElements(0);
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

                setHabits(fakeHabits);
                setTotalPages(1);
                setTotalElements(fakeHabits.length);
            }
        } catch (error) {
            console.error('Erreur lors du chargement des habitudes:', error);
            setHabits([]);
            setTotalPages(0);
            setTotalElements(0);
        } finally {
            setIsLoading(false);
        }
    };

    const handleCreateHabit = async (data: CreateHabitRequest | UpdateHabitRequest) => {
        try {
            // On sait que c'est une création, donc on cast vers CreateHabitRequest
            await habitService.createHabit(data as CreateHabitRequest);
            setIsCreating(false);
            loadHabits();
        } catch (error) {
            console.error('Erreur lors de la création:', error);
            throw error;
        }
    };

    const handleUpdateHabit = async (data: CreateHabitRequest | UpdateHabitRequest) => {
        if (!editingHabit) return;

        try {
            // On sait que c'est une mise à jour, donc on cast vers UpdateHabitRequest
            await habitService.updateHabit(editingHabit.id, data as UpdateHabitRequest);
            setEditingHabit(null);
            loadHabits();
        } catch (error) {
            console.error('Erreur lors de la mise à jour:', error);
            throw error;
        }
    };

    const handleDeleteHabit = async (habitId: number) => {
        if (!window.confirm('Êtes-vous sûr de vouloir supprimer cette habitude ?')) {
            return;
        }

        try {
            await habitService.deleteHabit(habitId);
            loadHabits();
        } catch (error) {
            console.error('Erreur lors de la suppression:', error);
        }
    };

    const handleToggleStatus = async (habitId: number) => {
        try {
            await habitService.toggleHabitStatus(habitId);
            loadHabits();
        } catch (error) {
            console.error('Erreur lors du changement de statut:', error);
        }
    };

    const categories: { value: HabitCategory | 'ALL'; label: string; icon?: string }[] = [
        { value: 'ALL', label: 'Toutes' },
        { value: 'SPORT', label: 'Sport', icon: '🏃‍♂️' },
        { value: 'SANTE', label: 'Santé', icon: '🏥' },
        { value: 'EDUCATION', label: 'Éducation', icon: '📚' },
        { value: 'TRAVAIL', label: 'Travail', icon: '💼' },
        { value: 'LIFESTYLE', label: 'Style de vie', icon: '🌱' },
        { value: 'SOCIAL', label: 'Social', icon: '👥' },
        { value: 'CREATIVITE', label: 'Créativité', icon: '🎨' },
        { value: 'FINANCE', label: 'Finance', icon: '💰' },
        { value: 'AUTRE', label: 'Autre', icon: '📌' },
    ];

    const activeHabits = (habits && Array.isArray(habits)) ? habits.filter(h => h.isActive).length : 0;
    const inactiveHabits = (habits && Array.isArray(habits)) ? habits.filter(h => !h.isActive).length : 0;

    return (
        <MainLayout
            title="Mes habitudes"
            subtitle={`${totalElements} habitude${totalElements > 1 ? 's' : ''} au total`}
            actions={
                <Button
                    variant="primary"
                    onClick={() => setIsCreating(true)}
                    icon={<Plus className="h-5 w-5" />}
                >
                    Nouvelle habitude
                </Button>
            }
        >
            <div className="space-y-6">
                {/* Statistiques rapides */}
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                    <Card variant="filled" className="bg-success-50 border-success-200">
                        <CardBody>
                            <div className="flex items-center justify-between">
                                <div>
                                    <p className="text-sm font-medium text-success-700">Actives</p>
                                    <p className="text-2xl font-bold text-success-900">{activeHabits}</p>
                                </div>
                                <div className="w-10 h-10 bg-success-100 rounded-xl flex items-center justify-center">
                                    <span className="text-xl">✅</span>
                                </div>
                            </div>
                        </CardBody>
                    </Card>

                    <Card variant="filled" className="bg-neutral-50 border-neutral-200">
                        <CardBody>
                            <div className="flex items-center justify-between">
                                <div>
                                    <p className="text-sm font-medium text-neutral-700">Inactives</p>
                                    <p className="text-2xl font-bold text-neutral-900">{inactiveHabits}</p>
                                </div>
                                <div className="w-10 h-10 bg-neutral-100 rounded-xl flex items-center justify-center">
                                    <span className="text-xl">⏸️</span>
                                </div>
                            </div>
                        </CardBody>
                    </Card>

                    <Card variant="filled" className="bg-primary-50 border-primary-200">
                        <CardBody>
                            <div className="flex items-center justify-between">
                                <div>
                                    <p className="text-sm font-medium text-primary-700">Total</p>
                                    <p className="text-2xl font-bold text-primary-900">{totalElements}</p>
                                </div>
                                <div className="w-10 h-10 bg-primary-100 rounded-xl flex items-center justify-center">
                                    <span className="text-xl">🎯</span>
                                </div>
                            </div>
                        </CardBody>
                    </Card>
                </div>

                {/* Filtres et recherche */}
                <Card variant="elevated">
                    <CardBody>
                        <div className="space-y-4">
                            {/* Ligne 1: Recherche */}
                            <div className="flex flex-col md:flex-row gap-4">
                                <div className="flex-1">
                                    <Input
                                        placeholder="Rechercher une habitude..."
                                        icon={<Search className="h-5 w-5" />}
                                        value={searchQuery}
                                        onChange={(e) => setSearchQuery(e.target.value)}
                                        fullWidth
                                    />
                                </div>
                                <div className="flex items-center gap-2">
                                    <Button
                                        variant={viewMode === 'grid' ? 'primary' : 'ghost'}
                                        size="sm"
                                        onClick={() => setViewMode('grid')}
                                        icon={<Grid className="h-4 w-4"/>} children={undefined}                                    />
                                    <Button
                                        variant={viewMode === 'list' ? 'primary' : 'ghost'}
                                        size="sm"
                                        onClick={() => setViewMode('list')}
                                        icon={<List className="h-4 w-4"/>} children={undefined}                                    />
                                </div>
                            </div>

                            {/* Ligne 2: Filtres par catégorie */}
                            <div className="flex flex-wrap gap-2">
                                {categories.map((category) => (
                                    <button
                                        key={category.value}
                                        onClick={() => setSelectedCategory(category.value)}
                                        className={`
                      flex items-center gap-2 px-3 py-2 rounded-xl border transition-all duration-200
                      ${selectedCategory === category.value
                                            ? 'bg-primary-100 border-primary-300 text-primary-700'
                                            : 'bg-white border-neutral-200 text-neutral-600 hover:border-primary-200 hover:text-primary-600'
                                        }
                    `}
                                    >
                                        {category.icon && <span>{category.icon}</span>}
                                        <span className="text-sm font-medium">{category.label}</span>
                                    </button>
                                ))}
                            </div>

                            {/* Ligne 3: Options */}
                            <div className="flex items-center justify-between">
                                <label className="flex items-center gap-2 cursor-pointer">
                                    <input
                                        type="checkbox"
                                        checked={showActiveOnly}
                                        onChange={(e) => setShowActiveOnly(e.target.checked)}
                                        className="w-4 h-4 text-primary-600 bg-neutral-100 border-neutral-300 rounded focus:ring-primary-500"
                                    />
                                    <span className="text-sm text-neutral-700">Actives uniquement</span>
                                </label>

                                {(searchQuery || selectedCategory !== 'ALL' || showActiveOnly) && (
                                    <Button
                                        variant="ghost"
                                        size="sm"
                                        onClick={() => {
                                            setSearchQuery('');
                                            setSelectedCategory('ALL');
                                            setShowActiveOnly(false);
                                        }}
                                    >
                                        Réinitialiser
                                    </Button>
                                )}
                            </div>
                        </div>
                    </CardBody>
                </Card>

                {/* Liste des habitudes */}
                {isLoading ? (
                    <div className={`grid gap-6 ${viewMode === 'grid' ? 'grid-cols-1 md:grid-cols-2 lg:grid-cols-3' : 'grid-cols-1'}`}>
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
                ) : (!habits || habits.length === 0) ? (
                    <Card variant="outlined" className="border-dashed">
                        <CardBody className="text-center py-12">
                            <div className="text-6xl mb-4">🎯</div>
                            <h3 className="text-lg font-medium text-neutral-900 mb-2">
                                {searchQuery || selectedCategory !== 'ALL' || showActiveOnly
                                    ? 'Aucune habitude trouvée'
                                    : 'Aucune habitude créée'
                                }
                            </h3>
                            <p className="text-neutral-600 mb-6">
                                {searchQuery || selectedCategory !== 'ALL' || showActiveOnly
                                    ? 'Essayez de modifier vos filtres de recherche'
                                    : 'Commencez votre parcours en créant votre première habitude !'
                                }
                            </p>
                            <Button
                                variant="primary"
                                onClick={() => setIsCreating(true)}
                                icon={<Plus className="h-5 w-5" />}
                            >
                                Créer une habitude
                            </Button>
                        </CardBody>
                    </Card>
                ) : (
                    <div className={`grid gap-6 ${viewMode === 'grid' ? 'grid-cols-1 md:grid-cols-2 lg:grid-cols-3' : 'grid-cols-1'}`}>
                        {habits && habits.map((habit) => (
                            <HabitCard
                                key={habit.id}
                                habit={habit}
                                onEdit={setEditingHabit}
                                onDelete={handleDeleteHabit}
                                onToggleStatus={handleToggleStatus}
                                onProgress={(habit) => console.log('Progress for', habit.title)}
                            />
                        ))}
                    </div>
                )}

                {/* Pagination */}
                {totalPages > 1 && (
                    <div className="flex justify-center items-center gap-2">
                        <Button
                            variant="outline"
                            size="sm"
                            disabled={currentPage === 0}
                            onClick={() => setCurrentPage(currentPage - 1)}
                        >
                            Précédent
                        </Button>

                        {[...Array(totalPages)].map((_, i) => (
                            <Button
                                key={i}
                                variant={currentPage === i ? 'primary' : 'ghost'}
                                size="sm"
                                onClick={() => setCurrentPage(i)}
                            >
                                {i + 1}
                            </Button>
                        ))}

                        <Button
                            variant="outline"
                            size="sm"
                            disabled={currentPage === totalPages - 1}
                            onClick={() => setCurrentPage(currentPage + 1)}
                        >
                            Suivant
                        </Button>
                    </div>
                )}
            </div>

            {/* Modals */}
            {isCreating && (
                <HabitForm
                    onSubmit={handleCreateHabit}
                    onCancel={() => setIsCreating(false)}
                />
            )}

            {editingHabit && (
                <HabitForm
                    habit={editingHabit}
                    onSubmit={handleUpdateHabit}
                    onCancel={() => setEditingHabit(null)}
                />
            )}
        </MainLayout>
    );
};

export default Habits;