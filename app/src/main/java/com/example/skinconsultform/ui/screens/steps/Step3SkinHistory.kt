package com.example.skinconsultform.ui.screens.steps

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.skinconsultform.domain.model.ConsultationForm
import com.example.skinconsultform.ui.components.*
import com.example.skinconsultform.ui.theme.StheticColors

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step3SkinHistory(
    form: ConsultationForm,
    onUpdate: (String, Any) -> Unit
) {
    val scrollState = rememberScrollState()

    val skinConcernOptions = listOf(
        "Acne / Breakouts", "Hyperpigmentation", "Fine Lines / Wrinkles",
        "Dryness", "Oiliness", "Sensitivity",
        "Redness / Rosacea", "Uneven texture", "Enlarged pores",
        "Scarring", "Warts", "Unwanted Hair", "Dark Underarms"
    )

    val treatmentOptions = listOf(
        "Chemical Peels", "Microdermabrasion",
        "Laser Treatments", "Botox / Fillers", "Microneedling",
        "LED Light Therapy", "Radio Frequency", "Pico Laser",
        "Diode Laser"
    )

    var showLastTreatmentPicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 32.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        StheticSectionHeader(
            title = "Skin History & Concerns",
            subtitle = "Help us understand your skin better"
        )

        // ── Skin concerns multi-select ────────────────────────────────
        Column {
            Text(
                text = "What are your main skin concerns?",
                style = MaterialTheme.typography.labelLarge,
                color = StheticColors.Charcoal700,
                modifier = Modifier.padding(bottom = 10.dp)
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                skinConcernOptions.forEach { concern ->
                    StheticChip(
                        label = concern,
                        isSelected = form.skinConcerns.contains(concern),
                        onClick = {
                            val updated = if (form.skinConcerns.contains(concern))
                                form.skinConcerns - concern
                            else
                                form.skinConcerns + concern
                            onUpdate("skinConcerns", updated)
                        }
                    )
                }
            }
        }

        GoldDivider()

        // ── Diagnosed skin condition ──────────────────────────────────
        Column {
            Text(
                text = "Have you ever been diagnosed with a skin condition?",
                style = MaterialTheme.typography.labelLarge,
                color = StheticColors.Charcoal700,
                modifier = Modifier.padding(bottom = 10.dp)
            )
            // Local state to track yes/no selection separately from text field
            var hasDiagnosis by remember {
                mutableStateOf(form.diagnosedSkinCondition.isNotBlank())
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StheticChip(
                    label = "No",
                    isSelected = !hasDiagnosis,
                    onClick = {
                        hasDiagnosis = false
                        onUpdate("diagnosedSkinCondition", "")
                    }
                )
                StheticChip(
                    label = "Yes",
                    isSelected = hasDiagnosis,
                    onClick = {
                        hasDiagnosis = true
                        // Set a placeholder so the text field becomes visible
                        // User will type the actual condition in the field below
                        if (form.diagnosedSkinCondition.isBlank()) {
                            onUpdate("diagnosedSkinCondition", " ")
                        }
                    }
                )
            }

            AnimatedVisibility(
                visible = hasDiagnosis,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                StheticTextField(
                    value = form.diagnosedSkinCondition,
                    onValueChange = { onUpdate("diagnosedSkinCondition", it) },
                    label = "Please specify",
                    placeholder = "e.g. Atopic dermatitis",
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
        }

        GoldDivider()

        // ── Previous treatments ───────────────────────────────────────
        Column {
            Text(
                text = "Have you ever had any of the following treatments?",
                style = MaterialTheme.typography.labelLarge,
                color = StheticColors.Charcoal700,
                modifier = Modifier.padding(bottom = 10.dp)
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                treatmentOptions.forEach { treatment ->
                    StheticChip(
                        label = treatment,
                        isSelected = form.previousTreatments.contains(treatment),
                        onClick = {
                            val updated =
                                if (form.previousTreatments.contains(treatment))
                                    form.previousTreatments - treatment
                                else
                                    form.previousTreatments + treatment
                            onUpdate("previousTreatments", updated)
                        }
                    )
                }
            }
        }

        // Date of last treatment — only if any selected
        AnimatedVisibility(
            visible = form.previousTreatments.isNotEmpty(),
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column {
                Text(
                    text = "Date of last treatment",
                    style = MaterialTheme.typography.labelLarge,
                    color = StheticColors.Charcoal700,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                OutlinedButton(
                    onClick = { showLastTreatmentPicker = true },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = StheticColors.Cream200,
                        contentColor = if (form.lastTreatmentDate.isBlank())
                            StheticColors.Charcoal300 else StheticColors.Charcoal900
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = form.lastTreatmentDate.ifBlank { "Select date" },
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        if (showLastTreatmentPicker) {
            DatePickerDialog(
                onDismissRequest = { showLastTreatmentPicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        showLastTreatmentPicker = false
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = java.time.Instant
                                .ofEpochMilli(millis)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate()
                            onUpdate(
                                "lastTreatmentDate",
                                date.format(
                                    java.time.format.DateTimeFormatter
                                        .ofPattern("dd MMM yyyy")
                                )
                            )
                        }
                    }) { Text("Confirm") }
                },
                dismissButton = {
                    TextButton(onClick = { showLastTreatmentPicker = false }) {
                        Text("Cancel")
                    }
                }
            ) { DatePicker(state = datePickerState) }
        }

        GoldDivider()

        // ── Current skincare products ─────────────────────────────────
        Text(
            text = "What skincare products are you currently using?",
            style = MaterialTheme.typography.labelLarge,
            color = StheticColors.Charcoal700
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StheticTextField(
                value = form.cleanser,
                onValueChange = { onUpdate("cleanser", it) },
                label = "Cleanser",
                placeholder = "Brand / product name",
                modifier = Modifier.weight(1f)
            )
            StheticTextField(
                value = form.moisturizer,
                onValueChange = { onUpdate("moisturizer", it) },
                label = "Moisturizer",
                placeholder = "Brand / product name",
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StheticTextField(
                value = form.spfProduct,
                onValueChange = { onUpdate("spfProduct", it) },
                label = "SPF / Sunscreen",
                placeholder = "Brand / SPF level",
                modifier = Modifier.weight(1f)
            )
            StheticTextField(
                value = form.otherProducts,
                onValueChange = { onUpdate("otherProducts", it) },
                label = "Other",
                placeholder = "Serums, treatments, etc.",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}