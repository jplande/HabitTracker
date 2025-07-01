// src/hooks/useApi.ts - Version corrigée avec gestion d'erreur fixée
import { useState, useCallback, useRef, useEffect } from 'react';

// Interface pour les erreurs API (remplace ApiError si non disponible)
interface ApiErrorInterface {
    message: string;
    status?: number;
    statusText?: string;
    data?: any;
}

// Classe ApiError locale si pas disponible dans apiClient
class LocalApiError extends Error implements ApiErrorInterface {
    status?: number;
    statusText?: string;
    data?: any;

    constructor(message: string, status?: number, statusText?: string, data?: any) {
        super(message);
        this.name = 'ApiError';
        this.status = status;
        this.statusText = statusText;
        this.data = data;
    }
}

// Fonction pour vérifier si c'est une erreur API
function isApiError(error: any): error is ApiErrorInterface {
    return error && typeof error === 'object' && 'message' in error;
}

// Fonction pour extraire le message d'erreur de différents types d'erreurs
function extractErrorMessage(error: any): string {
    if (typeof error === 'string') {
        return error;
    }

    if (error instanceof Error) {
        return error.message;
    }

    if (isApiError(error)) {
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

    return 'Une erreur inconnue est survenue';
}

interface UseApiState<T> {
    data: T | null;
    isLoading: boolean;
    error: string | null;
    isSuccess: boolean;
}

interface UseApiOptions {
    onSuccess?: (data: any) => void;
    onError?: (error: string) => void;
    retryCount?: number;
    retryDelay?: number;
    enableDebug?: boolean;
}

export function useApi<T = any>(options: UseApiOptions = {}) {
    const [state, setState] = useState<UseApiState<T>>({
        data: null,
        isLoading: false,
        error: null,
        isSuccess: false,
    });

    const retryCountRef = useRef(0);
    const maxRetries = options.retryCount || 0;
    const retryDelay = options.retryDelay || 1000;
    const enableDebug = options.enableDebug || process.env.NODE_ENV === 'development';

    const debugLog = useCallback((message: string, data?: any) => {
        if (enableDebug) {
            console.log(`🔍 useApi: ${message}`, data || '');
        }
    }, [enableDebug]);

    const execute = useCallback(async (apiCall: () => Promise<T>) => {
        try {
            debugLog('Début de l\'exécution');

            setState(prev => ({
                ...prev,
                isLoading: true,
                error: null,
                isSuccess: false,
            }));

            const data = await apiCall();
            debugLog('Succès de l\'API call', data);

            setState({
                data,
                isLoading: false,
                error: null,
                isSuccess: true,
            });

            retryCountRef.current = 0;
            options.onSuccess?.(data);
            return data;

        } catch (error) {
            debugLog('Erreur dans l\'API call', error);

            const errorMessage = extractErrorMessage(error);

            // Gestion des tentatives de retry
            if (retryCountRef.current < maxRetries) {
                retryCountRef.current++;
                debugLog(`Tentative de retry ${retryCountRef.current}/${maxRetries}`);

                setTimeout(() => {
                    execute(apiCall);
                }, retryDelay * retryCountRef.current);

                return;
            }

            debugLog('Échec définitif après retries', {
                error: errorMessage,
                retries: retryCountRef.current
            });

            setState({
                data: null,
                isLoading: false,
                error: errorMessage,
                isSuccess: false,
            });

            options.onError?.(errorMessage);
            throw error;
        }
    }, [maxRetries, retryDelay, options, debugLog]);

    const reset = useCallback(() => {
        debugLog('Reset du state');
        setState({
            data: null,
            isLoading: false,
            error: null,
            isSuccess: false,
        });
        retryCountRef.current = 0;
    }, [debugLog]);

    return {
        ...state,
        execute,
        reset,
        canRetry: retryCountRef.current < maxRetries,
        retryCount: retryCountRef.current,
    };
}

// Hook spécialisé pour les listes paginées
export function usePaginatedApi<T = any>(options: UseApiOptions = {}) {
    const api = useApi<{
        content: T[];
        totalPages: number;
        totalElements: number;
        pageable?: any;
    }>(options);

    const [currentPage, setCurrentPage] = useState(0);
    const [allData, setAllData] = useState<T[]>([]);
    const enableDebug = options.enableDebug || process.env.NODE_ENV === 'development';

    const debugLog = useCallback((message: string, data?: any) => {
        if (enableDebug) {
            console.log(`🔍 usePaginatedApi: ${message}`, data || '');
        }
    }, [enableDebug]);

    const loadPage = useCallback(async (
        apiCall: (page: number) => Promise<{
            content: T[];
            totalPages: number;
            totalElements: number;
            pageable?: any;
        }>,
        page: number = 0,
        append: boolean = false
    ) => {
        try {
            debugLog(`Chargement de la page ${page}`, { append });

            const result = await api.execute(() => apiCall(page));

            if (result) {
                debugLog('Page chargée avec succès', {
                    page,
                    contentLength: result.content?.length,
                    totalElements: result.totalElements
                });

                setCurrentPage(page);
                if (append && page > 0) {
                    setAllData(prev => {
                        const newData = [...prev, ...result.content];
                        debugLog('Données ajoutées (append)', {
                            previousLength: prev.length,
                            newLength: newData.length
                        });
                        return newData;
                    });
                } else {
                    debugLog('Données remplacées', { contentLength: result.content.length });
                    setAllData(result.content);
                }
            }

            return result;
        } catch (error) {
            debugLog('Erreur lors du chargement de la page', error);
            throw error;
        }
    }, [api, debugLog]);

    const loadMore = useCallback(async (
        apiCall: (page: number) => Promise<{
            content: T[];
            totalPages: number;
            totalElements: number;
            pageable?: any;
        }>
    ) => {
        if (api.data && currentPage < api.data.totalPages - 1) {
            debugLog('Chargement de la page suivante', {
                currentPage,
                totalPages: api.data.totalPages
            });
            return loadPage(apiCall, currentPage + 1, true);
        } else {
            debugLog('Pas de page suivante à charger', {
                currentPage,
                totalPages: api.data?.totalPages
            });
        }
    }, [api.data, currentPage, loadPage, debugLog]);

    const reset = useCallback(() => {
        debugLog('Reset de la pagination');
        api.reset();
        setCurrentPage(0);
        setAllData([]);
    }, [api, debugLog]);

    return {
        ...api,
        currentPage,
        allData,
        loadPage,
        loadMore,
        hasMore: api.data ? currentPage < api.data.totalPages - 1 : false,
        reset,
    };
}

// Hook pour les mutations (create, update, delete)
export function useMutation<TData = any, TVariables = any>(options: UseApiOptions = {}) {
    const [state, setState] = useState<UseApiState<TData> & { isIdle: boolean }>({
        data: null,
        isLoading: false,
        error: null,
        isSuccess: false,
        isIdle: true,
    });

    const enableDebug = options.enableDebug || process.env.NODE_ENV === 'development';

    const debugLog = useCallback((message: string, data?: any) => {
        if (enableDebug) {
            console.log(`🔍 useMutation: ${message}`, data || '');
        }
    }, [enableDebug]);

    const mutate = useCallback(async (
        apiCall: (variables: TVariables) => Promise<TData>,
        variables: TVariables
    ) => {
        try {
            debugLog('Début de mutation', variables);

            setState(prev => ({
                ...prev,
                isLoading: true,
                error: null,
                isSuccess: false,
                isIdle: false,
            }));

            const data = await apiCall(variables);
            debugLog('Mutation réussie', data);

            setState({
                data,
                isLoading: false,
                error: null,
                isSuccess: true,
                isIdle: false,
            });

            options.onSuccess?.(data);
            return data;

        } catch (error) {
            debugLog('Erreur de mutation', error);

            const errorMessage = extractErrorMessage(error);

            setState({
                data: null,
                isLoading: false,
                error: errorMessage,
                isSuccess: false,
                isIdle: false,
            });

            options.onError?.(errorMessage);
            throw error;
        }
    }, [options, debugLog]);

    const reset = useCallback(() => {
        debugLog('Reset de la mutation');
        setState({
            data: null,
            isLoading: false,
            error: null,
            isSuccess: false,
            isIdle: true,
        });
    }, [debugLog]);

    return {
        ...state,
        mutate,
        reset,
    };
}

// Hook pour charger des données au montage du composant
export function useAsyncData<T = any>(
    apiCall: () => Promise<T>,
    dependencies: any[] = [],
    options: UseApiOptions = {}
) {
    const api = useApi<T>(options);
    const enableDebug = options.enableDebug || process.env.NODE_ENV === 'development';

    const debugLog = useCallback((message: string, data?: any) => {
        if (enableDebug) {
            console.log(`🔍 useAsyncData: ${message}`, data || '');
        }
    }, [enableDebug]);

    useEffect(() => {
        debugLog('Exécution de useAsyncData', { dependencies });
        api.execute(apiCall);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, dependencies);

    return api;
}

// Hook utilitaire pour le debugging des API calls
export function useApiDebug() {
    const [logs, setLogs] = useState<Array<{
        timestamp: Date;
        message: string;
        data?: any;
    }>>([]);

    const addLog = useCallback((message: string, data?: any) => {
        setLogs(prev => [...prev.slice(-99), {
            timestamp: new Date(),
            message,
            data
        }]);
    }, []);

    const clearLogs = useCallback(() => {
        setLogs([]);
    }, []);

    return {
        logs,
        addLog,
        clearLogs,
    };
}

// Export de l'erreur API locale pour utilisation dans d'autres fichiers
export { LocalApiError as ApiError };
export type { ApiErrorInterface };