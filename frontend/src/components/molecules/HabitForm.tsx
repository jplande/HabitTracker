import React, { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { X } from 'lucide-react';
import Button from '../atoms/Button';
import Input from '../atoms/Input';
import Card, { CardHeader, CardBody, CardFooter } from '../atoms/Card';
import {
    CreateHabitRequest,
    UpdateHabitRequest,
    Habit,
    HabitCategory,
    HabitFrequency,
    habitService
} from '../../services/habitService';

interface HabitFormProps {
    habit?: Habit; // Pour l'édition
    onSubmit: (data: CreateHabitRequest | UpdateHabitRequest) => Promise<void>;
    onCancel: () => void;
    isLoading?: boolean;
}

interface FormData {
    title: string;
    description: string;
    category: HabitCategory;
    unit: string;
    frequency: HabitFrequency;
    targetValue: number | '';
}

const HabitForm: React.FC<HabitFormProps> = ({
                                                 habit,
                                                 onSubmit,
                                                 onCancel,
                                                 isLoading = false,
                                             }) => {
    const isEditing = !!habit;

    const {
        register,
        handleSubmit,
        formState: { errors, isValid },
        reset,
        watch,
    } = useForm<FormData>({
        defaultValues: {
            title: habit?.title || '',
            description: habit?.description || '',
            category: habit?.category || 'AUTRE',
            unit: habit?.unit || '',
            frequency: habit?.frequency || 'DAILY',
            targetValue: habit?.targetValue || '',
        },
    });

    const selectedCategory = watch('category');

    // Réinitialiser le formulaire si l'habitude change
    useEffect(() => {
        if (habit) {
            reset({
                title: habit.title,
                description: habit.description || '',
                category: habit.category,
                unit: habit.unit,
                frequency: habit.frequency,
                targetValue: habit.targetValue || '',
            });
        }
    }, [habit, reset]);

    const handleFormSubmit = async (data: FormData) => {
        const submitData = {
            ...data,
            targetValue: data.targetValue === '' ? undefined : Number(data.targetValue),
        };

        await onSubmit(submitData);
    };

    const categories: { value: HabitCategory; label: string; icon: string }[] = [
        { value: 'SPORT', label: 'Sport', icon: '🏃‍♂️' },
        { value: 'SANTE', label: 'Santé', icon: '🏥' },
        { value: 'EDUCATION', label: 'Éducation', icon: '📚' },
        { value: 'TRAVAIL', label: 'Travail', icon: '💼' },
        { value: 'LIFESTYLE', label: 'Style de vie', icon: '🌱' },
        { value: 'SOCIAL', label: 'Social', icon: '👥' },
        { value: 'CREATIVITE', label: 'Créativité', icon: '🎨' },
        { value: 'FINANCE', label: 'Finance', icon: '💰' },
        { value: 'AUTRE', label: 'Autre', icon: '📌' },
    ];

    const frequencies: { value: HabitFrequency; label: string }[] = [
        { value: 'DAILY', label: 'Quotidien' },
        { value: 'WEEKLY', label: 'Hebdomadaire' },
        { value: 'MONTHLY', label: 'Mensuel' },
    ];

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
            <Card variant="elevated" className="w-full max-w-2xl max-h-[90vh] overflow-y-auto">
                <CardHeader>
                    <div className="flex items-center justify-between">
                        <h2 className="text-2xl font-bold text-neutral-900">
                            {isEditing ? 'Modifier l\'habitude' : 'Nouvelle habitude'}
                        </h2>
                        <Button variant="ghost" size="sm" onClick={onCancel}>
                            <X className="h-5 w-5" />
                        </Button>
                    </div>
                </CardHeader>

                <form onSubmit={handleSubmit(handleFormSubmit)}>
                    <CardBody className="space-y-6">
                        {/* Titre */}
                        <Input
                            label="Titre de l'habitude"
                            placeholder="Ex: Courir 30 minutes"
                            fullWidth
                            error={errors.title?.message}
                            {...register('title', {
                                required: 'Le titre est obligatoire',
                                minLength: { value: 3, message: 'Le titre doit contenir au moins 3 caractères' },
                                maxLength: { value: 100, message: 'Le titre ne peut pas dépasser 100 caractères' },
                            })}
                        />

                        {/* Description */}
                        <div>
                            <label className="block text-sm font-medium text-neutral-700 mb-2">
                                Description (optionnel)
                            </label>
                            <textarea
                                className="w-full border border-neutral-300 rounded-xl px-4 py-3 transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-primary-500 resize-none"
                                rows={3}
                                placeholder="Décrivez votre habitude..."
                                {...register('description', {
                                    maxLength: { value: 500, message: 'La description ne peut pas dépasser 500 caractères' },
                                })}
                            />
                            {errors.description && (
                                <p className="mt-2 text-sm text-danger-600">{errors.description.message}</p>
                            )}
                        </div>

                        {/* Catégorie */}
                        <div>
                            <label className="block text-sm font-medium text-neutral-700 mb-3">
                                Catégorie
                            </label>
                            <div className="grid grid-cols-3 gap-3">
                                {categories.map((category) => (
                                    <label
                                        key={category.value}
                                        className={`
                      flex flex-col items-center p-3 border-2 rounded-xl cursor-pointer transition-all duration-200
                      ${selectedCategory === category.value
                                            ? 'border-primary-500 bg-primary-50'
                                            : 'border-neutral-200 hover:border-primary-300 hover:bg-neutral-50'
                                        }
                    `}
                                    >
                                        <span className="text-2xl mb-1">{category.icon}</span>
                                        <span className="text-sm font-medium text-center">{category.label}</span>
                                        <input
                                            type="radio"
                                            value={category.value}
                                            className="sr-only"
                                            {...register('category', { required: 'Veuillez sélectionner une catégorie' })}
                                        />
                                    </label>
                                ))}
                            </div>
                            {errors.category && (
                                <p className="mt-2 text-sm text-danger-600">{errors.category.message}</p>
                            )}
                        </div>

                        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                            {/* Unité */}
                            <Input
                                label="Unité de mesure"
                                placeholder="Ex: minutes, kilomètres, pages"
                                fullWidth
                                error={errors.unit?.message}
                                {...register('unit', {
                                    required: 'L\'unité est obligatoire',
                                    maxLength: { value: 50, message: 'L\'unité ne peut pas dépasser 50 caractères' },
                                })}
                            />

                            {/* Valeur cible */}
                            <Input
                                label="Objectif (optionnel)"
                                type="number"
                                step="0.1"
                                min="0"
                                placeholder="Ex: 30"
                                fullWidth
                                error={errors.targetValue?.message}
                                {...register('targetValue', {
                                    min: { value: 0.1, message: 'La valeur doit être positive' },
                                    max: { value: 999999, message: 'La valeur est trop importante' },
                                })}
                            />
                        </div>

                        {/* Fréquence */}
                        <div>
                            <label className="block text-sm font-medium text-neutral-700 mb-3">
                                Fréquence
                            </label>
                            <div className="grid grid-cols-3 gap-3">
                                {frequencies.map((frequency) => (
                                    <label
                                        key={frequency.value}
                                        className={`
                      flex items-center justify-center p-3 border-2 rounded-xl cursor-pointer transition-all duration-200
                      ${watch('frequency') === frequency.value
                                            ? 'border-primary-500 bg-primary-50'
                                            : 'border-neutral-200 hover:border-primary-300 hover:bg-neutral-50'
                                        }
                    `}
                                    >
                                        <span className="font-medium">{frequency.label}</span>
                                        <input
                                            type="radio"
                                            value={frequency.value}
                                            className="sr-only"
                                            {...register('frequency', { required: 'Veuillez sélectionner une fréquence' })}
                                        />
                                    </label>
                                ))}
                            </div>
                            {errors.frequency && (
                                <p className="mt-2 text-sm text-danger-600">{errors.frequency.message}</p>
                            )}
                        </div>
                    </CardBody>

                    <CardFooter>
                        <div className="flex justify-end gap-3 w-full">
                            <Button
                                type="button"
                                variant="outline"
                                onClick={onCancel}
                                disabled={isLoading}
                            >
                                Annuler
                            </Button>
                            <Button
                                type="submit"
                                variant="primary"
                                isLoading={isLoading}
                                disabled={!isValid}
                            >
                                {isEditing ? 'Modifier' : 'Créer'}
                            </Button>
                        </div>
                    </CardFooter>
                </form>
            </Card>
        </div>
    );
};

export default HabitForm;