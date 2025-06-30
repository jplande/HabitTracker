import React, { useState } from 'react';
import Modal from '../atoms/Modal';
import Input from '../atoms/Input';
import Button from '../atoms/Button';
import { User } from '../../services/authService';

interface EditProfile {
    user: User;
    onClose: () => void;
}

const EditProfileModal: React.FC<EditProfile> = ({ user, onClose }) => {
    const [username, setUsername] = useState(user.username);
    // const [email, setEmail] = useState(user.email);
    // const [password, setPassword] = useState('');

    const handleSave = () => {
        console.log({ username});
        onClose();
    };

    return (
        <Modal isOpen={true} onClose={onClose} title="Modifier mon profil">
            <div className="">
                <Input label="Nom d'utilisateur" value={username} onChange={(e) => setUsername(e.target.value)} />
                
                <div className="flex justify-end gap-3 pt-2">
                    <Button variant="secondary" onClick={onClose}>Annuler</Button>
                    <Button onClick={handleSave}>Modifier</Button>
                </div>
            </div>
        </Modal>
    );
};

export default EditProfileModal;
