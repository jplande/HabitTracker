// Settings.tsx
import React, { useState } from "react";
import { useAuth } from "../contexts/AuthContext";
import MainLayout from "../components/templates/MainLayout";
import Card, { CardBody, CardHeader } from "../components/atoms/Card";
import Button from "../components/atoms/Button";
import EditProfileModal from "../components/molecules/EditModal";
import Modal from "../components/atoms/Modal";

const Setting: React.FC = () => {
  const { user, logout } = useAuth();
  const [isModalOpen, setIsModalOpen] = useState(false);

  const [confirmLogoutOpen, setConfirmLogoutOpen] = useState(false);

  if (!user) return null;

  return (
    <MainLayout title="Paramètres">
      <div className="flex justify-center">
        <Card variant="elevated" padding="lg" className="max-w-xl w-full">
          <CardHeader>
            <h2 className="text-xl font-semibold text-neutral-900">
              Paramètres du profil
            </h2>
          </CardHeader>
          <CardBody className="space-y-4">
            <div className="flex">
              <span className="font-semibold mr-2">Nom d'utilisateur :</span>
              <span>{user.username}</span>
            </div>
            <div className="flex">
              <span className="font-semibold mr-2">Prénom :</span>
              <span>{user.firstName}</span>
            </div>
            <div className="flex">
              <span className="font-semibold mr-2">Nom :</span>
              <span>{user.lastName}</span>
            </div>
            <div className="flex">
              <span className="font-semibold mr-2">Email :</span>
              <span>{user.email}</span>
            </div>

            <Button variant="secondary" onClick={() => setIsModalOpen(true)}>
              Modifier mon profil
            </Button>

            <div className="pt-4 border-t">
              <Button
                variant="primary"
                onClick={() => setConfirmLogoutOpen(true)}
              >
                Se déconnecter
              </Button>
            </div>
          </CardBody>
        </Card>
      </div>
      {isModalOpen ? (
        <EditProfileModal user={user} onClose={() => setIsModalOpen(false)} />
      ) : null}

      {confirmLogoutOpen ? (
        <Modal
          isOpen={true}
          onClose={() => setConfirmLogoutOpen(false)}
          title="Confirmer la déconnexion"
        >
          <p className="mb-4">Êtes-vous sûr de vouloir vous déconnecter ?</p>
          <div className="flex justify-end gap-3">
            <Button
              variant="secondary"
              onClick={() => setConfirmLogoutOpen(false)}
            >
              Annuler
            </Button>
            <Button variant="primary" onClick={logout}>
              Se déconnecter
            </Button>
          </div>
        </Modal>
      ) : null}
    </MainLayout>
  );
};

export default Setting;
