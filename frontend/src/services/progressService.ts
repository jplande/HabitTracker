// src/services/progressService.ts - Version mise à jour compatible
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

export interface Progress {
    id: number;
    userId: number;
    habitId: number;
    date: string;
    value: number;
    note?: string;
    createdAt: string;

    // Informations enrichies
    habitTitle?: string;
    habitUnit?: string;
    habitTarget?: number;
    completionPercentage?: number;
    targetReached?: boolean;
}

export interface CreateProgressRequest {
    date: string;
    value: number;
    note?: string;
}

export interface UpdateProgressRequest {
    date?: string;
    value?: number;
    note?: string;
}

export interface ProgressStats {
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

class ProgressService {
    // Gestion des progrès individuels
    async getProgress(id: number): Promise<Progress> {
        try {
            console.log('🔍 ProgressService.getProgress - ID:', id);

            const response = await apiClient.get(`/progress/${id}`);
            console.log('🔍 ProgressService.getProgress - Réponse:', response);

            // Si la réponse est encapsulée dans EntityModel
            if (response && typeof response === 'object' && 'content' in response) {
                return response.content;
            }

            return response;
        } catch (error) {
            console.error('❌ Erreur getProgress:', error);
            const errorMessage = extractErrorMessage(error, 'Progrès non trouvé');
            throw new Error(errorMessage);
        }
    }

    async updateProgress(id: number, data: UpdateProgressRequest): Promise<Progress> {
        try {
            console.log('🔍 ProgressService.updateProgress - ID:', id, 'Données:', data);

            const response = await apiClient.put(`/progress/${id}`, data);
            console.log('🔍 ProgressService.updateProgress - Réponse:', response);

            // Si la réponse est encapsulée dans EntityModel
            if (response && typeof response === 'object' && 'content' in response) {
                return response.content;
            }

            return response;
        } catch (error) {
            console.error('❌ Erreur updateProgress:', error);
            const errorMessage = extractErrorMessage(error, 'Erreur lors de la mise à jour du progrès');
            throw new Error(errorMessage);
        }
    }

    async deleteProgress(id: number): Promise<void> {
        try {
            console.log('🔍 ProgressService.deleteProgress - ID:', id);

            await apiClient.delete(`/progress/${id}`);
            console.log('✅ ProgressService.deleteProgress - Suppression réussie');
        } catch (error) {
            console.error('❌ Erreur deleteProgress:', error);
            const errorMessage = extractErrorMessage(error, 'Erreur lors de la suppression du progrès');
            throw new Error(errorMessage);
        }
    }

    // Progrès par habitude
    async getHabitProgress(habitId: number, params?: {
        page?: number;
        size?: number;
    }): Promise<PaginatedResponse<Progress>> {
        try {
            console.log('🔍 ProgressService.getHabitProgress - HabitId:', habitId, 'Params:', params);

            const response: HateoasPagedResponse<Progress> | PaginatedResponse<Progress> | Progress[] =
                await apiClient.get(`/habits/${habitId}/progress`, { params });

            console.log('🔍 ProgressService.getHabitProgress - Réponse brute:', response);

            let progressList: Progress[] = [];
            let totalElements = 0;
            let totalPages = 0;
            let pageNumber = 0;
            let pageSize = 20;

            // Cas 1: Réponse HATEOAS avec _embedded
            if (response && typeof response === 'object' && '_embedded' in response && response._embedded) {
                console.log('🔍 Structure HATEOAS détectée');
                console.log('🔍 Clés dans _embedded:', Object.keys(response._embedded));

                // Chercher la clé qui contient les données des progrès
                const possibleKeys = [
                    'progressResponseList',
                    'progressResponses',
                    'progress',
                    'progressList',
                    'content'
                ];

                let dataKey = '';
                for (const key of Object.keys(response._embedded)) {
                    if (possibleKeys.includes(key) || key.toLowerCase().includes('progress')) {
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

                    progressList = items.map((item: any) => {
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
                progressList = response.content;
                totalElements = response.totalElements || progressList.length;
                totalPages = response.totalPages || 1;
                pageNumber = response.pageable?.pageNumber || 0;
                pageSize = response.pageable?.pageSize || 20;
            }
            // Cas 3: Tableau direct
            else if (Array.isArray(response)) {
                console.log('🔍 Tableau direct détecté');
                progressList = response;
                totalElements = progressList.length;
                totalPages = 1;
                pageNumber = 0;
                pageSize = progressList.length;
            }
            // Cas 4: Structure inconnue
            else {
                console.error('❌ Structure de réponse non reconnue:', response);
                console.error('❌ Type:', typeof response);
                console.error('❌ Clés:', response && typeof response === 'object' ? Object.keys(response) : 'N/A');
                throw new Error('Structure de réponse API non reconnue');
            }

            // Validation des données extraites
            if (!Array.isArray(progressList)) {
                console.error('❌ Les progrès extraits ne sont pas un tableau:', progressList);
                progressList = [];
            }

            console.log(`✅ ${progressList.length} progrès extraits avec succès`);
            console.log('🔍 Premier progrès:', progressList[0]);

            const result: PaginatedResponse<Progress> = {
                content: progressList,
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
            console.error('❌ Erreur complète dans ProgressService.getHabitProgress:', error);
            const errorMessage = extractErrorMessage(error, 'Erreur lors de la récupération des progrès');
            throw new Error(errorMessage);
        }
    }

    async createProgress(habitId: number, data: CreateProgressRequest): Promise<Progress> {
        try {
            console.log('🔍 ProgressService.createProgress - HabitId:', habitId, 'Données:', data);

            const response = await apiClient.post(`/habits/${habitId}/progress`, data);
            console.log('🔍 ProgressService.createProgress - Réponse:', response);

            // Si la réponse est encapsulée dans EntityModel
            if (response && typeof response === 'object' && 'content' in response) {
                return response.content;
            }

            return response;
        } catch (error) {
            console.error('❌ Erreur createProgress:', error);
            const errorMessage = extractErrorMessage(error, 'Erreur lors de la création du progrès');
            throw new Error(errorMessage);
        }
    }

    async getHabitStatistics(habitId: number, days: number = 30): Promise<ProgressStats> {
        try {
            console.log('🔍 ProgressService.getHabitStatistics - HabitId:', habitId, 'Days:', days);

            const response = await apiClient.get(`/habits/${habitId}/progress/stats`, {
                params: { days }
            });
            console.log('🔍 ProgressService.getHabitStatistics - Réponse:', response);

            // Si la réponse est encapsulée dans EntityModel
            if (response && typeof response === 'object' && 'content' in response) {
                return response.content;
            }

            return response;
        } catch (error) {
            console.error('❌ Erreur getHabitStatistics:', error);
            const errorMessage = extractErrorMessage(error, 'Erreur lors de la récupération des statistiques');
            throw new Error(errorMessage);
        }
    }

    async getHabitChartData(habitId: number, days: number = 30, chartType: string = 'line'): Promise<any> {
        try {
            console.log('🔍 ProgressService.getHabitChartData - HabitId:', habitId, 'Days:', days, 'Type:', chartType);

            const response = await apiClient.get(`/habits/${habitId}/progress/charts`, {
                params: { days, chartType }
            });
            console.log('🔍 ProgressService.getHabitChartData - Réponse:', response);

            return response;
        } catch (error) {
            console.error('❌ Erreur getHabitChartData:', error);
            const errorMessage = extractErrorMessage(error, 'Erreur lors de la récupération des données de graphique');
            throw new Error(errorMessage);
        }
    }

    // Progrès par utilisateur
    async getUserProgress(userId: number, params?: {
        page?: number;
        size?: number;
    }): Promise<PaginatedResponse<Progress>> {
        try {
            console.log('🔍 ProgressService.getUserProgress - UserId:', userId, 'Params:', params);

            const response = await apiClient.get(`/users/${userId}/progress`, { params });
            console.log('🔍 ProgressService.getUserProgress - Réponse:', response);

            // Utiliser la même logique de parsing que getHabitProgress
            // Pour simplifier, on assume une structure simple ici
            if (Array.isArray(response)) {
                return {
                    content: response,
                    totalElements: response.length,
                    totalPages: 1,
                    pageable: { pageNumber: 0, pageSize: response.length },
                    last: true,
                    first: true
                };
            }

            return response;
        } catch (error) {
            console.error('❌ Erreur getUserProgress:', error);
            const errorMessage = extractErrorMessage(error, 'Erreur lors de la récupération des progrès utilisateur');
            throw new Error(errorMessage);
        }
    }

    async getTodayProgress(userId: number): Promise<Progress[]> {
        try {
            console.log('🔍 ProgressService.getTodayProgress - UserId:', userId);

            const response = await apiClient.get(`/users/${userId}/progress/today`);
            console.log('🔍 ProgressService.getTodayProgress - Réponse:', response);

            // Si la réponse est un tableau d'EntityModel
            if (Array.isArray(response)) {
                return response.map((item: any) => {
                    if (item && typeof item === 'object' && 'content' in item) {
                        return item.content;
                    }
                    return item;
                }).filter(Boolean);
            }

            return response || [];
        } catch (error) {
            console.error('❌ Erreur getTodayProgress:', error);
            const errorMessage = extractErrorMessage(error, 'Erreur lors de la récupération des progrès du jour');
            throw new Error(errorMessage);
        }
    }

    async getProgressSummary(userId: number, days: number = 7): Promise<any> {
        try {
            console.log('🔍 ProgressService.getProgressSummary - UserId:', userId, 'Days:', days);

            const response = await apiClient.get(`/users/${userId}/progress/summary`, {
                params: { days }
            });
            console.log('🔍 ProgressService.getProgressSummary - Réponse:', response);

            return response;
        } catch (error) {
            console.error('❌ Erreur getProgressSummary:', error);
            const errorMessage = extractErrorMessage(error, 'Erreur lors de la récupération du résumé des progrès');
            throw new Error(errorMessage);
        }
    }

    // Méthodes utilitaires
    formatProgressValue(progress: Progress): string {
        return `${progress.value} ${progress.habitUnit || ''}`;
    }

    calculateCompletionPercentage(progress: Progress): number {
        if (!progress.habitTarget || progress.habitTarget <= 0) {
            return 0;
        }
        return Math.round((progress.value / progress.habitTarget) * 100);
    }

    isTargetReached(progress: Progress): boolean {
        if (!progress.habitTarget || progress.habitTarget <= 0) {
            return false;
        }
        return progress.value >= progress.habitTarget;
    }

    // Méthodes pour calculer les statistiques côté client (fallback)
    calculateProgressStats(progressList: Progress[], habit: any): ProgressStats {
        if (!progressList || progressList.length === 0) {
            return {
                habitId: habit.id,
                habitTitle: habit.title,
                habitUnit: habit.unit,
                habitTarget: habit.targetValue,
                startDate: new Date().toISOString().split('T')[0],
                endDate: new Date().toISOString().split('T')[0],
                totalDays: 0,
                totalEntries: 0,
                consecutiveDays: 0,
                completionRate: 0,
                totalValue: 0,
                averageValue: 0,
                maxValue: 0,
                minValue: 0,
                lastValue: 0,
                progressTrend: 0,
                improvementDetected: false,
                daysTargetReached: 0,
                targetReachRate: 0,
                consistencyScore: 0,
                lastEntryDate: new Date().toISOString().split('T')[0],
                daysSinceLastEntry: 0
            };
        }

        const sortedProgress = [...progressList].sort((a, b) =>
            new Date(a.date).getTime() - new Date(b.date).getTime()
        );

        const totalEntries = progressList.length;
        const totalValue = progressList.reduce((sum, p) => sum + p.value, 0);
        const averageValue = totalValue / totalEntries;
        const maxValue = Math.max(...progressList.map(p => p.value));
        const minValue = Math.min(...progressList.map(p => p.value));
        const lastValue = sortedProgress[sortedProgress.length - 1]?.value || 0;

        // Calculer la série consécutive
        let consecutiveDays = 0;
        const today = new Date();
        const progressDates = new Set(progressList.map(p => p.date));

        for (let i = 0; i < 365; i++) {
            const checkDate = new Date(today);
            checkDate.setDate(checkDate.getDate() - i);
            const dateString = checkDate.toISOString().split('T')[0];

            if (progressDates.has(dateString)) {
                consecutiveDays++;
            } else {
                break;
            }
        }

        // Calculer le taux de réussite si on a un objectif
        const daysTargetReached = habit.targetValue
            ? progressList.filter(p => p.value >= habit.targetValue).length
            : totalEntries;

        const targetReachRate = totalEntries > 0 ? (daysTargetReached / totalEntries) * 100 : 0;

        const completionRate = targetReachRate;

        const startDate = sortedProgress[0]?.date || new Date().toISOString().split('T')[0];
        const endDate = sortedProgress[sortedProgress.length - 1]?.date || new Date().toISOString().split('T')[0];
        const lastEntryDate = endDate;

        // Calculer les jours depuis la dernière entrée
        const daysSinceLastEntry = Math.ceil(
            (new Date().getTime() - new Date(lastEntryDate).getTime()) / (1000 * 60 * 60 * 24)
        );

        return {
            habitId: habit.id,
            habitTitle: habit.title,
            habitUnit: habit.unit,
            habitTarget: habit.targetValue,
            startDate,
            endDate,
            totalDays: Math.ceil(
                (new Date(endDate).getTime() - new Date(startDate).getTime()) / (1000 * 60 * 60 * 24)
            ) + 1,
            totalEntries,
            consecutiveDays,
            completionRate,
            totalValue,
            averageValue,
            maxValue,
            minValue,
            lastValue,
            progressTrend: 0, // TODO: calculer la tendance
            improvementDetected: false, // TODO: détecter l'amélioration
            daysTargetReached,
            targetReachRate,
            consistencyScore: Math.min(100, (consecutiveDays / 30) * 100), // Score sur 30 jours
            lastEntryDate,
            daysSinceLastEntry
        };
    }
}

export const progressService = new ProgressService();