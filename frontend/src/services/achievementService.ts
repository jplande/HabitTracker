// src/services/achievementService.ts
import axios, { AxiosInstance } from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

export interface Achievement {
    id: number;
    userId: number;
    name: string;
    description: string;
    icon: string;
    achievementType: string;
    unlockedAt: string;

    // Informations enrichies
    isNew?: boolean;
    rarity?: number;
    category?: string;
}

export interface AchievementSummary {
    userId: number;
    username: string;

    // Statistiques globales
    totalAchievements: number;
    totalPossibleAchievements: number;
    completionPercentage: number;

    // Par type
    achievementsByType: { [key: string]: number };

    // Récents
    lastAchievementDate?: string;
    lastAchievementName?: string;
    achievementsThisWeek: number;
    achievementsThisMonth: number;

    // Raretés
    commonAchievements: number;
    rareAchievements: number;
    epicAchievements: number;
    legendaryAchievements: number;

    // Progression
    nextPossibleAchievement?: string;
    progressToNextAchievement: number;
}

export interface AchievementCheckRequest {
    userId: number;
    habitId?: number;
    triggerType?: string;
}

export interface AchievementCheckResponse {
    userId: number;
    totalChecked: number;
    newAchievementsUnlocked: number;
    newAchievements: Achievement[];
    message: string;
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

class AchievementService {
    private api: AxiosInstance;

    constructor() {
        this.api = axios.create({
            baseURL: API_BASE_URL,
            headers: {
                'Content-Type': 'application/json',
            },
        });

        // Intercepteur pour ajouter le token d'authentification
        this.api.interceptors.request.use(
            (config) => {
                const token = localStorage.getItem('accessToken');
                if (token) {
                    config.headers.Authorization = `Bearer ${token}`;
                }
                return config;
            },
            (error) => Promise.reject(error)
        );
    }

    // Récupération des achievements
    async getUserAchievements(userId: number, params?: {
        page?: number;
        size?: number;
    }): Promise<PaginatedResponse<Achievement>> {
        try {
            const response = await this.api.get(`/users/${userId}/achievements`, { params });
            return response.data;
        } catch (error: any) {
            throw new Error(error.response?.data?.message || 'Erreur lors de la récupération des badges');
        }
    }

    async getRecentAchievements(userId: number, days: number = 7): Promise<Achievement[]> {
        try {
            const response = await this.api.get(`/users/${userId}/achievements/recent`, {
                params: { days }
            });
            return response.data;
        } catch (error: any) {
            throw new Error(error.response?.data?.message || 'Erreur lors de la récupération des badges récents');
        }
    }

    async getUserSummary(userId: number): Promise<AchievementSummary> {
        try {
            const response = await this.api.get(`/users/${userId}/achievements/summary`);
            return response.data;
        } catch (error: any) {
            throw new Error(error.response?.data?.message || 'Erreur lors de la récupération du résumé des badges');
        }
    }

    // Vérification et déblocage des achievements
    async checkAchievements(request: AchievementCheckRequest): Promise<AchievementCheckResponse> {
        try {
            const response = await this.api.post('/achievements/check', request);
            return response.data;
        } catch (error: any) {
            throw new Error(error.response?.data?.message || 'Erreur lors de la vérification des badges');
        }
    }

    // Utilitaires
    async getAchievementTypes(): Promise<string[]> {
        try {
            const response = await this.api.get('/achievements/types');
            return response.data;
        } catch (error: any) {
            throw new Error('Erreur lors de la récupération des types de badges');
        }
    }

    // Méthodes utilitaires pour le frontend
    getAchievementIcon(type: string): string {
        const icons: { [key: string]: string } = {
            'CONSISTENCY': '🎯',
            'MILESTONE': '🏆',
            'STREAK': '🔥',
            'DEDICATION': '💪',
            'OVERACHIEVER': '⭐',
            'VARIETY': '🌈',
            'EARLY_BIRD': '🌅',
            'PERSEVERANCE': '🗿'
        };
        return icons[type] || '🏅';
    }

    getAchievementTypeLabel(type: string): string {
        const labels: { [key: string]: string } = {
            'CONSISTENCY': 'Constance',
            'MILESTONE': 'Étape importante',
            'STREAK': 'Série',
            'DEDICATION': 'Dévouement',
            'OVERACHIEVER': 'Dépassement',
            'VARIETY': 'Variété',
            'EARLY_BIRD': 'Lève-tôt',
            'PERSEVERANCE': 'Persévérance'
        };
        return labels[type] || 'Badge';
    }

    getRarityLabel(rarity: number): string {
        if (rarity >= 90) return 'Commun';
        if (rarity >= 70) return 'Peu commun';
        if (rarity >= 40) return 'Rare';
        if (rarity >= 15) return 'Épique';
        return 'Légendaire';
    }

    getRarityColor(rarity: number): string {
        if (rarity >= 90) return 'neutral';
        if (rarity >= 70) return 'primary';
        if (rarity >= 40) return 'secondary';
        if (rarity >= 15) return 'warning';
        return 'danger';
    }

    formatAchievementDate(dateString: string): string {
        const date = new Date(dateString);
        const now = new Date();
        const diffTime = Math.abs(now.getTime() - date.getTime());
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

        if (diffDays === 1) return 'Hier';
        if (diffDays < 7) return `Il y a ${diffDays} jour${diffDays > 1 ? 's' : ''}`;
        if (diffDays < 30) return `Il y a ${Math.ceil(diffDays / 7)} semaine${Math.ceil(diffDays / 7) > 1 ? 's' : ''}`;
        if (diffDays < 365) return `Il y a ${Math.ceil(diffDays / 30)} mois`;
        return `Il y a ${Math.ceil(diffDays / 365)} an${Math.ceil(diffDays / 365) > 1 ? 's' : ''}`;
    }
}

export const achievementService = new AchievementService();