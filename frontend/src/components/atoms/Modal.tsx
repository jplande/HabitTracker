import React from 'react';
import { X } from 'lucide-react';

interface ModalProps {
    isOpen: boolean;
    onClose: () => void;
    title?: string;
    children: React.ReactNode;
}

const Modal: React.FC<ModalProps> = ({ isOpen, onClose, title, children }) => {
    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-40">
            <div className="bg-white rounded-xl shadow-xl max-w-md w-full p-6 relative">
                <button
                    className="absolute top-4 right-4 text-neutral-500 hover:text-neutral-700"
                    onClick={onClose}
                >
                    <X size={20} />
                </button>
                {title && (
                    <h2 className="text-lg font-semibold text-neutral-800 mb-4">{title}</h2>
                )}
                {children}
            </div>
        </div>
    );
};

export default Modal;
