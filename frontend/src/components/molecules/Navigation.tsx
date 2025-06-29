import React, { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import {
    Home,
    Target,
    TrendingUp,
    Trophy,
    User,
    LogOut,
    Menu,
    X,
    Settings
} from 'lucide-react';
import { useAuth } from '../../contexts/AuthContext';
import Button from '../atoms/Button';
import Badge from '../atoms/Badge';

const Navigation: React.FC = () => {
    const { user, logout } = useAuth();
    const location = useLocation();
    const navigate = useNavigate();
    const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    const navItems = [
        { path: '/dashboard', icon: Home, label: 'Tableau de bord' },
        { path: '/habits', icon: Target, label: 'Mes habitudes' },
        { path: '/progress', icon: TrendingUp, label: 'Progression' },
        { path: '/achievements', icon: Trophy, label: 'Badges' },
        { path: '/profile', icon: User, label: 'Profil' },
    ];

    const isActiveRoute = (path: string) => {
        return location.pathname === path || location.pathname.startsWith(path + '/');
    };

    const NavLink: React.FC<{ item: any; mobile?: boolean }> = ({ item, mobile = false }) => (
        <Link
            to={item.path}
            onClick={() => mobile && setIsMobileMenuOpen(false)}
            className={`
        flex items-center gap-3 px-3 py-2 rounded-xl transition-all duration-200
        ${isActiveRoute(item.path)
                ? 'bg-primary-100 text-primary-700 shadow-soft'
                : 'text-neutral-600 hover:bg-neutral-100 hover:text-neutral-900'
            }
        ${mobile ? 'w-full text-lg' : ''}
      `}
        >
            <item.icon className={`${mobile ? 'h-6 w-6' : 'h-5 w-5'}`} />
            <span className={mobile ? 'font-medium' : 'hidden lg:block font-medium'}>
        {item.label}
      </span>
        </Link>
    );

    return (
        <>
            {/* Navigation Desktop */}
            <nav className="hidden md:flex flex-col h-screen w-64 bg-white border-r border-neutral-200 p-6">
                {/* Logo */}
                <div className="flex items-center gap-3 mb-8">
                    <div className="w-10 h-10 bg-primary-500 rounded-xl flex items-center justify-center">
                        <Target className="h-6 w-6 text-white" />
                    </div>
                    <div>
                        <h1 className="font-bold text-xl text-neutral-900">HabitTracker</h1>
                        <p className="text-sm text-neutral-500">Construisez vos habitudes</p>
                    </div>
                </div>

                {/* Menu principal */}
                <div className="flex-1">
                    <div className="space-y-2">
                        {navItems.map((item) => (
                            <NavLink key={item.path} item={item} />
                        ))}
                    </div>

                    {/* Séparateur */}
                    <div className="my-6 border-t border-neutral-200" />

                    {/* Menu secondaire */}
                    <div className="space-y-2">
                        <Link
                            to="/settings"
                            className={`
                flex items-center gap-3 px-3 py-2 rounded-xl transition-all duration-200
                ${isActiveRoute('/settings')
                                ? 'bg-primary-100 text-primary-700'
                                : 'text-neutral-600 hover:bg-neutral-100 hover:text-neutral-900'
                            }
              `}
                        >
                            <Settings className="h-5 w-5" />
                            <span className="hidden lg:block font-medium">Paramètres</span>
                        </Link>
                    </div>
                </div>

                {/* Profil utilisateur */}
                <div className="border-t border-neutral-200 pt-6">
                    <div className="flex items-center gap-3 mb-4">
                        <div className="w-10 h-10 bg-gradient-to-br from-primary-400 to-accent-500 rounded-full flex items-center justify-center">
              <span className="text-white font-semibold">
                {user?.username?.charAt(0).toUpperCase()}
              </span>
                        </div>
                        <div className="hidden lg:block flex-1">
                            <p className="font-medium text-neutral-900">{user?.username}</p>
                            <p className="text-sm text-neutral-500">{user?.email}</p>
                        </div>
                    </div>

                    <Button
                        variant="ghost"
                        size="sm"
                        onClick={handleLogout}
                        className="w-full justify-start text-neutral-600 hover:text-danger-600"
                    >
                        <LogOut className="h-4 w-4" />
                        <span className="hidden lg:block ml-2">Déconnexion</span>
                    </Button>
                </div>
            </nav>

            {/* Navigation Mobile */}
            <div className="md:hidden">
                {/* Header mobile */}
                <header className="bg-white border-b border-neutral-200 px-4 py-4 flex items-center justify-between">
                    <div className="flex items-center gap-3">
                        <div className="w-8 h-8 bg-primary-500 rounded-lg flex items-center justify-center">
                            <Target className="h-5 w-5 text-white" />
                        </div>
                        <h1 className="font-bold text-lg text-neutral-900">HabitTracker</h1>
                    </div>

                    <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
                    >
                        {isMobileMenuOpen ? <X className="h-6 w-6" /> : <Menu className="h-6 w-6" />}
                    </Button>
                </header>

                {/* Menu mobile overlay */}
                {isMobileMenuOpen && (
                    <div className="fixed inset-0 z-50 bg-black bg-opacity-50" onClick={() => setIsMobileMenuOpen(false)}>
                        <div
                            className="absolute right-0 top-0 h-full w-80 bg-white p-6 shadow-strong"
                            onClick={(e) => e.stopPropagation()}
                        >
                            {/* Header du menu mobile */}
                            <div className="flex items-center justify-between mb-8">
                                <div className="flex items-center gap-3">
                                    <div className="w-10 h-10 bg-gradient-to-br from-primary-400 to-accent-500 rounded-full flex items-center justify-center">
                    <span className="text-white font-semibold">
                      {user?.username?.charAt(0).toUpperCase()}
                    </span>
                                    </div>
                                    <div>
                                        <p className="font-medium text-neutral-900">{user?.username}</p>
                                        <Badge variant="primary" size="sm">{user?.role}</Badge>
                                    </div>
                                </div>
                                <Button
                                    variant="ghost"
                                    size="sm"
                                    onClick={() => setIsMobileMenuOpen(false)}
                                >
                                    <X className="h-6 w-6" />
                                </Button>
                            </div>

                            {/* Menu items */}
                            <div className="space-y-2 mb-8">
                                {navItems.map((item) => (
                                    <NavLink key={item.path} item={item} mobile />
                                ))}

                                <div className="my-4 border-t border-neutral-200" />

                                <Link
                                    to="/settings"
                                    onClick={() => setIsMobileMenuOpen(false)}
                                    className={`
                    flex items-center gap-3 px-3 py-2 rounded-xl transition-all duration-200 w-full text-lg
                    ${isActiveRoute('/settings')
                                        ? 'bg-primary-100 text-primary-700'
                                        : 'text-neutral-600 hover:bg-neutral-100 hover:text-neutral-900'
                                    }
                  `}
                                >
                                    <Settings className="h-6 w-6" />
                                    <span className="font-medium">Paramètres</span>
                                </Link>
                            </div>

                            {/* Déconnexion */}
                            <Button
                                variant="outline"
                                size="lg"
                                onClick={handleLogout}
                                className="w-full justify-center text-danger-600 border-danger-200 hover:bg-danger-50"
                            >
                                <LogOut className="h-5 w-5" />
                                <span className="ml-2">Déconnexion</span>
                            </Button>
                        </div>
                    </div>
                )}

                {/* Bottom navigation mobile (alternative) */}
                <nav className="fixed bottom-0 left-0 right-0 bg-white border-t border-neutral-200 px-4 py-2">
                    <div className="flex items-center justify-around">
                        {navItems.slice(0, 4).map((item) => (
                            <Link
                                key={item.path}
                                to={item.path}
                                className={`
                  flex flex-col items-center gap-1 p-2 rounded-lg transition-all duration-200
                  ${isActiveRoute(item.path)
                                    ? 'text-primary-600'
                                    : 'text-neutral-500 hover:text-neutral-700'
                                }
                `}
                            >
                                <item.icon className="h-5 w-5" />
                                <span className="text-xs font-medium">{item.label}</span>
                            </Link>
                        ))}
                        <button
                            onClick={() => setIsMobileMenuOpen(true)}
                            className="flex flex-col items-center gap-1 p-2 rounded-lg transition-all duration-200 text-neutral-500 hover:text-neutral-700"
                        >
                            <Menu className="h-5 w-5" />
                            <span className="text-xs font-medium">Menu</span>
                        </button>
                    </div>
                </nav>
            </div>
        </>
    );
};

export default Navigation;