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
import { progressService } from '../services/progressService';
import { achievementService } from '../services/achievementService';
import { useAuth } from '../contexts/AuthContext';

const Habits: React.FC = () => {
    const { user } = useAuth();
    const [habits, setHabits] = useState<Habit[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [isCreating, setIsCreating] = useState(false);
    const [editingHabit, setEditingHabit] = useState<Habit | null>(null);
    const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');
    const [error, setError] = useState<string | null>(null);

    // Filtres et recherche
    const [searchQuery, setSearchQuery] = useState('');
    const [selectedCategory, setSelectedCategory] = useState<HabitCategory | 'ALL'>('ALL');
    const [showActiveOnly, setShowActiveOnly] = useState(false);

    // Pagination
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);

    useEffect(() => {
        console.log('🔄 useEffect triggered - Chargement des habitudes');
        loadHabits();
    }, [currentPage, searchQuery, selectedCategory, showActiveOnly]);

    const loadHabits = async () => {
        try {
            console.log('🔍 === DÉBUT LOADHABITS ===');
            console.log('🔍 Utilisateur connecté:', user);
            console.log('🔍 ID utilisateur:', user?.id);

            setIsLoading(true);
            setError(null);

            if (!user?.id) {
                console.error('❌ Utilisateur non connecté');
                setError('Utilisateur non connecté. Veuillez vous reconnecter.');
                setHabits([]);
                setTotalPages(0);
                setTotalElements(0);
                return;
            }

            const requestParams = {
                page: currentPage,
                size: 12,
                search: searchQuery || undefined,
                category: selectedCategory !== 'ALL' ? selectedCategory : undefined,
                active: showActiveOnly ? true : undefined,
            };

            console.log('🔍 Paramètres de la requête:', requestParams);

            const response = await habitService.getHabits(requestParams);
            console.log('🔍 Réponse du service:', response);

            if (!response) {
                console.error('❌ Réponse vide du service');
                throw new Error('Aucune réponse du serveur');
            }

            if (!Array.isArray(response.content)) {
                console.error('❌ response.content n\'est pas un tableau:', response.content);
                throw new Error('Format de données invalide reçu du serveur');
            }

            console.log(`✅ ${response.content.length} habitude(s) reçue(s)`);
            console.log('📊 Métadonnées:', {
                totalElements: response.totalElements,
                totalPages: response.totalPages,
                currentPage: response.pageable?.pageNumber
            });

            // Mise à jour du state
            setHabits(response.content);
            setTotalPages(response.totalPages || 0);
            setTotalElements(response.totalElements || 0);

            console.log('✅ State mis à jour avec succès');
            console.log('🔍 === FIN LOADHABITS ===');

        } catch (error: any) {
            console.error('❌ ERREUR COMPLÈTE dans loadHabits:', {
                message: error.message,
                status: error.status,
                response: error.response,
                stack: error.stack
            });

            const errorMessage = error.message || 'Erreur lors du chargement des habitudes';
            setError(errorMessage);
            setHabits([]);
            setTotalPages(0);
            setTotalElements(0);
        } finally {
            setIsLoading(false);
        }
    };

    const handleCreateHabit = async (data: CreateHabitRequest | UpdateHabitRequest) => {
        try {
            console.log('🔍 === DÉBUT CRÉATION HABITUDE ===');
            console.log('🔍 Données reçues:', data);

            // Validation des données côté client
            if (!data.title?.trim()) {
                throw new Error('Le titre est obligatoire');
            }

            if (!data.category) {
                throw new Error('La catégorie est obligatoire');
            }

            if (!data.unit?.trim()) {
                throw new Error('L\'unité est obligatoire');
            }

            if (!data.frequency) {
                throw new Error('La fréquence est obligatoire');
            }

            console.log('✅ Validation côté client OK');

            const createRequest = data as CreateHabitRequest;
            console.log('🔍 Données pour la création:', createRequest);

            const createdHabit = await habitService.createHabit(createRequest);
            console.log('✅ Habitude créée:', createdHabit);

            // Vérifier les nouveaux achievements
            if (user?.id) {
                try {
                    console.log('🏆 Vérification des achievements...');
                    await achievementService.checkAchievements({
                        userId: user.id,
                        triggerType: 'HABIT_CREATED'
                    });
                    console.log('✅ Achievements vérifiés');
                } catch (achievementError) {
                    console.warn('⚠️ Erreur lors de la vérification des achievements:', achievementError);
                    // Ne pas faire échouer la création pour cela
                }
            }

            // Fermer le modal et recharger
            setIsCreating(false);
            setCurrentPage(0); // Retourner à la première page
            await loadHabits();

            console.log('✅ === FIN CRÉATION HABITUDE ===');

        } catch (error: any) {
            console.error('❌ Erreur lors de la création:', error);
            throw error; // Re-throw pour que le formulaire puisse gérer l'erreur
        }
    };

    const handleUpdateHabit = async (data: CreateHabitRequest | UpdateHabitRequest) => {
        if (!editingHabit) {
            console.error('❌ Aucune habitude en cours d\'édition');
            return;
        }

        try {
            console.log('🔍 === DÉBUT MISE À JOUR HABITUDE ===');
            console.log('🔍 Habitude à modifier:', editingHabit.id);
            console.log('🔍 Nouvelles données:', data);

            const updateRequest = data as UpdateHabitRequest;
            const updatedHabit = await habitService.updateHabit(editingHabit.id, updateRequest);
            console.log('✅ Habitude mise à jour:', updatedHabit);

            setEditingHabit(null);
            await loadHabits();

            console.log('✅ === FIN MISE À JOUR HABITUDE ===');

        } catch (error: any) {
            console.error('❌ Erreur lors de la mise à jour:', error);
            throw error;
        }
    };

    const handleDeleteHabit = async (habitId: number) => {
        const confirmMessage = 'Êtes-vous sûr de vouloir supprimer cette habitude ? Cette action supprimera également tous les progrès associés.';

        if (!window.confirm(confirmMessage)) {
            return;
        }

        try {
            console.log('🔍 Suppression de l\'habitude:', habitId);

            await habitService.deleteHabit(habitId);
            console.log('✅ Habitude supprimée');

            await loadHabits();
        } catch (error: any) {
            console.error('❌ Erreur lors de la suppression:', error);
            setError(error.message || 'Erreur lors de la suppression de l\'habitude');
        }
    };

    const handleToggleStatus = async (habitId: number) => {
        try {
            console.log('🔍 Changement de statut de l\'habitude:', habitId);

            await habitService.toggleHabitStatus(habitId);
            console.log('✅ Statut modifié');

            await loadHabits();
        } catch (error: any) {
            console.error('❌ Erreur lors du changement de statut:', error);
            setError(error.message || 'Erreur lors du changement de statut');
        }
    };

    const handleAddProgress = async (habit: Habit) => {
        try {
            console.log('🔍 Ajout de progrès pour l\'habitude:', habit.id);

            const today = new Date().toISOString().split('T')[0];
            const defaultValue = habit.targetValue || 1;

            await progressService.createProgress(habit.id, {
                date: today,
                value: defaultValue,
                note: `Progrès ajouté rapidement`
            });

            console.log('✅ Progrès ajouté');

            // Vérifier les nouveaux achievements
            if (user?.id) {
                try {
                    const achievementCheck = await achievementService.checkAchievements({
                        userId: user.id,
                        habitId: habit.id,
                        triggerType: 'PROGRESS_ADDED'
                    });

                    if (achievementCheck.newAchievementsUnlocked > 0) {
                        console.log('🏆 Nouveaux badges débloqués:', achievementCheck.newAchievements);
                        // Ici vous pourriez afficher une notification
                    }
                } catch (achievementError) {
                    console.warn('⚠️ Erreur lors de la vérification des achievements:', achievementError);
                }
            }

            // Recharger pour mettre à jour les statistiques
            await loadHabits();

        } catch (error: any) {
            console.error('❌ Erreur lors de l\'ajout du progrès:', error);
            setError(error.message || 'Erreur lors de l\'ajout du progrès');
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

    const activeHabits = habits.filter(h => h.isActive).length;
    const inactiveHabits = habits.filter(h => !h.isActive).length;

    const handleResetFilters = () => {
        console.log('🔄 Réinitialisation des filtres');
        setSearchQuery('');
        setSelectedCategory('ALL');
        setShowActiveOnly(false);
        setCurrentPage(0);
    };

    const hasActiveFilters = searchQuery || selectedCategory !== 'ALL' || showActiveOnly;

    // Affichage d'erreur critique
    if (error && habits.length === 0 && !isLoading) {
        return (
            <MainLayout title="Mes habitudes">
                <Card variant="outlined" className="border-danger-200">
                    <CardBody className="text-center py-12">
                        <div className="text-6xl mb-4">⚠️</div>
                        <h3 className="text-lg font-medium text-danger-900 mb-2">
                            Erreur de chargement
                        </h3>
                        <p className="text-danger-600 mb-6">{error}</p>
                        <Button
                            variant="primary"
                            onClick={loadHabits}
                        >
                            Réessayer
                        </Button>
                    </CardBody>
                </Card>
            </MainLayout>
        );
    }

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
                {/* Affichage des erreurs non critiques */}
                {error && habits.length > 0 && (
                    <div className="bg-warning-50 border border-warning-200 rounded-xl p-4">
                        <p className="text-warning-700 text-sm">{error}</p>
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
                            {/* Ligne 1: Recherche et vue */}
                            <div className="flex flex-col md:flex-row gap-4">
                                <div className="flex-1">
                                    <Input
                                        placeholder="Rechercher une habitude..."
                                        icon={<Search className="h-5 w-5" />}
                                        value={searchQuery}
                                        onChange={(e) => {
                                            setSearchQuery(e.target.value);
                                            setCurrentPage(0);
                                        }}
                                        fullWidth
                                    />
                                </div>
                                <div className="flex items-center gap-2">
                                    <Button
                                        variant={viewMode === 'grid' ? 'primary' : 'ghost'}
                                        size="sm"
                                        onClick={() => setViewMode('grid')}
                                        icon={<Grid className="h-4 w-4"/>}
                                    >
                                        {null}
                                    </Button>
                                    <Button
                                        variant={viewMode === 'list' ? 'primary' : 'ghost'}
                                        size="sm"
                                        onClick={() => setViewMode('list')}
                                        icon={<List className="h-4 w-4"/>}
                                    >
                                        {null}
                                    </Button>
                                </div>
                            </div>

                            {/* Ligne 2: Filtres par catégorie */}
                            <div className="flex flex-wrap gap-2">
                                {categories.map((category) => (
                                    <button
                                        key={category.value}
                                        onClick={() => {
                                            setSelectedCategory(category.value);
                                            setCurrentPage(0);
                                        }}
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
                                        onChange={(e) => {
                                            setShowActiveOnly(e.target.checked);
                                            setCurrentPage(0);
                                        }}
                                        className="w-4 h-4 text-primary-600 bg-neutral-100 border-neutral-300 rounded focus:ring-primary-500"
                                    />
                                    <span className="text-sm text-neutral-700">Actives uniquement</span>
                                </label>

                                {hasActiveFilters && (
                                    <Button
                                        variant="ghost"
                                        size="sm"
                                        onClick={handleResetFilters}
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
                ) : habits.length === 0 ? (
                    <Card variant="outlined" className="border-dashed">
                        <CardBody className="text-center py-12">
                            <div className="text-6xl mb-4">🎯</div>
                            <h3 className="text-lg font-medium text-neutral-900 mb-2">
                                {hasActiveFilters
                                    ? 'Aucune habitude trouvée'
                                    : 'Aucune habitude créée'
                                }
                            </h3>
                            <p className="text-neutral-600 mb-6">
                                {hasActiveFilters
                                    ? 'Essayez de modifier vos filtres de recherche'
                                    : 'Commencez votre parcours en créant votre première habitude !'
                                }
                            </p>
                            {hasActiveFilters ? (
                                <Button
                                    variant="outline"
                                    onClick={handleResetFilters}
                                >
                                    Réinitialiser les filtres
                                </Button>
                            ) : (
                                <Button
                                    variant="primary"
                                    onClick={() => setIsCreating(true)}
                                    icon={<Plus className="h-5 w-5" />}
                                >
                                    Créer une habitude
                                </Button>
                            )}
                        </CardBody>
                    </Card>
                ) : (
                    <div className={`grid gap-6 ${viewMode === 'grid' ? 'grid-cols-1 md:grid-cols-2 lg:grid-cols-3' : 'grid-cols-1'}`}>
                        {habits.map((habit) => (
                            <HabitCard
                                key={habit.id}
                                habit={habit}
                                onEdit={setEditingHabit}
                                onDelete={handleDeleteHabit}
                                onToggleStatus={handleToggleStatus}
                                onProgress={handleAddProgress}
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

                        {[...Array(Math.min(totalPages, 7))].map((_, i) => {
                            let pageNumber: number;
                            if (totalPages <= 7) {
                                pageNumber = i;
                            } else if (currentPage < 3) {
                                pageNumber = i;
                            } else if (currentPage > totalPages - 4) {
                                pageNumber = totalPages - 7 + i;
                            } else {
                                pageNumber = currentPage - 3 + i;
                            }

                            if (pageNumber < 0 || pageNumber >= totalPages) return null;

                            return (
                                <Button
                                    key={pageNumber}
                                    variant={currentPage === pageNumber ? 'primary' : 'ghost'}
                                    size="sm"
                                    onClick={() => setCurrentPage(pageNumber)}
                                >
                                    {pageNumber + 1}
                                </Button>
                            );
                        })}

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

                {/* Debug info (à supprimer en production) */}
                {process.env.NODE_ENV === 'development' && (
                    <Card variant="outlined" className="border-neutral-200 bg-neutral-50">
                        <CardBody>
                            <details>
                                <summary className="cursor-pointer text-sm font-medium text-neutral-700">
                                    🔍 Informations de debug
                                </summary>
                                <div className="mt-4 space-y-2 text-xs text-neutral-600">
                                    <div>Utilisateur: {user?.id || 'Non connecté'}</div>
                                    <div>Habitudes chargées: {habits.length}</div>
                                    <div>Total éléments: {totalElements}</div>
                                    <div>Page actuelle: {currentPage + 1}/{totalPages}</div>
                                    <div>Filtres actifs: {hasActiveFilters ? 'Oui' : 'Non'}</div>
                                    <div>Recherche: "{searchQuery}"</div>
                                    <div>Catégorie: {selectedCategory}</div>
                                    <div>Actives seulement: {showActiveOnly ? 'Oui' : 'Non'}</div>
                                    <div>Erreur: {error || 'Aucune'}</div>
                                </div>
                            </details>
                        </CardBody>
                    </Card>
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