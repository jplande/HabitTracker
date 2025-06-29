import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { Target, Mail, Lock, User, Eye, EyeOff } from 'lucide-react';
import { useAuth } from '../contexts/AuthContext';
import Button from '../components/atoms/Button';
import Input from '../components/atoms/Input';
import Card, { CardHeader, CardBody, CardFooter } from '../components/atoms/Card';

interface RegisterFormData {
    username: string;
    email: string;
    password: string;
    confirmPassword: string;
    firstName: string;
    lastName: string;
}

const Register: React.FC = () => {
    const [showPassword, setShowPassword] = useState(false);
    const [showConfirmPassword, setShowConfirmPassword] = useState(false);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState('');

    const { register: registerUser } = useAuth();
    const navigate = useNavigate();

    const {
        register,
        handleSubmit,
        formState: { errors, isValid },
        watch,
    } = useForm<RegisterFormData>();

    const password = watch('password');

    const onSubmit = async (data: RegisterFormData) => {
        setIsLoading(true);
        setError('');

        try {
            await registerUser({
                username: data.username,
                email: data.email,
                password: data.password,
                firstName: data.firstName,
                lastName: data.lastName,
            });
            navigate('/dashboard');
        } catch (err: any) {
            setError(err.message || 'Une erreur est survenue lors de l\'inscription');
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

            <div className="w-full max-w-lg relative z-10">
                {/* Logo et titre */}
                <div className="text-center mb-8">
                    <div className="w-20 h-20 bg-gradient-to-br from-primary-500 to-accent-500 rounded-3xl flex items-center justify-center mx-auto mb-4 shadow-medium">
                        <Target className="h-10 w-10 text-white" />
                    </div>
                    <h1 className="text-3xl font-bold text-neutral-900 mb-2">
                        Créez votre compte
                    </h1>
                    <p className="text-neutral-600">
                        Commencez votre parcours vers de meilleures habitudes
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

                            {/* Prénom et Nom */}
                            <div className="grid grid-cols-2 gap-4">
                                <Input
                                    label="Prénom"
                                    placeholder="John"
                                    fullWidth
                                    error={errors.firstName?.message}
                                    {...register('firstName', {
                                        required: 'Le prénom est obligatoire',
                                        minLength: {
                                            value: 2,
                                            message: 'Le prénom doit contenir au moins 2 caractères'
                                        },
                                        maxLength: {
                                            value: 50,
                                            message: 'Le prénom ne peut pas dépasser 50 caractères'
                                        }
                                    })}
                                />

                                <Input
                                    label="Nom"
                                    placeholder="Doe"
                                    fullWidth
                                    error={errors.lastName?.message}
                                    {...register('lastName', {
                                        required: 'Le nom est obligatoire',
                                        minLength: {
                                            value: 2,
                                            message: 'Le nom doit contenir au moins 2 caractères'
                                        },
                                        maxLength: {
                                            value: 50,
                                            message: 'Le nom ne peut pas dépasser 50 caractères'
                                        }
                                    })}
                                />
                            </div>

                            {/* Nom d'utilisateur */}
                            <Input
                                label="Nom d'utilisateur"
                                placeholder="johndoe"
                                icon={<User className="h-5 w-5" />}
                                fullWidth
                                error={errors.username?.message}
                                {...register('username', {
                                    required: 'Le nom d\'utilisateur est obligatoire',
                                    minLength: {
                                        value: 3,
                                        message: 'Le nom d\'utilisateur doit contenir au moins 3 caractères'
                                    },
                                    maxLength: {
                                        value: 20,
                                        message: 'Le nom d\'utilisateur ne peut pas dépasser 20 caractères'
                                    },
                                    pattern: {
                                        value: /^[a-zA-Z0-9_]+$/,
                                        message: 'Le nom d\'utilisateur ne peut contenir que des lettres, chiffres et underscores'
                                    }
                                })}
                            />

                            {/* Email */}
                            <Input
                                label="Adresse email"
                                type="email"
                                placeholder="john.doe@example.com"
                                icon={<Mail className="h-5 w-5" />}
                                fullWidth
                                error={errors.email?.message}
                                {...register('email', {
                                    required: 'L\'email est obligatoire',
                                    pattern: {
                                        value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
                                        message: 'Veuillez entrer une adresse email valide'
                                    }
                                })}
                            />

                            {/* Mot de passe */}
                            <div className="relative">
                                <Input
                                    label="Mot de passe"
                                    type={showPassword ? 'text' : 'password'}
                                    placeholder="Choisissez un mot de passe fort"
                                    icon={<Lock className="h-5 w-5" />}
                                    fullWidth
                                    error={errors.password?.message}
                                    {...register('password', {
                                        required: 'Le mot de passe est obligatoire',
                                        minLength: {
                                            value: 8,
                                            message: 'Le mot de passe doit contenir au moins 8 caractères'
                                        },
                                        pattern: {
                                            value: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/,
                                            message: 'Le mot de passe doit contenir au moins une majuscule, une minuscule et un chiffre'
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

                            {/* Confirmation mot de passe */}
                            <div className="relative">
                                <Input
                                    label="Confirmer le mot de passe"
                                    type={showConfirmPassword ? 'text' : 'password'}
                                    placeholder="Répétez votre mot de passe"
                                    icon={<Lock className="h-5 w-5" />}
                                    fullWidth
                                    error={errors.confirmPassword?.message}
                                    {...register('confirmPassword', {
                                        required: 'Veuillez confirmer votre mot de passe',
                                        validate: (value) =>
                                            value === password || 'Les mots de passe ne correspondent pas'
                                    })}
                                />
                                <button
                                    type="button"
                                    onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                                    className="absolute right-3 top-[38px] text-neutral-500 hover:text-neutral-700"
                                >
                                    {showConfirmPassword ? <EyeOff className="h-5 w-5" /> : <Eye className="h-5 w-5" />}
                                </button>
                            </div>

                            {/* Conditions d'utilisation */}
                            <div className="flex items-start gap-3">
                                <input
                                    type="checkbox"
                                    required
                                    className="w-4 h-4 mt-1 text-primary-600 bg-neutral-100 border-neutral-300 rounded focus:ring-primary-500"
                                />
                                <p className="text-sm text-neutral-600">
                                    J'accepte les{' '}
                                    <Link to="/terms" className="text-primary-600 hover:text-primary-700 font-medium">
                                        conditions d'utilisation
                                    </Link>{' '}
                                    et la{' '}
                                    <Link to="/privacy" className="text-primary-600 hover:text-primary-700 font-medium">
                                        politique de confidentialité
                                    </Link>
                                </p>
                            </div>
                        </CardBody>

                        <CardFooter>
                            <div className="space-y-4 w-full">
                                {/* Bouton d'inscription */}
                                <Button
                                    type="submit"
                                    variant="primary"
                                    size="lg"
                                    isLoading={isLoading}
                                    disabled={!isValid}
                                    className="w-full"
                                >
                                    Créer mon compte
                                </Button>

                                {/* Lien vers connexion */}
                                <div className="text-center">
                                    <span className="text-neutral-600">Déjà un compte ? </span>
                                    <Link
                                        to="/login"
                                        className="text-primary-600 hover:text-primary-700 font-medium"
                                    >
                                        Se connecter
                                    </Link>
                                </div>
                            </div>
                        </CardFooter>
                    </form>
                </Card>
            </div>
        </div>
    );
};

export default Register;