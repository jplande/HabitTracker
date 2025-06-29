import React from 'react';
import { Calendar, Target, TrendingUp, Edit, Trash2, Power } from 'lucide-react';
import Card, { CardHeader, CardBody, CardFooter } from '../atoms/Card';
import Badge from '../atoms/Badge';
import Button from '../atoms/Button';
import { Habit, habitService } from '../../services/habitService';

interface HabitCardProps {
    habit: Habit;
    onEdit?: (habit: Habit) => void;
    onDelete?: (habitId: number) => void;
    onToggleStatus?: (habitId: number) => void;
    onProgress?: (habit: Habit) => void;
}

const HabitCard: React.FC<HabitCardProps> = ({
                                                 habit,
                                                 onEdit,
                                                 onDelete,
                                                 onToggleStatus,
                                                 onProgress,
                                             }) => {
    const categoryIcon = habitService.getCategoryIcon(habit.category);
    const categoryLabel = habitService.getCategoryLabel(habit.category);
    const frequencyLabel = habitService.getFrequencyLabel(habit.frequency);

    const getStreakBadgeVariant = (streak?: number) => {
        if (!streak) return 'neutral';
        if (streak >= 30) return 'success';
        if (streak >= 7) return 'primary';
        return 'secondary';
    };

    const getCompletionBadgeVariant = (completion?: number) => {
        if (!completion) return 'neutral';
        if (completion >= 90) return 'success';
        if (completion >= 70) return 'warning';
        return 'danger';
    };

    return (
        <Card variant="elevated" hover className="h-full">
            <CardHeader>
                <div className="flex items-start justify-between">
                    <div className="flex items-center gap-3">
                        <div className="text-2xl">{categoryIcon}</div>
                        <div>
                            <h3 className="font-semibold text-lg text-neutral-900">{habit.title}</h3>
                            <Badge variant="neutral" size="sm">{categoryLabel}</Badge>
                        </div>
                    </div>

                    <div className="flex items-center gap-1">
                        <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => onToggleStatus?.(habit.id)}
                            className={habit.isActive ? 'text-success-600' : 'text-neutral-400'}
                        >
                            <Power className="h-4 w-4" />
                        </Button>

                        {onEdit && (
                            <Button
                                variant="ghost"
                                size="sm"
                                onClick={() => onEdit(habit)}
                            >
                                <Edit className="h-4 w-4" />
                            </Button>
                        )}

                        {onDelete && (
                            <Button
                                variant="ghost"
                                size="sm"
                                onClick={() => onDelete(habit.id)}
                                className="text-danger-600 hover:text-danger-700"
                            >
                                <Trash2 className="h-4 w-4" />
                            </Button>
                        )}
                    </div>
                </div>
            </CardHeader>

            <CardBody>
                {habit.description && (
                    <p className="text-neutral-600 text-sm mb-4 line-clamp-2">
                        {habit.description}
                    </p>
                )}

                <div className="space-y-3">
                    {/* Objectif */}
                    <div className="flex items-center gap-2 text-sm">
                        <Target className="h-4 w-4 text-primary-500" />
                        <span className="text-neutral-600">
              {habit.targetValue ? `${habit.targetValue} ${habit.unit}` : habit.unit}
                            <span className="text-neutral-400 ml-1">({frequencyLabel})</span>
            </span>
                    </div>

                    {/* Progression actuelle */}
                    {habit.currentStreak !== undefined && (
                        <div className="flex items-center justify-between">
                            <div className="flex items-center gap-2 text-sm">
                                <Calendar className="h-4 w-4 text-accent-500" />
                                <span className="text-neutral-600">Série actuelle</span>
                            </div>
                            <Badge variant={getStreakBadgeVariant(habit.currentStreak)} size="sm">
                                {habit.currentStreak} jour{habit.currentStreak !== 1 ? 's' : ''}
                            </Badge>
                        </div>
                    )}

                    {/* Taux de completion */}
                    {habit.averageCompletion !== undefined && (
                        <div className="flex items-center justify-between">
                            <div className="flex items-center gap-2 text-sm">
                                <TrendingUp className="h-4 w-4 text-success-500" />
                                <span className="text-neutral-600">Completion</span>
                            </div>
                            <Badge variant={getCompletionBadgeVariant(habit.averageCompletion)} size="sm">
                                {Math.round(habit.averageCompletion)}%
                            </Badge>
                        </div>
                    )}

                    {/* Barre de progression visuelle */}
                    {habit.averageCompletion !== undefined && (
                        <div className="mt-3">
                            <div className="w-full bg-neutral-200 rounded-full h-2">
                                <div
                                    className="bg-gradient-to-r from-primary-500 to-accent-500 h-2 rounded-full transition-all duration-300"
                                    style={{ width: `${Math.min(habit.averageCompletion, 100)}%` }}
                                />
                            </div>
                        </div>
                    )}
                </div>
            </CardBody>

            {onProgress && (
                <CardFooter>
                    <Button
                        variant="primary"
                        size="sm"
                        onClick={() => onProgress(habit)}
                        className="w-full"
                        disabled={!habit.isActive}
                    >
                        Ajouter un progrès
                    </Button>
                </CardFooter>
            )}

            {/* Indicateur de statut */}
            {!habit.isActive && (
                <div className="absolute top-2 right-2">
                    <Badge variant="neutral" size="sm">Inactif</Badge>
                </div>
            )}
        </Card>
    );
};

export default HabitCard;