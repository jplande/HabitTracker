// src/App.tsx
import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './contexts/AuthContext';
import { NotificationProvider } from './components/organisms/NotificationProvider';
import ErrorBoundary from './components/ErrorBoundary';

// Pages
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import Habits from './pages/Habits';
import Progress from './pages/Progress';
import Profile from './pages/Profile';
import Settings from './pages/Settings';

// Styles
import './index.css';

// Composant pour protéger les routes
const ProtectedRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const { isAuthenticated, isLoading } = useAuth();

    if (isLoading) {
        return (
            <div className="min-h-screen bg-neutral-50 flex items-center justify-center">
                <div className="text-center">
                    <div className="animate-spin rounded-full h-16 w-16 border-b-2 border-primary-600 mx-auto mb-4"></div>
                    <p className="text-neutral-600">Chargement...</p>
                </div>
            </div>
        );
    }

    return isAuthenticated ? <>{children}</> : <Navigate to="/login" replace />;
};

// Composant pour rediriger les utilisateurs connectés
const PublicRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const { isAuthenticated, isLoading } = useAuth();

    if (isLoading) {
        return (
            <div className="min-h-screen bg-neutral-50 flex items-center justify-center">
                <div className="text-center">
                    <div className="animate-spin rounded-full h-16 w-16 border-b-2 border-primary-600 mx-auto mb-4"></div>
                    <p className="text-neutral-600">Chargement...</p>
                </div>
            </div>
        );
    }

    return isAuthenticated ? <Navigate to="/dashboard" replace /> : <>{children}</>;
};

// Pages temporaires pour les routes manquantes
const ComingSoon: React.FC<{ title: string }> = ({ title }) => {
    return (
        <div className="min-h-screen bg-neutral-50 flex items-center justify-center">
            <div className="text-center">
                <div className="text-6xl mb-4">🚧</div>
                <h1 className="text-2xl font-bold text-neutral-900 mb-2">{title}</h1>
                <p className="text-neutral-600 mb-6">Cette page sera bientôt disponible !</p>
                <button
                    onClick={() => window.history.back()}
                    className="px-6 py-3 bg-primary-600 text-white rounded-xl hover:bg-primary-700 transition-colors"
                >
                    Retour
                </button>
            </div>
        </div>
    );
};

function App() {
    return (
        <ErrorBoundary>
            <NotificationProvider>
                <AuthProvider>
                    <Router>
                        <div className="App">
                            <Routes>
                                {/* Routes publiques */}
                                <Route
                                    path="/login"
                                    element={
                                        <PublicRoute>
                                            <Login />
                                        </PublicRoute>
                                    }
                                />
                                <Route
                                    path="/register"
                                    element={
                                        <PublicRoute>
                                            <Register />
                                        </PublicRoute>
                                    }
                                />

                                {/* Routes protégées */}
                                <Route
                                    path="/dashboard"
                                    element={
                                        <ProtectedRoute>
                                            <ErrorBoundary>
                                                <Dashboard />
                                            </ErrorBoundary>
                                        </ProtectedRoute>
                                    }
                                />
                                <Route
                                    path="/habits"
                                    element={
                                        <ProtectedRoute>
                                            <ErrorBoundary>
                                                <Habits />
                                            </ErrorBoundary>
                                        </ProtectedRoute>
                                    }
                                />
                                <Route
                                    path="/habits/:id/edit"
                                    element={
                                        <ProtectedRoute>
                                            <ErrorBoundary>
                                                <Habits />
                                            </ErrorBoundary>
                                        </ProtectedRoute>
                                    }
                                />
                                <Route
                                    path="/progress"
                                    element={
                                        <ProtectedRoute>
                                            <ErrorBoundary>
                                                <Progress />
                                            </ErrorBoundary>
                                        </ProtectedRoute>
                                    }
                                />
                                <Route
                                    path="/achievements"
                                    element={
                                        <ProtectedRoute>
                                            <ComingSoon title="Badges et Réalisations" />
                                        </ProtectedRoute>
                                    }
                                />
                                <Route
                                    path="/profile"
                                    element={
                                        <ProtectedRoute>
                                            <ErrorBoundary>
                                                <Profile />
                                            </ErrorBoundary>
                                        </ProtectedRoute>
                                    }
                                />
                                <Route
                                    path="/settings"
                                    element={
                                        <ProtectedRoute>
                                            <ErrorBoundary>
                                                <Settings />
                                            </ErrorBoundary>
                                        </ProtectedRoute>
                                    }
                                />

                                {/* Routes de redirection */}
                                <Route path="/" element={<Navigate to="/dashboard" replace />} />

                                {/* Route 404 */}
                                <Route
                                    path="*"
                                    element={
                                        <div className="min-h-screen bg-neutral-50 flex items-center justify-center">
                                            <div className="text-center">
                                                <div className="text-6xl mb-4">🔍</div>
                                                <h1 className="text-2xl font-bold text-neutral-900 mb-2">Page non trouvée</h1>
                                                <p className="text-neutral-600 mb-6">
                                                    La page que vous recherchez n'existe pas.
                                                </p>
                                                <div className="flex gap-3 justify-center">
                                                    <button
                                                        onClick={() => window.history.back()}
                                                        className="px-6 py-3 bg-primary-600 text-white rounded-xl hover:bg-primary-700 transition-colors"
                                                    >
                                                        Retour
                                                    </button>
                                                    <button
                                                        onClick={() => window.location.href = '/dashboard'}
                                                        className="px-6 py-3 bg-neutral-200 text-neutral-700 rounded-xl hover:bg-neutral-300 transition-colors"
                                                    >
                                                        Tableau de bord
                                                    </button>
                                                </div>
                                            </div>
                                        </div>
                                    }
                                />
                            </Routes>
                        </div>
                    </Router>
                </AuthProvider>
            </NotificationProvider>
        </ErrorBoundary>
    );
}

export default App;