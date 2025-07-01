import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { Target, Mail, Lock, Eye, EyeOff } from 'lucide-react';
import { useAuth } from '../contexts/AuthContext';
import Button from '../components/atoms/Button';
import Input from '../components/atoms/Input';
import Card, { CardHeader, CardBody, CardFooter } from '../components/atoms/Card';

interface LoginFormData {
    username: string;
    password: string;
}

const Login: React.FC = () => {
    const [showPassword, setShowPassword] = useState(false);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState('');

    const { login } = useAuth();
    const navigate = useNavigate();

    const {
        register,
        handleSubmit,
        formState: { errors, isValid },
    } = useForm<LoginFormData>();

    const onSubmit = async (data: LoginFormData) => {
        setIsLoading(true);
        setError('');

        try {
            await login(data.username, data.password);
            navigate('/dashboard');
        } catch (err: any) {
            setError(err.message || 'Une erreur est survenue lors de la connexion');
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-primary-50 via-white to-accent-50 flex items-center justify-center p-4">
            {/* Background pattern */}
            <div className="absolute inset-0 opacity-5">
                <div className="absolute inset-0" style={{
                    backgroundImage: `url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23d4623a' fill-opacity='0.1'%3E%3Ccircle cx='30' cy='30' r='2'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E")`,
                }} />
            </div>

            <div className="w-full max-w-md relative z-10">
                {/* Logo et titre */}
                <div className="text-center mb-8">
                    <div className="w-20 h-20 bg-gradient-to-br from-primary-500 to-accent-500 rounded-3xl flex items-center justify-center mx-auto mb-4 shadow-medium">
                        <Target className="h-10 w-10 text-white" />
                    </div>
                    <h1 className="text-3xl font-bold text-neutral-900 mb-2">
                        Bon retour !
                    </h1>
                    <p className="text-neutral-600">
                        Connectez-vous pour continuer votre parcours
                    </p>
                </div>

                <Card variant="elevated" className="backdrop-blur-sm bg-white/80">
                    <form onSubmit={handleSubmit(onSubmit)}>
                        <CardBody className="space-y-6">
                            {/* Erreur globale */}
                            {error && (
                                <div className="bg-danger-50 border border-danger-200 rounded-xl p-4">
                                    <p className="text-danger-700 text-sm">{error}</p>
                                </div>
                            )}

                            {/* Nom d'utilisateur */}
                            <Input
                                label="Nom d'utilisateur ou email"
                                placeholder="Entrez votre nom d'utilisateur"
                                icon={<Mail className="h-5 w-5" />}
                                fullWidth
                                error={errors.username?.message}
                                {...register('username', {
                                    required: 'Le nom d\'utilisateur est obligatoire',
                                    minLength: {
                                        value: 3,
                                        message: 'Le nom d\'utilisateur doit contenir au moins 3 caractères'
                                    }
                                })}
                            />

                            {/* Mot de passe */}
                            <div>
                                <Input
                                    label="Mot de passe"
                                    type={showPassword ? 'text' : 'password'}
                                    placeholder="Entrez votre mot de passe"
                                    icon={<Lock className="h-5 w-5" />}
                                    fullWidth
                                    error={errors.password?.message}
                                    {...register('password', {
                                        required: 'Le mot de passe est obligatoire',
                                        minLength: {
                                            value: 8,
                                            message: 'Le mot de passe doit contenir au moins 8 caractères'
                                        }
                                    })}
                                />
                                <button
                                    type="button"
                                    onClick={() => setShowPassword(!showPassword)}
                                    className="absolute right-3 top-[38px] text-neutral-500 hover:text-neutral-700"
                                >
                                    {showPassword ? <EyeOff className="h-5 w-5" /> : <Eye className="h-5 w-5" />}
                                </button>
                            </div>

                            {/* Options */}
                            <div className="flex items-center justify-between text-sm">
                                <label className="flex items-center gap-2 cursor-pointer">
                                    <input
                                        type="checkbox"
                                        className="w-4 h-4 text-primary-600 bg-neutral-100 border-neutral-300 rounded focus:ring-primary-500"
                                    />
                                    <span className="text-neutral-700">Se souvenir de moi</span>
                                </label>
                                <Link
                                    to="/forgot-password"
                                    className="text-primary-600 hover:text-primary-700 font-medium"
                                >
                                    Mot de passe oublié ?
                                </Link>
                            </div>
                        </CardBody>

                        <CardFooter>
                            <div className="space-y-4 w-full">
                                {/* Bouton de connexion */}
                                <Button
                                    type="submit"
                                    variant="primary"
                                    size="lg"
                                    isLoading={isLoading}
                                    disabled={!isValid}
                                    className="w-full"
                                >
                                    Se connecter
                                </Button>

                                {/* Lien vers inscription */}
                                <div className="text-center">
                                    <span className="text-neutral-600">Pas encore de compte ? </span>
                                    <Link
                                        to="/register"
                                        className="text-primary-600 hover:text-primary-700 font-medium"
                                    >
                                        Créer un compte
                                    </Link>
                                </div>
                            </div>
                        </CardFooter>
                    </form>
                </Card>

                {/* Demo credentials */}
                <Card variant="filled" className="mt-6">
                    <CardBody>
                        <h3 className="font-semibold text-neutral-900 mb-3">
                            🚀 Comptes de démonstration
                        </h3>
                        <div className="space-y-2 text-sm">
                            <div className="flex justify-between">
                                <span className="text-neutral-600">Admin:</span>
                                <span className="font-mono text-neutral-800">admin / admin123</span>
                            </div>
                            <div className="flex justify-between">
                                <span className="text-neutral-600">Utilisateur:</span>
                                <span className="font-mono text-neutral-800">alice_novice / password123</span>
                            </div>
                        </div>
                    </CardBody>
                </Card>
            </div>
        </div>
    );
};

export default Login;