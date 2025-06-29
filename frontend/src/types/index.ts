// Types pour les utilisateurs
export interface User {
    id: number;
    username: string;
    email: string;
    firstName?: string;
    lastName?: string;
    role: 'USER' | 'ADMIN';
    createdAt: string;
}

// Types pour l'authentification
export interface AuthResponse {
    accessToken: string;
    refreshToken: string;
    tokenType: string;
    expiresIn: number;
    user: User;
}

export interface LoginRequest {
    username: string;
    password: string;
}

export interface RegisterRequest {
    username: string;
    email: string;
    password: string;
    firstName?: string;
    lastName?: string;
}

// Types pour les habitudes
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

// Types pour les réponses paginées
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

// Types pour les progrès
export interface Progress {
    id: number;
    userId: number;
    habitId: number;
    date: string;
    value: number;
    note?: string;
    createdAt: string;
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

// Types pour les badges/réalisations
export interface Achievement {
    id: number;
    userId: number;
    name: string;
    description: string;
    icon: string;
    achievementType: string;
    unlockedAt: string;
}

// Types utilitaires
export interface ApiError {
    timestamp: string;
    status: number;
    error: string;
    message: string;
    path: string;
}

// Types pour les statistiques
export interface DashboardStats {
    totalHabits: number;
    activeHabits: number;
    completedToday: number;
    currentStreak: number;
    weeklyProgress: number;
}