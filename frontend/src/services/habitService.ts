import axios, { AxiosInstance } from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

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

class HabitService {
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

    async getHabits(params?: {
        page?: number;
        size?: number;
        search?: string;
        category?: HabitCategory;
        active?: boolean;
    }): Promise<PaginatedResponse<Habit>> {
        try {
            const response = await this.api.get('/habits', { params });
            return response.data;
        } catch (error: any) {
            throw new Error(error.response?.data?.message || 'Erreur lors de la récupération des habitudes');
        }
    }

    async getHabitById(id: number): Promise<Habit> {
        try {
            const response = await this.api.get(`/habits/${id}`);
            return response.data;
        } catch (error: any) {
            throw new Error(error.response?.data?.message || 'Habitude non trouvée');
        }
    }

    async createHabit(habitData: CreateHabitRequest): Promise<Habit> {
        try {
            const response = await this.api.post('/habits', habitData);
            return response.data;
        } catch (error: any) {
            throw new Error(error.response?.data?.message || 'Erreur lors de la création de l\'habitude');
        }
    }

    async updateHabit(id: number, habitData: UpdateHabitRequest): Promise<Habit> {
        try {
            const response = await this.api.put(`/habits/${id}`, habitData);
            return response.data;
        } catch (error: any) {
            throw new Error(error.response?.data?.message || 'Erreur lors de la mise à jour de l\'habitude');
        }
    }

    async deleteHabit(id: number): Promise<void> {
        try {
            await this.api.delete(`/habits/${id}`);
        } catch (error: any) {
            throw new Error(error.response?.data?.message || 'Erreur lors de la suppression de l\'habitude');
        }
    }

    async toggleHabitStatus(id: number): Promise<Habit> {
        try {
            const response = await this.api.patch(`/habits/${id}/toggle`);
            return response.data;
        } catch (error: any) {
            throw new Error(error.response?.data?.message || 'Erreur lors du changement de statut');
        }
    }

    async getHabitCategories(): Promise<HabitCategory[]> {
        try {
            const response = await this.api.get('/habits/categories');
            return response.data;
        } catch (error: any) {
            throw new Error('Erreur lors de la récupération des catégories');
        }
    }

    async getHabitFrequencies(): Promise<HabitFrequency[]> {
        try {
            const response = await this.api.get('/habits/frequencies');
            return response.data;
        } catch (error: any) {
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
}

export const habitService = new HabitService();