# DoseCalc — guide du dépôt

Application Android native de calcul de **volume posologique**, **100 % hors-ligne** :
aucun réseau, aucun compte, aucune dépendance Google Play Services / Firebase / analytics.
FOSS uniquement → compatible /e/OS et publiable sur F-Droid.

À partir d'un poids corporel, d'une posologie par kg et d'une concentration, l'app calcule
le volume à administrer.

## Stack
- **Kotlin 2.2** (compilateur K2) — `kotlin = 2.2.21`
- **Jetpack Compose + Material 3** pour l'UI
- **Gradle Kotlin DSL** (`build.gradle.kts`) + version catalog (`gradle/libs.versions.toml`)
- **AGP 8.13.2**, **Gradle 8.14.5**
- `minSdk 24`, `compileSdk = targetSdk = 36` (Android 16, dernière stable compatible avec la
  ligne Kotlin 2.2 / AGP 8.13 ; passer à l'API 37 exigerait AGP 9.1+).
- **Module unique** (`:app`).

> Note de toolchain : AGP/Kotlin exigent **JDK 17**. Le `JAVA_HOME` doit pointer vers un JDK 17
> (un JDK 11 système ne suffit pas). Sur cette machine : `…\Programs\jdk17\jdk-17.0.19+10`.
> Le chemin du SDK Android est dans `local.properties` (non versionné).

## Architecture & invariant
> **Invariant : toute la logique de calcul reste pure et testable hors UI.**

- Cœur métier : [`app/src/main/kotlin/com/example/dosecalc/logic/DoseMath.kt`](app/src/main/kotlin/com/example/dosecalc/logic/DoseMath.kt)
  — Kotlin **pur**, zéro import Android/Compose, donc testable en JVM **sans émulateur**.
  - `computeVolumeMl(...)` : cœur numérique (entrées `Double` + unités typées).
  - `computeVolumeMlFromInput(...)` : parse les chaînes (vide / non numérique / virgule FR) puis délègue.
  - Résultat = `DoseResult.Success(volumeMl)` ou `DoseResult.Invalid(reason)` — pas d'exception, pas de NaN.
- UI Compose (`com.example.dosecalc.ui`) : `MainActivity`, `DoseCalcScreen`, `DoseCalcViewModel`.
  Le ViewModel ne contient **aucune** logique de calcul ; il appelle seulement `computeVolumeMlFromInput`.
- Tests : [`app/src/test/kotlin/com/example/dosecalc/logic/DoseMathTest.kt`](app/src/test/kotlin/com/example/dosecalc/logic/DoseMathTest.kt) (JUnit4).

### Formule
`volume_ml = (poids_kg × posologie_µg_par_kg) / concentration_µg_par_ml`,
toutes les masses ramenées au microgramme (µg) en interne.

Cas limites : concentration ≤ 0 → invalide (pas de division par zéro) ; valeurs négatives → rejetées ;
champ vide / non numérique → pas de calcul, pas de crash.

## Commandes
Toujours avec `JAVA_HOME` pointant vers un JDK 17.

```bash
# Tests de la logique (rapides, sans émulateur)
./gradlew :app:testDebugUnitTest

# Tous les tests
./gradlew test

# Build de l'APK debug
./gradlew assembleDebug      # -> app/build/outputs/apk/debug/app-debug.apk

# Installer/lancer sur un appareil/émulateur connecté
./gradlew installDebug

# Nettoyer
./gradlew clean
```

Sous Windows PowerShell : `.\gradlew.bat …` (penser à `$env:JAVA_HOME` = JDK 17).

## Convention de commit
[Conventional Commits](https://www.conventionalcommits.org/) :
`feat:`, `fix:`, `test:`, `chore:`, `docs:`, `refactor:`…
La logique métier est développée en **TDD strict** : tests rouges d'abord, implémentation jusqu'au
vert, puis commit.
