package fr.dauxais.dosecalc.logic

/**
 * Cœur métier du calcul posologique — **Kotlin pur**, sans aucune dépendance Android/Compose,
 * donc testable en JVM sans émulateur.
 *
 * Formule : `volume_ml = (poids_kg × posologie_µg_par_kg) / concentration_µg_par_ml`,
 * toutes les masses ramenées au microgramme (µg) en interne.
 */

/** Unité de masse, exprimée via son facteur de conversion vers le microgramme (µg). */
enum class MassUnit(val microgramsPerUnit: Double) {
    MICROGRAM(1.0),
    MILLIGRAM(1_000.0),
    GRAM(1_000_000.0),
}

/** Unité de poids corporel, exprimée via son facteur de conversion vers le kilogramme. */
enum class WeightUnit(val kilogramsPerUnit: Double) {
    KILOGRAM(1.0),
    GRAM(0.001),
}

/** Raisons d'invalidité explicites — pas d'exception levée, pas de NaN/Infinity propagé. */
enum class InvalidReason { EMPTY_OR_NON_NUMERIC, NEGATIVE_VALUE, NON_POSITIVE_CONCENTRATION }

/** Résultat du calcul : soit un volume valide, soit une raison d'invalidité. */
sealed interface DoseResult {
    data class Success(val volumeMl: Double) : DoseResult
    data class Invalid(val reason: InvalidReason) : DoseResult
}

/**
 * Cœur numérique pur.
 *
 * La concentration peut être exprimée par rapport à un volume de référence quelconque
 * (ex. « 250 µg / 5 ml ») via [concentrationVolumeMl] ; sa valeur par défaut `1.0` correspond
 * à une concentration « par ml ».
 *
 * @return [DoseResult.Success] avec le volume en ml, ou [DoseResult.Invalid] si :
 *  - la concentration ou le volume de référence est ≤ 0
 *    ([InvalidReason.NON_POSITIVE_CONCENTRATION], évite la division par zéro) ;
 *  - le poids ou la posologie est négatif ([InvalidReason.NEGATIVE_VALUE]).
 */
fun computeVolumeMl(
    bodyWeight: Double, weightUnit: WeightUnit,
    dosePerKg: Double, doseUnit: MassUnit,
    concentration: Double, concentrationUnit: MassUnit,
    concentrationVolumeMl: Double = 1.0,
): DoseResult {
    if (bodyWeight < 0.0 || dosePerKg < 0.0) {
        return DoseResult.Invalid(InvalidReason.NEGATIVE_VALUE)
    }
    if (concentration <= 0.0 || concentrationVolumeMl <= 0.0) {
        return DoseResult.Invalid(InvalidReason.NON_POSITIVE_CONCENTRATION)
    }

    val weightKg = bodyWeight * weightUnit.kilogramsPerUnit
    val doseMicrogramsPerKg = dosePerKg * doseUnit.microgramsPerUnit
    // Concentration ramenée au µg/ml à partir du volume de référence.
    val concentrationMicrogramsPerMl =
        concentration * concentrationUnit.microgramsPerUnit / concentrationVolumeMl

    val totalDoseMicrograms = weightKg * doseMicrogramsPerKg
    val volumeMl = totalDoseMicrograms / concentrationMicrogramsPerMl
    return DoseResult.Success(volumeMl)
}

/**
 * Variante orientée UI : parse des chaînes saisies par l'utilisateur (gère le champ vide,
 * le non-numérique et la virgule décimale française), puis délègue à [computeVolumeMl].
 *
 * Permet à la couche Compose/ViewModel de rester triviale : elle ne contient aucune logique de calcul.
 */
fun computeVolumeMlFromInput(
    bodyWeightText: String, weightUnit: WeightUnit,
    dosePerKgText: String, doseUnit: MassUnit,
    concentrationText: String, concentrationUnit: MassUnit,
    concentrationVolumeText: String = "1",
): DoseResult {
    val bodyWeight = parseNumber(bodyWeightText)
        ?: return DoseResult.Invalid(InvalidReason.EMPTY_OR_NON_NUMERIC)
    val dosePerKg = parseNumber(dosePerKgText)
        ?: return DoseResult.Invalid(InvalidReason.EMPTY_OR_NON_NUMERIC)
    val concentration = parseNumber(concentrationText)
        ?: return DoseResult.Invalid(InvalidReason.EMPTY_OR_NON_NUMERIC)
    val concentrationVolume = parseNumber(concentrationVolumeText)
        ?: return DoseResult.Invalid(InvalidReason.EMPTY_OR_NON_NUMERIC)

    return computeVolumeMl(
        bodyWeight, weightUnit,
        dosePerKg, doseUnit,
        concentration, concentrationUnit,
        concentrationVolume,
    )
}

/**
 * Parse un nombre décimal en tolérant la virgule (locale FR) comme le point.
 * Retourne `null` si la chaîne est vide, non numérique, ou non finie.
 */
private fun parseNumber(text: String): Double? {
    val normalized = text.trim().replace(',', '.')
    if (normalized.isEmpty()) return null
    val value = normalized.toDoubleOrNull() ?: return null
    return if (value.isFinite()) value else null
}
