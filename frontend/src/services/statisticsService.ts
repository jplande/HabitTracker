// src/services/statisticsService.ts
import axios, { AxiosInstance } from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

export interface DashboardStats {
    totalHabits: number;
    activeHabits: number;
    completedToday: number;
    currentStreak: number;
    weeklyProgress: number;
}

export interface UserStatistics {
    habitCount: number;
    activeHabits: number;
    totalProgress: number;
    streakCount: number;
    completionRate: number;
    lastActivity: string;
    weeklyStats: {
        day: string;
        completed: number;
        total: number;
    }[];
    monthlyTrends: {
        month: string;
        progress: number;
        habits: number;
    }[];
}

class StatisticsService {
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

    async getUserStatistics(userId: number, days: number = 30): Promise<UserStatistics> {
        try {
            const response = await this.api.get(`/users/${userId}/statistics`, {
                params: { days }
            });
            return response.data;
        } catch (error: any) {
            throw new Error(error.response?.data?.message || 'Erreur lors de la récupération des statistiques');
        }
    }

    async getUserTrends(userId: number): Promise<any> {
        try {
            const response = await this.api.get(`/users/${userId}/trends`);
            return response.data;
        } catch (error: any) {
            throw new Error(error.response?.data?.message || 'Erreur lors de la récupération des tendances');
        }
    }

    async getUserDashboard(userId: number, days: number = 30): Promise<any> {
        try {
            const response = await this.api.get(`/users/${userId}/dashboard`, {
                params: { days }
            });
            return response.data;
        } catch (error: any) {
            throw new Error(error.response?.data?.message || 'Erreur lors de la récupération du dashboard');
        }
    }

    async getHabitStatistics(habitId: number, days: number = 30): Promise<any> {
        try {
            const response = await this.api.get(`/habits/${habitId}/statistics`, {
                params: { days }
            });
            return response.data;
        } catch (error: any) {
            throw new Error(error.response?.data?.message || 'Erreur lors de la récupération des statistiques de l\'habitude');
        }
    }

    async getHabitChartData(habitId: number, days: number = 30, type: string = 'line'): Promise<any> {
        try {
            const response = await this.api.get(`/habits/${habitId}/charts/${type}`, {
                params: { days }
            });
            return response.data;
        } catch (error: any) {
            throw new Error(error.response?.data?.message || 'Erreur lors de la récupération des données de graphique');
        }
    }

    // Méthodes utilitaires pour calculer les stats depuis les vraies données
    calculateDashboardStats(habits: any[], progressData: any[]): DashboardStats {
        const totalHabits = habits.length;
        const activeHabits = habits.filter(h => h.isActive).length;

        // Calculer les habitudes complétées aujourd'hui
        const today = new Date().toISOString().split('T')[0];
        const todayProgress = progressData.filter(p => p.date === today);
        const completedToday = todayProgress.length;

        // Calculer la série actuelle (simulation basée sur les données récentes)
        const recentProgress = progressData
            .sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime())
            .slice(0, 30);

        let currentStreak = 0;
        const dates = new Set(recentProgress.map(p => p.date));
        const sortedDates = Array.from(dates).sort().reverse();

        for (let i = 0; i < sortedDates.length; i++) {
            const date = new Date(sortedDates[i]);
            const expectedDate = new Date();
            expectedDate.setDate(expectedDate.getDate() - i);

            if (date.toDateString() === expectedDate.toDateString()) {
                currentStreak++;
            } else {
                break;
            }
        }

        // Calculer le progrès hebdomadaire
        const weekAgo = new Date();
        weekAgo.setDate(weekAgo.getDate() - 7);
        const weeklyProgress = progressData.filter(p =>
            new Date(p.date) >= weekAgo
        );

        const weeklyProgressRate = activeHabits > 0
            ? Math.round((weeklyProgress.length / (activeHabits * 7)) * 100)
            : 0;

        return {
            totalHabits,
            activeHabits,
            completedToday,
            currentStreak,
            weeklyProgress: weeklyProgressRate
        };
    }
}

export const statisticsService = new StatisticsService();