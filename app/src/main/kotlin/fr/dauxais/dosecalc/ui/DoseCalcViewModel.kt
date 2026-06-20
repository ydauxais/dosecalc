package fr.dauxais.dosecalc.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import fr.dauxais.dosecalc.logic.DoseResult
import fr.dauxais.dosecalc.logic.MassUnit
import fr.dauxais.dosecalc.logic.WeightUnit
import fr.dauxais.dosecalc.logic.computeVolumeMlFromInput

/**
 * ViewModel léger : ne détient QUE l'état de saisie et délègue tout le calcul à la
 * fonction pure [computeVolumeMlFromInput]. Aucune logique métier ici (invariant du projet).
 */
class DoseCalcViewModel : ViewModel() {

    var weightText by mutableStateOf("")
    var weightUnit by mutableStateOf(WeightUnit.KILOGRAM)

    var doseText by mutableStateOf("")
    var doseUnit by mutableStateOf(MassUnit.MILLIGRAM)

    var concentrationText by mutableStateOf("")
    var concentrationUnit by mutableStateOf(MassUnit.MILLIGRAM)

    /** Volume de référence de la concentration (ex. « 5 » pour 250 µg / 5 ml). 1 ml par défaut. */
    var concentrationVolumeText by mutableStateOf("1")

    /**
     * Résultat courant, ou `null` tant qu'aucune saisie n'a été faite (on n'affiche
     * alors ni volume ni erreur). Recalculé à la volée à partir de l'état de saisie.
     */
    val result: DoseResult?
        get() = if (weightText.isBlank() && doseText.isBlank() && concentrationText.isBlank()) {
            null
        } else {
            computeVolumeMlFromInput(
                bodyWeightText = weightText, weightUnit = weightUnit,
                dosePerKgText = doseText, doseUnit = doseUnit,
                concentrationText = concentrationText, concentrationUnit = concentrationUnit,
                concentrationVolumeText = concentrationVolumeText,
            )
        }
}
