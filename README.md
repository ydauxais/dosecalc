# DoseCalc

Application Android de **calcul de volume posologique**, 100 % **hors-ligne**, sans réseau ni compte.
À partir d'un poids, d'une posologie par kg et d'une concentration, elle calcule le volume à administrer.

Aucune dépendance Google Play Services / Firebase / analytics — uniquement des bibliothèques FOSS,
compatible /e/OS et publiable sur F-Droid.

> ⚠️ **Aide au calcul uniquement.** Vérifiez toujours l'unité, la concentration et la dose obtenue.
> Cet outil ne remplace pas le jugement d'un professionnel de santé.

## Fonctionnalités
- Poids en **kg** ou **g**
- Posologie en **mg** ou **µg** par kg
- Concentration en **mg** ou **µg**, exprimable **par un volume de référence** quelconque (ex. 250 µg / 5 ml)
- Résultat en ml, en temps réel, avec gestion d'erreur (champ vide, valeur négative, concentration nulle…)
- Interface en français, Material 3

## Installation

### Via Obtainium (recommandé, mises à jour automatiques)
1. Installez [Obtainium](https://github.com/ImranR98/Obtainium) (disponible sur F-Droid).
2. Ajoutez une application → collez l'URL de ce dépôt : `https://github.com/ydauxais/dosecalc`
3. Obtainium détecte les [Releases](https://github.com/ydauxais/dosecalc/releases) et installe/met à jour l'APK.

> Aucune option de développeur n'est nécessaire — seulement l'autorisation Android
> « installer des applications inconnues » pour Obtainium (réglage utilisateur normal).

### Manuellement
Téléchargez le dernier `dosecalc-vX.Y.apk` depuis les [Releases](https://github.com/ydauxais/dosecalc/releases),
ouvrez-le sur le téléphone et autorisez l'installation depuis cette source.

## Build
Nécessite un **JDK 17**.

```bash
./gradlew test            # tests unitaires de la logique (sans émulateur)
./gradlew assembleDebug   # APK de debug
./gradlew assembleRelease # APK release minifié (R8) — signé si keystore configuré
```

Détails d'architecture, conventions et invariants : voir [CLAUDE.md](CLAUDE.md).

## Licence
À définir (par défaut : tous droits réservés tant qu'aucune licence n'est ajoutée).
Pour une publication F-Droid, ajouter une licence libre (ex. GPLv3 ou MIT).
