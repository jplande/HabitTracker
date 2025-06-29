import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './contexts/AuthContext';
import ErrorBoundary from './components/ErrorBoundary';

// Pages
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import Habits from './pages/Habits';

// Styles
import './index.css';

// Composant pour protéger les routes
const ProtectedRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const { isAuthenticated, isLoading } = useAuth();

    if (isLoading) {
        return (
            <div className="min-h-screen bg-neutral-50 flex items-center justify-center">
                <div className="animate-spin rounded-full h-16 w-16 border-b-2 border-primary-600"></div>
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
                <div className="animate-spin rounded-full h-16 w-16 border-b-2 border-primary-600"></div>
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
                <p className="text-neutral-600">Cette page sera bientôt disponible !</p>
            </div>
        </div>
    );
};

function App() {
    return (
        <ErrorBoundary>
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
                                        <Habits />
                                    </ProtectedRoute>
                                }
                            />
                            <Route
                                path="/progress"
                                element={
                                    <ProtectedRoute>
                                        <ComingSoon title="Progression" />
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
                                        <ComingSoon title="Profil Utilisateur" />
                                    </ProtectedRoute>
                                }
                            />
                            <Route
                                path="/settings"
                                element={
                                    <ProtectedRoute>
                                        <ComingSoon title="Paramètres" />
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
                                            <button
                                                onClick={() => window.history.back()}
                                                className="px-6 py-3 bg-primary-600 text-white rounded-xl hover:bg-primary-700 transition-colors"
                                            >
                                                Retour
                                            </button>
                                        </div>
                                    </div>
                                }
                            />
                        </Routes>
                    </div>
                </Router>
            </AuthProvider>
        </ErrorBoundary>
            );
            }

            export default App;