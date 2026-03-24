# 🚗 DadaDrive

> Application Android de transport à la demande — inspirée de Bolt/Uber.  
> Connecte les passagers à des chauffeurs en temps réel, avec suivi GPS, paiement intégré et historique de courses.

---

## 📋 Table des matières

- [Aperçu du projet](#aperçu-du-projet)
- [Stack technique](#stack-technique)
- [Architecture MVVM](#architecture-mvvm)
- [Structure des dossiers](#structure-des-dossiers)
- [Mise en route](#mise-en-route)
- [Pusher le projet sur Git](#pusher-le-projet-sur-git)

---

## Aperçu du projet

DadaDrive est une application mobile Android qui permet :

- 📍 La **géolocalisation en temps réel** du passager et du chauffeur
- 🚕 La **mise en relation** instantanée passager ↔ chauffeur
- 💳 Le **paiement intégré** (mobile money, carte bancaire)
- ⭐ Le **système de notation** des courses
- 📜 L'**historique des trajets** avec détail des prix
- 🔔 Les **notifications push** en temps réel

---

## Stack technique

| Composant | Technologie |
|---|---|
| Langage | Kotlin |
| Architecture | MVVM + Clean Architecture |
| UI | Jetpack Compose / XML Fragments |
| Navigation | Jetpack Navigation Component |
| Injection de dépendances | Hilt (Dagger) |
| Réseau | Retrofit + OkHttp |
| Base de données locale | Room |
| Temps réel | Firebase Firestore / WebSocket |
| Cartes & GPS | Google Maps SDK / Mapbox |
| Authentification | Firebase Auth |
| Reactive programming | Kotlin Coroutines + Flow |
| Tests | JUnit, Mockito, Espresso |

---

## Architecture MVVM

DadaDrive suit le pattern **MVVM (Model – View – ViewModel)** combiné aux principes de la **Clean Architecture**, organisée en 3 couches indépendantes :

```
┌─────────────────────────────────────────┐
│              UI LAYER (View)            │
│   Fragments · Activities · Composables  │
│   Observe → LiveData / StateFlow        │
└──────────────┬──────────────────────────┘
               │ observe / call
┌──────────────▼──────────────────────────┐
│           VIEWMODEL LAYER               │
│   Gère l'état UI · Appelle les UseCases │
│   Ne connaît pas la View directement    │
└──────────────┬──────────────────────────┘
               │ execute
┌──────────────▼──────────────────────────┐
│           DOMAIN LAYER                  │
│   UseCases · Interfaces Repository      │
│   Logique métier pure · Sans Android    │
└──────────────┬──────────────────────────┘
               │ implement
┌──────────────▼──────────────────────────┐
│            DATA LAYER                   │
│   Repositories · Room · Retrofit        │
│   Firebase · Sources de données         │
└─────────────────────────────────────────┘
```

### Rôle de chaque couche

**UI Layer** — Ce que l'utilisateur voit et avec quoi il interagit.  
Contient les `Fragment`, `Activity`, `ViewModel` liés à l'UI. Elle observe les données exposées par le ViewModel via `LiveData` ou `StateFlow` et ne contient **aucune logique métier**.

**ViewModel Layer** — Le pont entre l'UI et la logique.  
Survit aux rotations d'écran. Appelle les `UseCase` du domaine, expose les états UI, et gère les événements utilisateur.

**Domain Layer** — Le cœur de l'application.  
Contient les `UseCase` (ex: `GetNearbyDriversUseCase`, `BookRideUseCase`) et les interfaces `Repository`. Cette couche est **100% Kotlin pur**, sans dépendance Android — facilitant les tests unitaires.

**Data Layer** — La source de vérité.  
Implémente les interfaces du domaine. Orchestre les données entre la base locale (Room), l'API distante (Retrofit) et Firebase. Contient aussi les `DTO` (objets de transfert) et les `DAO`.

---

## Structure des dossiers

```
app/src/main/java/com/dadadrive/
│
├── data/
│   ├── local/
│   │   ├── dao/              # Interfaces Room DAO (requêtes BDD locale)
│   │   └── database/         # Configuration AppDatabase Room
│   ├── remote/
│   │   ├── api/              # Interfaces Retrofit (endpoints REST)
│   │   └── dto/              # Modèles de réponse API (Data Transfer Objects)
│   └── repository/           # Implémentations concrètes des repositories
│
├── domain/
│   ├── model/                # Entités métier (User, Ride, Driver, Location...)
│   ├── repository/           # Interfaces abstraites des repositories
│   └── usecase/              # Cas d'utilisation (BookRide, TrackDriver, Pay...)
│
├── ui/
│   ├── auth/
│   │   ├── login/            # Écran connexion (Fragment + ViewModel)
│   │   └── register/         # Écran inscription (Fragment + ViewModel)
│   ├── home/                 # Carte principale + recherche de chauffeur
│   ├── files/                # Historique de courses et documents
│   ├── profile/              # Profil utilisateur et paramètres
│   └── common/               # Composants UI réutilisables (boutons, dialogs...)
│
├── di/                       # Modules Hilt (injection de dépendances)
└── utils/                    # Extensions Kotlin, constantes, helpers
```

---

## Mise en route

### Prérequis

- Android Studio Hedgehog (ou plus récent)
- JDK 17+
- Un compte Firebase (pour Auth + Firestore)
- Une clé API Google Maps

### Installation

```bash
# 1. Cloner le dépôt
git clone https://github.com/ton-username/dadadrive.git
cd dadadrive

# 2. Ouvrir dans Android Studio
# File > Open > sélectionner le dossier DadaDrive

# 3. Ajouter le fichier google-services.json
# Télécharge-le depuis Firebase Console et place-le dans app/

# 4. Ajouter ta clé Google Maps dans local.properties
MAPS_API_KEY=ta_cle_ici

# 5. Synchroniser Gradle et lancer l'app
```

---

## Pusher le projet sur Git

Suis ces étapes dans ton terminal (PowerShell ou Git Bash) depuis le dossier `DadaDrive` :

### Étape 1 — Initialiser le dépôt Git

```powershell
cd "C:\Users\User\Desktop\DadaDrive"
git init
```

### Étape 2 — Créer le fichier .gitignore

```powershell
# Télécharge le .gitignore recommandé pour Android
Invoke-WebRequest -Uri "https://www.toptal.com/developers/gitignore/api/android,kotlin,androidstudio" -OutFile ".gitignore"
```

> Ou crée-le manuellement avec le contenu minimal :
> ```
> *.iml
> .gradle/
> /local.properties
> /.idea/
> .DS_Store
> /build/
> /captures/
> google-services.json
> ```

### Étape 3 — Ajouter tous les fichiers

```powershell
git add .
```

### Étape 4 — Faire le premier commit

```powershell
git commit -m "feat: initial project structure with MVVM architecture"
```

### Étape 5 — Créer le dépôt sur GitHub

1. Va sur [github.com/new](https://github.com/new)
2. Nomme le dépôt `dadadrive`
3. Ne coche **pas** "Initialize with README" (on en a déjà un)
4. Clique **Create repository**

### Étape 6 — Lier et pusher

```powershell
git remote add origin https://github.com/ton-username/dadadrive.git
git branch -M main
git push -u origin main
```

### Vérification

```powershell
# Voir le statut
git status

# Voir les commits
git log --oneline

# Voir la branche distante liée
git remote -v
```

---

## Workflow Git recommandé

```powershell
# Créer une branche pour chaque fonctionnalité
git checkout -b feature/auth-login

# Après tes modifications
git add .
git commit -m "feat(auth): add login screen with ViewModel"
git push origin feature/auth-login

# Puis créer une Pull Request sur GitHub
```

---

## Contributeurs

| Nom | Rôle |
|---|---|
| [@ton-username](https://github.com/ton-username) | Lead Developer |

---

## Licence

Ce projet est sous licence MIT. Voir le fichier [LICENSE](LICENSE) pour plus de détails.
