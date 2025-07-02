#  HabitTracker

Application de suivi d'habitudes développée avec Spring Boot et React.

##  Technologies

- **Backend** : Spring Boot + PostgreSQL + Redis
- **Frontend** : React + TypeScript + Tailwind CSS
- **Admin** : Thymeleaf
- **Doc** : Swagger

##  Prérequis

- Docker & Docker Compose
- Node.js & npm

##  Installation

### 1. Cloner le projet
```bash
git clone https://github.com/jplande/habit-tracker.git
cd habit-tracker
```

### 2. Démarrer le backend
```bash
cd backend
docker-compose up --build
```

### 3. Démarrer le frontend
```bash
cd frontend
npm install
npm start
```

##  Accès

- **Application** : http://localhost:3000/login
- **Admin** : http://localhost:8080/admin
- **API** : http://localhost:8080/swagger-ui.html

##  Comptes de test

**Application utilisateur** :
- `alice_novice` / `password123`
- `admin` / `admin123`

**Interface admin** :
- `admin` / `admin123`

##  Problèmes courants

### Refresh token
```bash
docker-compose down
npm start
docker-compose up 
```

### Erreur de connexion API
Vérifier que le backend est démarré sur le port 8080 :
```bash
curl http://localhost:8080/actuator/health
```
