# LeafSocial

Application Android intégrée de l'écosystème LeafVibe.

## Version 0.1.0 — première base testable
- Une APK unique et navigation Compose.
- Sept espaces : LeafCommunity, LeafLearn, LeafIdeas, LeafNews, LeafMarket, LeafLibrary, LeafWellness.
- Identité Oasis #206B4D / noyer #6B4935 / crème.
- Écran d'entrée et profil de démonstration.
- Création de publications locales persistées sur l'appareil.
- Navigation Accueil / Explorer / Créer / Profil.

## Limites importantes de cette préversion
Cette version est un prototype fonctionnel local, pas encore un réseau social en production. L'écran d'entrée est une simulation locale : aucun compte serveur, aucune authentification sécurisée, aucune messagerie distante ni synchronisation multi-appareils. Les modules sont des espaces de navigation en cours de construction. Ne pas utiliser de vrai mot de passe : la saisie n'est pas envoyée ni sécurisée par un backend.

## Compiler
Ouvrir le projet dans Android Studio (JDK 17, Android SDK 35), puis lancer `gradle assembleDebug` ou utiliser le workflow GitHub Actions.

Application ID : `org.leafvibe.leafsocial`
Minimum Android : 8.0 (API 26)
