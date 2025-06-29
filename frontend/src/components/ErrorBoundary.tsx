import React, { Component, ReactNode } from 'react';
import { AlertTriangle, RefreshCw } from 'lucide-react';
import Card, { CardBody } from './atoms/Card';
import Button from './atoms/Button';

interface Props {
    children: ReactNode;
    fallback?: ReactNode;
}

interface State {
    hasError: boolean;
    error?: Error;
}

class ErrorBoundary extends Component<Props, State> {
    constructor(props: Props) {
        super(props);
        this.state = { hasError: false };
    }

    static getDerivedStateFromError(error: Error): State {
        return {
            hasError: true,
            error,
        };
    }

    componentDidCatch(error: Error, errorInfo: React.ErrorInfo) {
        console.error('ErrorBoundary caught an error:', error, errorInfo);
    }

    handleReset = () => {
        this.setState({ hasError: false, error: undefined });
    };

    render() {
        if (this.state.hasError) {
            if (this.props.fallback) {
                return this.props.fallback;
            }

            return (
                <div className="min-h-[400px] flex items-center justify-center p-6">
                    <Card variant="outlined" className="max-w-md w-full">
                        <CardBody className="text-center py-8">
                            <AlertTriangle className="h-16 w-16 text-warning-500 mx-auto mb-4" />
                            <h2 className="text-xl font-semibold text-neutral-900 mb-2">
                                Oops ! Une erreur s'est produite
                            </h2>
                            <p className="text-neutral-600 mb-6">
                                Ne vous inquiétez pas, ce n'est probablement qu'un problème temporaire.
                            </p>

                            {process.env.NODE_ENV === 'development' && this.state.error && (
                                <details className="text-left mb-6 bg-neutral-100 p-4 rounded-lg">
                                    <summary className="cursor-pointer font-medium text-sm text-neutral-700 mb-2">
                                        Détails de l'erreur (dev)
                                    </summary>
                                    <pre className="text-xs text-neutral-600 overflow-auto">
                    {this.state.error.message}
                                        {this.state.error.stack}
                  </pre>
                                </details>
                            )}

                            <div className="flex flex-col gap-3">
                                <Button
                                    variant="primary"
                                    onClick={this.handleReset}
                                    icon={<RefreshCw className="h-4 w-4" />}
                                >
                                    Réessayer
                                </Button>

                                <Button
                                    variant="outline"
                                    onClick={() => window.location.reload()}
                                >
                                    Recharger la page
                                </Button>
                            </div>
                        </CardBody>
                    </Card>
                </div>
            );
        }

        return this.props.children;
    }
}

export default ErrorBoundary;