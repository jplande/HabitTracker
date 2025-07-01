// src/services/habitService.ts - Version corrigée avec gestion d'erreur fixée
import { apiClient } from './apiClient';

// Interface pour les erreurs API
interface ApiErrorInterface {
    message: string;
    status?: number;
    statusText?: string;
    data?: any;
}

// Fonction pour extraire le message d'erreur de différents types d'erreurs
function extractErrorMessage(error: any, defaultMessage: string = 'Une erreur est survenue'): string {
    if (typeof error === 'string') {
        return error;
    }

    if (error instanceof Error) {
        return error.message;
    }

    if (error && typeof error === 'object') {
        // Gestion des erreurs de response HTTP
        if ('response' in error && error.response) {
            const response = error.response;
            if (response.data?.message) {
                return response.data.message;
            }
            if (response.statusText) {
                return `Erreur ${response.status}: ${response.statusText}`;
            }
            return `Erreur HTTP ${response.status}`;
        }

        // Gestion des erreurs avec propriété message
        if ('message' in error) {
            return error.message;
        }

        // Gestion des erreurs réseau
        if ('code' in error && error.code === 'NETWORK_ERROR') {
            return 'Erreur de connexion réseau';
        }
    }

    return defaultMessage;
}

// Types pour les habitudes
export interface Habit {
    id: number;
    userId: number;
    title: string;
    description?: string;
    category: HabitCategory;
    unit: string;
    frequency: HabitFrequency;
    targetValue?: number;
    isActive: boolean;
    createdAt: string;
    totalProgress?: number;
    currentStreak?: number;
    averageCompletion?: number;
    lastProgressDate?: string;
}

export type HabitCategory =
    | 'SPORT'
    | 'SANTE'
    | 'EDUCATION'
    | 'TRAVAIL'
    | 'LIFESTYLE'
    | 'SOCIAL'
    | 'CREATIVITE'
    | 'FINANCE'
    | 'AUTRE';

export type HabitFrequency = 'DAILY' | 'WEEKLY' | 'MONTHLY';

export interface CreateHabitRequest {
    title: string;
    description?: string;
    category: HabitCategory;
    unit: string;
    frequency: HabitFrequency;
    targetValue?: number;
}

export interface UpdateHabitRequest {
    title?: string;
    description?: string;
    category?: HabitCategory;
    unit?: string;
    frequency?: HabitFrequency;
    targetValue?: number;
    isActive?: boolean;
}

export interface PaginatedResponse<T> {
    content: T[];
    pageable: {
        pageNumber: number;
        pageSize: number;
    };
    totalElements: number;
    totalPages: number;
    last: boolean;
    first: boolean;
}

// Interface pour la réponse HATEOAS de Spring Boot
export interface HateoasPagedResponse<T> {
    _embedded?: {
        [key: string]: Array<{
            content?: T;
            [key: string]: any;
        }>;
    };
    page?: {
        size: number;
        totalElements: number;
        totalPages: number;
        number: number;
    };
    _links?: any;
}

// Interface pour EntityModel de Spring HATEOAS
export interface EntityModel<T> {
    content?: T;
    _links?: any;
}

class HabitService {
    async getHabits(params?: {
        page?: number;
        size?: number;
        search?: string;
        category?: HabitCategory;
        active?: boolean;
    }): Promise<PaginatedResponse<Habit>> {
        try {
            console.log('🔍 HabitService.getHabits - Paramètres envoyés:', params);

            const response: HateoasPagedResponse<Habit> | PaginatedResponse<Habit> | Habit[] =
                await apiClient.get('/habits', { params });

            console.log('🔍 HabitService.getHabits - Réponse brute:', response);

            let habits: Habit[] = [];
            let totalElements = 0;
            let totalPages = 0;
            let pageNumber = 0;
            let pageSize = 20;

            // Cas 1: Réponse HATEOAS avec _embedded
            if (response && typeof response === 'object' && '_embedded' in response && response._embedded) {
                console.log('🔍 Structure HATEOAS détectée');
                console.log('🔍 Clés dans _embedded:', Object.keys(response._embedded));

                // Chercher la clé qui contient les données des habitudes
                const possibleKeys = [
                    'habitResponseList',
                    'habitResponses',
                    'habits',
                    'content'
                ];

                let dataKey = '';
                for (const key of Object.keys(response._embedded)) {
                    if (possibleKeys.includes(key) || key.toLowerCase().includes('habit')) {
                        dataKey = key;
                        break;
                    }
                }

                // Si aucune clé connue, prendre la première
                if (!dataKey && Object.keys(response._embedded).length > 0) {
                    dataKey = Object.keys(response._embedded)[0];
                }

                if (dataKey && response._embedded[dataKey]) {
                    console.log(`🔍 Utilisation de la clé: ${dataKey}`);

                    const items = response._embedded[dataKey];
                    console.log('🔍 Items trouvés:', items);

                    habits = items.map((item: any) => {
                        // Si l'item a une propriété 'content' (EntityModel)
                        if (item && typeof item === 'object' && 'content' in item) {
                            console.log('🔍 Extraction depuis item.content:', item.content);
                            return item.content;
                        }
                        // Sinon prendre l'item directement
                        return item;
                    }).filter(Boolean); // Enlever les valeurs null/undefined
                }

                // Métadonnées de pagination
                if ('page' in response && response.page) {
                    totalElements = response.page.totalElements || 0;
                    totalPages = response.page.totalPages || 0;
                    pageNumber = response.page.number || 0;
                    pageSize = response.page.size || 20;
                }
            }
            // Cas 2: Réponse simple avec propriété content
            else if (response && typeof response === 'object' && 'content' in response && Array.isArray(response.content)) {
                console.log('🔍 Structure simple avec content détectée');
                habits = response.content;
                totalElements = response.totalElements || habits.length;
                totalPages = response.totalPages || 1;
                pageNumber = response.pageable?.pageNumber || 0;
                pageSize = response.pageable?.pageSize || 20;
            }
            // Cas 3: Tableau direct
            else if (Array.isArray(response)) {
                console.log('🔍 Tableau direct détecté');
                habits = response;
                totalElements = habits.length;
                totalPages = 1;
                pageNumber = 0;
                pageSize = habits.length;
            }
            // Cas 4: Structure inconnue
            else {
                console.error('❌ Structure de réponse non reconnue:', response);
                console.error('❌ Type:', typeof response);
                console.error('❌ Clés:', response && typeof response === 'object' ? Object.keys(response) : 'N/A');
                throw new Error('Structure de réponse API non reconnue');
            }

            // Validation des données extraites
            if (!Array.isArray(habits)) {
                console.error('❌ Les habitudes extraites ne sont pas un tableau:', habits);
                habits = [];
            }

            console.log(`✅ ${habits.length} habitude(s) extraite(s) avec succès`);
            console.log('🔍 Première habitude:', habits[0]);

            const result: PaginatedResponse<Habit> = {
                content: habits,
                totalElements,
                totalPages: Math.max(totalPages, 1),
                pageable: {
                    pageNumber,
                    pageSize
                },
                last: totalPages <= 1 || pageNumber >= totalPages - 1,
                first: pageNumber === 0
            };

            console.log('✅ Réponse formatée finale:', result);
            return result;

        } catch (error) {
            console.error('❌ Erreur complète dans HabitService.getHabits:', error);

            const errorMessage = extractErrorMessage(error, 'Erreur lors de la récupération des habitudes');
            throw new Error(errorMessage);
        }
    }

    async getHabitById(id: number): Promise<Habit> {
        try {
            console.log('🔍 HabitService.getHabitById - ID:', id);

            const response = await apiClient.get(`/habits/${id}`);
            console.log('🔍 HabitService.getHabitById - Réponse:', response);

            // Si la réponse est encapsulée dans EntityModel
            if (response && typeof response === 'object' && 'content' in response) {
                return response.content;
            }

            return response;
        } catch (error) {
            console.error('❌ Erreur getHabitById:', error);
            const errorMessage = extractErrorMessage(error, 'Habitude non trouvée');
            throw new Error(errorMessage);
        }
    }

    async createHabit(habitData: CreateHabitRequest): Promise<Habit> {
        try {
            console.log('🔍 HabitService.createHabit - Données:', habitData);

            // Validation côté client
            const validationErrors = this.validateHabitData(habitData);
            if (validationErrors.length > 0) {
                throw new Error(`Données invalides: ${validationErrors.join(', ')}`);
            }

            const response = await apiClient.post('/habits', habitData);
            console.log('🔍 HabitService.createHabit - Réponse:', response);

            // Si la réponse est encapsulée dans EntityModel
            if (response && typeof response === 'object' && 'content' in response) {
                return response.content;
            }

            return response;
        } catch (error) {
            console.error('❌ Erreur createHabit:', error);
            const errorMessage = extractErrorMessage(error, 'Erreur lors de la création de l\'habitude');
            throw new Error(errorMessage);
        }
    }

    async updateHabit(id: number, habitData: UpdateHabitRequest): Promise<Habit> {
        try {
            console.log('🔍 HabitService.updateHabit - ID:', id, 'Données:', habitData);

            // Validation côté client
            const validationErrors = this.validateHabitData(habitData);
            if (validationErrors.length > 0) {
                throw new Error(`Données invalides: ${validationErrors.join(', ')}`);
            }

            const response = await apiClient.put(`/habits/${id}`, habitData);
            console.log('🔍 HabitService.updateHabit - Réponse:', response);

            // Si la réponse est encapsulée dans EntityModel
            if (response && typeof response === 'object' && 'content' in response) {
                return response.content;
            }

            return response;
        } catch (error) {
            console.error('❌ Erreur updateHabit:', error);
            const errorMessage = extractErrorMessage(error, 'Erreur lors de la mise à jour de l\'habitude');
            throw new Error(errorMessage);
        }
    }

    async deleteHabit(id: number): Promise<void> {
        try {
            console.log('🔍 HabitService.deleteHabit - ID:', id);

            await apiClient.delete(`/habits/${id}`);
            console.log('✅ HabitService.deleteHabit - Suppression réussie');
        } catch (error) {
            console.error('❌ Erreur deleteHabit:', error);
            const errorMessage = extractErrorMessage(error, 'Erreur lors de la suppression de l\'habitude');
            throw new Error(errorMessage);
        }
    }

    async toggleHabitStatus(id: number): Promise<Habit> {
        try {
            console.log('🔍 HabitService.toggleHabitStatus - ID:', id);

            const response = await apiClient.patch(`/habits/${id}/toggle`);
            console.log('🔍 HabitService.toggleHabitStatus - Réponse:', response);

            // Si la réponse est encapsulée dans EntityModel
            if (response && typeof response === 'object' && 'content' in response) {
                return response.content;
            }

            return response;
        } catch (error) {
            console.error('❌ Erreur toggleHabitStatus:', error);
            const errorMessage = extractErrorMessage(error, 'Erreur lors du changement de statut');
            throw new Error(errorMessage);
        }
    }

    async getHabitCategories(): Promise<HabitCategory[]> {
        try {
            return await apiClient.get('/habits/categories');
        } catch (error) {
            console.error('❌ Erreur getHabitCategories:', error);
            throw new Error('Erreur lors de la récupération des catégories');
        }
    }

    async getHabitFrequencies(): Promise<HabitFrequency[]> {
        try {
            return await apiClient.get('/habits/frequencies');
        } catch (error) {
            console.error('❌ Erreur getHabitFrequencies:', error);
            throw new Error('Erreur lors de la récupération des fréquences');
        }
    }

    // Méthodes utilitaires pour le frontend
    getCategoryIcon(category: HabitCategory): string {
        const icons: Record<HabitCategory, string> = {
            SPORT: '🏃‍♂️',
            SANTE: '🏥',
            EDUCATION: '📚',
            TRAVAIL: '💼',
            LIFESTYLE: '🌱',
            SOCIAL: '👥',
            CREATIVITE: '🎨',
            FINANCE: '💰',
            AUTRE: '📌',
        };
        return icons[category] || '📌';
    }

    getCategoryLabel(category: HabitCategory): string {
        const labels: Record<HabitCategory, string> = {
            SPORT: 'Sport',
            SANTE: 'Santé',
            EDUCATION: 'Éducation',
            TRAVAIL: 'Travail',
            LIFESTYLE: 'Style de vie',
            SOCIAL: 'Social',
            CREATIVITE: 'Créativité',
            FINANCE: 'Finance',
            AUTRE: 'Autre',
        };
        return labels[category] || 'Autre';
    }

    getFrequencyLabel(frequency: HabitFrequency): string {
        const labels: Record<HabitFrequency, string> = {
            DAILY: 'Quotidien',
            WEEKLY: 'Hebdomadaire',
            MONTHLY: 'Mensuel',
        };
        return labels[frequency] || 'Quotidien';
    }

    // Méthodes de validation
    validateHabitData(data: CreateHabitRequest | UpdateHabitRequest): string[] {
        const errors: string[] = [];

        if ('title' in data && data.title !== undefined) {
            if (!data.title.trim()) {
                errors.push('Le titre est obligatoire');
            } else if (data.title.length > 100) {
                errors.push('Le titre ne peut pas dépasser 100 caractères');
            }
        }

        if ('description' in data && data.description && data.description.length > 500) {
            errors.push('La description ne peut pas dépasser 500 caractères');
        }

        if ('unit' in data && data.unit !== undefined) {
            if (!data.unit.trim()) {
                errors.push('L\'unité est obligatoire');
            } else if (data.unit.length > 50) {
                errors.push('L\'unité ne peut pas dépasser 50 caractères');
            }
        }

        if ('targetValue' in data && data.targetValue !== undefined && data.targetValue <= 0) {
            errors.push('La valeur cible doit être positive');
        }

        return errors;
    }

    // Méthodes pour calculer les statistiques côté client
    calculateHabitStats(habit: Habit, progressData: any[]): Habit {
        if (!progressData || progressData.length === 0) {
            return habit;
        }

        // Calculer les statistiques basiques
        const totalProgress = progressData.length;
        const totalValue = progressData.reduce((sum, p) => sum + (p.value || 0), 0);
        const averageValue = totalValue / totalProgress;

        // Calculer le taux de completion si on a une cible
        let averageCompletion = 0;
        if (habit.targetValue && habit.targetValue > 0) {
            averageCompletion = (averageValue / habit.targetValue) * 100;
        }

        // Calculer la série actuelle
        const sortedProgress = progressData
            .sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime());

        let currentStreak = 0;
        const today = new Date();

        for (let i = 0; i < sortedProgress.length; i++) {
            const progressDate = new Date(sortedProgress[i].date);
            const expectedDate = new Date(today);
            expectedDate.setDate(expectedDate.getDate() - i);

            if (progressDate.toDateString() === expectedDate.toDateString()) {
                currentStreak++;
            } else {
                break;
            }
        }

        return {
            ...habit,
            totalProgress,
            currentStreak,
            averageCompletion,
            lastProgressDate: sortedProgress[0]?.date
        };
    }
}

export const habitService = new HabitService();