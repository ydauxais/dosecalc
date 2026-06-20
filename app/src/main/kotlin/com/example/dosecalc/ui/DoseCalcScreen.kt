package com.example.dosecalc.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dosecalc.R
import com.example.dosecalc.logic.DoseResult
import com.example.dosecalc.logic.InvalidReason
import com.example.dosecalc.logic.MassUnit
import com.example.dosecalc.logic.WeightUnit
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoseCalcScreen(viewModel: DoseCalcViewModel = viewModel()) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.title)) }) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Poids : valeur + unité (kg / g)
            MeasureRow(
                label = stringResource(R.string.weight_label),
                value = viewModel.weightText,
                onValueChange = { viewModel.weightText = it },
                selectedUnitLabel = weightUnitLabel(viewModel.weightUnit),
                unitOptions = WeightUnit.entries.map { it to weightUnitLabel(it) },
                onUnitSelected = { viewModel.weightUnit = it },
            )

            // Posologie : masse (mg / µg) par kg
            MeasureRow(
                label = stringResource(R.string.dose_label),
                value = viewModel.doseText,
                onValueChange = { viewModel.doseText = it },
                selectedUnitLabel = massUnitLabel(viewModel.doseUnit),
                unitOptions = doseUnits.map { it to massUnitLabel(it) },
                onUnitSelected = { viewModel.doseUnit = it },
            )

            // Concentration : masse (mg / µg) par ml
            MeasureRow(
                label = stringResource(R.string.concentration_label),
                value = viewModel.concentrationText,
                onValueChange = { viewModel.concentrationText = it },
                selectedUnitLabel = massUnitLabel(viewModel.concentrationUnit),
                unitOptions = doseUnits.map { it to massUnitLabel(it) },
                onUnitSelected = { viewModel.concentrationUnit = it },
            )

            ResultCard(result = viewModel.result)

            OutlinedTextField(
                value = viewModel.disclaimer,
                onValueChange = { viewModel.disclaimer = it },
                label = { Text(stringResource(R.string.disclaimer_label)) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** Unités de masse proposées pour la posologie et la concentration. */
private val doseUnits = listOf(MassUnit.MILLIGRAM, MassUnit.MICROGRAM)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> MeasureRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    selectedUnitLabel: String,
    unitOptions: List<Pair<T, String>>,
    onUnitSelected: (T) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f),
        )

        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.width(110.dp),
        ) {
            OutlinedTextField(
                value = selectedUnitLabel,
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                unitOptions.forEach { (unit, unitLabel) ->
                    DropdownMenuItem(
                        text = { Text(unitLabel) },
                        onClick = {
                            onUnitSelected(unit)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultCard(result: DoseResult?) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.result_label),
                style = MaterialTheme.typography.titleMedium,
            )
            when (result) {
                is DoseResult.Success -> Text(
                    text = "${formatVolume(result.volumeMl)} ${stringResource(R.string.result_unit)}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                is DoseResult.Invalid -> Text(
                    text = errorMessage(result.reason),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
                null -> Text(
                    text = stringResource(R.string.result_placeholder),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun errorMessage(reason: InvalidReason): String = stringResource(
    when (reason) {
        InvalidReason.EMPTY_OR_NON_NUMERIC -> R.string.error_empty
        InvalidReason.NEGATIVE_VALUE -> R.string.error_negative
        InvalidReason.NON_POSITIVE_CONCENTRATION -> R.string.error_concentration
    },
)

@Composable
private fun weightUnitLabel(unit: WeightUnit): String = stringResource(
    when (unit) {
        WeightUnit.KILOGRAM -> R.string.unit_kg
        WeightUnit.GRAM -> R.string.unit_g
    },
)

@Composable
private fun massUnitLabel(unit: MassUnit): String = stringResource(
    when (unit) {
        MassUnit.MILLIGRAM -> R.string.unit_mg
        MassUnit.MICROGRAM -> R.string.unit_ug
        MassUnit.GRAM -> R.string.unit_g
    },
)

/**
 * Formate un volume en ml : jusqu'à 3 décimales, séparateur décimal français,
 * zéros de fin supprimés (1.0 -> "1", 0.4 -> "0,4", 0.125 -> "0,125").
 */
private fun formatVolume(volumeMl: Double): String {
    val rounded = String.format(Locale.FRANCE, "%.3f", volumeMl)
    return if (rounded.contains(',')) rounded.trimEnd('0').trimEnd(',') else rounded
}
