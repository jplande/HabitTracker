import React from 'react';
import { createRoot } from 'react-dom/client';
import App from './App';
import './index.css';

// Configuration React DevTools
if (process.env.NODE_ENV === 'development') {
    console.log('🚀 HabitTracker Frontend - Mode Développement');
    console.log('🎯 Version:', process.env.REACT_APP_VERSION || '1.0.0');
    console.log('🔗 API URL:', process.env.REACT_APP_API_URL || 'http://localhost:8080/api');
}

const container = document.getElementById('root');

if (!container) {
    throw new Error('Root container missing in index.html');
}

const root = createRoot(container);

root.render(
    <React.StrictMode>
        <App />
    </React.StrictMode>
);

// Web Vitals et performances (optionnel)
if (process.env.NODE_ENV === 'production') {
    // Service Worker pour PWA (à implémenter plus tard)
    if ('serviceWorker' in navigator) {
        window.addEventListener('load', () => {
            navigator.serviceWorker.register('/sw.js')
                .then((registration) => {
                    console.log('SW registered: ', registration);
                })
                .catch((registrationError) => {
                    console.log('SW registration failed: ', registrationError);
                });
        });
    }
}