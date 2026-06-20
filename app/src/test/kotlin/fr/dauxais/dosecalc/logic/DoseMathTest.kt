package fr.dauxais.dosecalc.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests du cœur métier pur (aucune dépendance Android/Compose).
 * Écrits AVANT l'implémentation (TDD strict).
 */
class DoseMathTest {

    private fun volumeOf(result: DoseResult): Double {
        assertTrue("Attendu Success mais reçu $result", result is DoseResult.Success)
        return (result as DoseResult.Success).volumeMl
    }

    // --- Cas de vérification du cahier des charges ---

    @Test
    fun `10kg 5mg par kg 50mg par ml donne 1 ml`() {
        val r = computeVolumeMl(
            bodyWeight = 10.0, weightUnit = WeightUnit.KILOGRAM,
            dosePerKg = 5.0, doseUnit = MassUnit.MILLIGRAM,
            concentration = 50.0, concentrationUnit = MassUnit.MILLIGRAM,
        )
        assertEquals(1.0, volumeOf(r), 1e-9)
    }

    @Test
    fun `10kg 5mg par kg 250ug par ml donne 200 ml`() {
        val r = computeVolumeMl(
            bodyWeight = 10.0, weightUnit = WeightUnit.KILOGRAM,
            dosePerKg = 5.0, doseUnit = MassUnit.MILLIGRAM,
            concentration = 250.0, concentrationUnit = MassUnit.MICROGRAM,
        )
        assertEquals(200.0, volumeOf(r), 1e-9)
    }

    @Test
    fun `4kg 100ug par kg 1mg par ml donne 0_4 ml`() {
        val r = computeVolumeMl(
            bodyWeight = 4.0, weightUnit = WeightUnit.KILOGRAM,
            dosePerKg = 100.0, doseUnit = MassUnit.MICROGRAM,
            concentration = 1.0, concentrationUnit = MassUnit.MILLIGRAM,
        )
        assertEquals(0.4, volumeOf(r), 1e-9)
    }

    // --- Conversions d'unités ---

    @Test
    fun `poids en grammes equivaut aux kilogrammes`() {
        val r = computeVolumeMl(
            bodyWeight = 10_000.0, weightUnit = WeightUnit.GRAM,
            dosePerKg = 5.0, doseUnit = MassUnit.MILLIGRAM,
            concentration = 50.0, concentrationUnit = MassUnit.MILLIGRAM,
        )
        assertEquals(1.0, volumeOf(r), 1e-9)
    }

    @Test
    fun `posologie en ug equivaut a mg`() {
        val enMg = computeVolumeMl(
            10.0, WeightUnit.KILOGRAM, 5.0, MassUnit.MILLIGRAM, 50.0, MassUnit.MILLIGRAM,
        )
        val enUg = computeVolumeMl(
            10.0, WeightUnit.KILOGRAM, 5_000.0, MassUnit.MICROGRAM, 50.0, MassUnit.MILLIGRAM,
        )
        assertEquals(volumeOf(enMg), volumeOf(enUg), 1e-9)
    }

    @Test
    fun `concentration en ug coherente avec mg`() {
        val enMg = computeVolumeMl(
            10.0, WeightUnit.KILOGRAM, 5.0, MassUnit.MILLIGRAM, 50.0, MassUnit.MILLIGRAM,
        )
        val enUg = computeVolumeMl(
            10.0, WeightUnit.KILOGRAM, 5.0, MassUnit.MILLIGRAM, 50_000.0, MassUnit.MICROGRAM,
        )
        assertEquals(volumeOf(enMg), volumeOf(enUg), 1e-9)
    }

    // --- Cas limites ---

    @Test
    fun `concentration nulle est invalide`() {
        val r = computeVolumeMl(
            10.0, WeightUnit.KILOGRAM, 5.0, MassUnit.MILLIGRAM, 0.0, MassUnit.MILLIGRAM,
        )
        assertEquals(DoseResult.Invalid(InvalidReason.NON_POSITIVE_CONCENTRATION), r)
    }

    @Test
    fun `concentration negative est invalide`() {
        val r = computeVolumeMl(
            10.0, WeightUnit.KILOGRAM, 5.0, MassUnit.MILLIGRAM, -1.0, MassUnit.MILLIGRAM,
        )
        assertEquals(DoseResult.Invalid(InvalidReason.NON_POSITIVE_CONCENTRATION), r)
    }

    @Test
    fun `poids negatif est invalide`() {
        val r = computeVolumeMl(
            -10.0, WeightUnit.KILOGRAM, 5.0, MassUnit.MILLIGRAM, 50.0, MassUnit.MILLIGRAM,
        )
        assertEquals(DoseResult.Invalid(InvalidReason.NEGATIVE_VALUE), r)
    }

    @Test
    fun `posologie negative est invalide`() {
        val r = computeVolumeMl(
            10.0, WeightUnit.KILOGRAM, -5.0, MassUnit.MILLIGRAM, 50.0, MassUnit.MILLIGRAM,
        )
        assertEquals(DoseResult.Invalid(InvalidReason.NEGATIVE_VALUE), r)
    }

    @Test
    fun `poids nul donne un volume nul`() {
        val r = computeVolumeMl(
            0.0, WeightUnit.KILOGRAM, 5.0, MassUnit.MILLIGRAM, 50.0, MassUnit.MILLIGRAM,
        )
        assertEquals(0.0, volumeOf(r), 1e-9)
    }

    // --- Concentration exprimée par un volume de référence ≠ 1 ml (ex. 250 µg / 5 ml) ---

    @Test
    fun `concentration 250ug pour 5ml`() {
        // 2 kg × 100 µg/kg = 200 µg ; conc = 250 µg / 5 ml = 50 µg/ml ; volume = 4 ml
        val r = computeVolumeMl(
            bodyWeight = 2.0, weightUnit = WeightUnit.KILOGRAM,
            dosePerKg = 100.0, doseUnit = MassUnit.MICROGRAM,
            concentration = 250.0, concentrationUnit = MassUnit.MICROGRAM,
            concentrationVolumeMl = 5.0,
        )
        assertEquals(4.0, volumeOf(r), 1e-9)
    }

    @Test
    fun `250ug pour 5ml equivaut a 50ug pour 1ml`() {
        val parVolume = computeVolumeMl(
            2.0, WeightUnit.KILOGRAM, 100.0, MassUnit.MICROGRAM,
            250.0, MassUnit.MICROGRAM, concentrationVolumeMl = 5.0,
        )
        val par1ml = computeVolumeMl(
            2.0, WeightUnit.KILOGRAM, 100.0, MassUnit.MICROGRAM,
            50.0, MassUnit.MICROGRAM, concentrationVolumeMl = 1.0,
        )
        assertEquals(volumeOf(par1ml), volumeOf(parVolume), 1e-9)
    }

    @Test
    fun `volume de reference par defaut vaut 1ml`() {
        // L'omission du volume de référence doit se comporter comme « par ml ».
        val sansVolume = computeVolumeMl(
            10.0, WeightUnit.KILOGRAM, 5.0, MassUnit.MILLIGRAM, 50.0, MassUnit.MILLIGRAM,
        )
        val avecVolume1 = computeVolumeMl(
            10.0, WeightUnit.KILOGRAM, 5.0, MassUnit.MILLIGRAM,
            50.0, MassUnit.MILLIGRAM, concentrationVolumeMl = 1.0,
        )
        assertEquals(volumeOf(avecVolume1), volumeOf(sansVolume), 1e-9)
    }

    @Test
    fun `volume de reference nul est invalide`() {
        val r = computeVolumeMl(
            10.0, WeightUnit.KILOGRAM, 5.0, MassUnit.MILLIGRAM,
            50.0, MassUnit.MILLIGRAM, concentrationVolumeMl = 0.0,
        )
        assertEquals(DoseResult.Invalid(InvalidReason.NON_POSITIVE_CONCENTRATION), r)
    }

    @Test
    fun `volume de reference negatif est invalide`() {
        val r = computeVolumeMl(
            10.0, WeightUnit.KILOGRAM, 5.0, MassUnit.MILLIGRAM,
            50.0, MassUnit.MILLIGRAM, concentrationVolumeMl = -5.0,
        )
        assertEquals(DoseResult.Invalid(InvalidReason.NON_POSITIVE_CONCENTRATION), r)
    }

    @Test
    fun `volume de reference depuis une chaine`() {
        val r = computeVolumeMlFromInput(
            "2", WeightUnit.KILOGRAM, "100", MassUnit.MICROGRAM,
            "250", MassUnit.MICROGRAM, concentrationVolumeText = "5",
        )
        assertEquals(4.0, volumeOf(r), 1e-9)
    }

    @Test
    fun `volume de reference vide est invalide`() {
        val r = computeVolumeMlFromInput(
            "2", WeightUnit.KILOGRAM, "100", MassUnit.MICROGRAM,
            "250", MassUnit.MICROGRAM, concentrationVolumeText = "",
        )
        assertEquals(DoseResult.Invalid(InvalidReason.EMPTY_OR_NON_NUMERIC), r)
    }

    // --- Parsing des chaînes (variante UI) ---

    @Test
    fun `chaine vide est invalide`() {
        val r = computeVolumeMlFromInput(
            "", WeightUnit.KILOGRAM, "5", MassUnit.MILLIGRAM, "50", MassUnit.MILLIGRAM,
        )
        assertEquals(DoseResult.Invalid(InvalidReason.EMPTY_OR_NON_NUMERIC), r)
    }

    @Test
    fun `chaine non numerique est invalide`() {
        val r = computeVolumeMlFromInput(
            "abc", WeightUnit.KILOGRAM, "5", MassUnit.MILLIGRAM, "50", MassUnit.MILLIGRAM,
        )
        assertEquals(DoseResult.Invalid(InvalidReason.EMPTY_OR_NON_NUMERIC), r)
    }

    @Test
    fun `virgule decimale est acceptee`() {
        // 2,5 kg, 4 mg/kg, 50 mg/ml -> (2.5*4)/50 = 0.2 ml
        val r = computeVolumeMlFromInput(
            "2,5", WeightUnit.KILOGRAM, "4", MassUnit.MILLIGRAM, "50", MassUnit.MILLIGRAM,
        )
        assertEquals(0.2, volumeOf(r), 1e-9)
    }

    @Test
    fun `point decimal est accepte`() {
        val r = computeVolumeMlFromInput(
            "2.5", WeightUnit.KILOGRAM, "4", MassUnit.MILLIGRAM, "50", MassUnit.MILLIGRAM,
        )
        assertEquals(0.2, volumeOf(r), 1e-9)
    }
}
