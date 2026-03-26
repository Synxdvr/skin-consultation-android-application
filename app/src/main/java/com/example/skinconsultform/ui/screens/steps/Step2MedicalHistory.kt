package com.example.skinconsultform.ui.screens.steps

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.skinconsultform.domain.model.ConsultationForm
import com.example.skinconsultform.ui.components.*

@Composable
fun Step2MedicalHistory(
    form: ConsultationForm,
    onUpdate: (String, Any) -> Unit
) {
    val scrollState = rememberScrollState()

    val medicalOptions = listOf(
        "Heart condition",
        "High blood pressure",
        "Epilepsy",
        "Diabetes",
        "Skin conditions (eczema, psoriasis, rosacea)",
        "Hormonal imbalance"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 32.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StheticSectionHeader(
            title = "Medical History",
            subtitle = "Please check all that apply"
        )

        // ── Medical conditions checkboxes ─────────────────────────────
        medicalOptions.forEach { condition ->
            val isChecked = form.medicalConditions.contains(condition)
            StheticToggleRow(
                label = condition,
                checked = isChecked,
                onCheckedChange = { checked ->
                    val updated = if (checked)
                        form.medicalConditions + condition
                    else
                        form.medicalConditions - condition
                    onUpdate("medicalConditions", updated)
                }
            )
        }

        // ── Cancer — with conditional text field ──────────────────────
        var hasCancer by remember {
            mutableStateOf(
                form.cancerDetails.isNotBlank() ||
                        form.medicalConditions.contains("Cancer")
            )
        }

        StheticToggleRow(
            label = "Cancer",
            subtitle = "If yes, please specify type and year",
            checked = hasCancer,
            onCheckedChange = { checked ->
                hasCancer = checked
                if (!checked) {
                    onUpdate("cancerDetails", "")
                    val updated = form.medicalConditions - "Cancer"
                    onUpdate("medicalConditions", updated)
                } else {
                    val updated = form.medicalConditions + "Cancer"
                    onUpdate("medicalConditions", updated)
                }
            }
        )
        AnimatedVisibility(
            visible = hasCancer,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            StheticTextField(
                value = form.cancerDetails,
                onValueChange = { onUpdate("cancerDetails", it) },
                label = "Cancer type & year",
                placeholder = "e.g. Breast cancer, 2019",
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // ── Allergies — conditional ───────────────────────────────────
        var hasAllergies by remember {
            mutableStateOf(form.allergyDetails.isNotBlank())
        }

        StheticToggleRow(
            label = "Allergies",
            subtitle = "Food, medication, or topical products",
            checked = hasAllergies,
            onCheckedChange = { checked ->
                hasAllergies = checked
                if (!checked) onUpdate("allergyDetails", "")
            }
        )
        AnimatedVisibility(
            visible = hasAllergies,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            StheticTextField(
                value = form.allergyDetails,
                onValueChange = { onUpdate("allergyDetails", it) },
                label = "Please list your allergies",
                placeholder = "e.g. Penicillin, fragrance, nuts",
                singleLine = false,
                minLines = 2,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        GoldDivider(modifier = Modifier.padding(vertical = 4.dp))

        // ── Pregnancy ─────────────────────────────────────────────────
        StheticYesNoRow(
            question = "Are you currently pregnant or breastfeeding?",
            value = form.isPregnantOrBreastfeeding,
            onValueChange = { onUpdate("isPregnantOrBreastfeeding", it) },
            warningIfYes = true
        )
        AnimatedVisibility(visible = form.isPregnantOrBreastfeeding) {
            StheticWarningBanner(
                message = "Some treatments may not be suitable during pregnancy " +
                        "or breastfeeding. Our specialist will advise you."
            )
        }

        // ── Medications ───────────────────────────────────────────────
        var hasMedications by remember {
            mutableStateOf(form.medications.isNotBlank())
        }

        StheticYesNoRow(
            question = "Are you taking any medication or supplements?",
            value = hasMedications,
            onValueChange = { taking ->
                hasMedications = taking
                if (!taking) onUpdate("medications", "")
            }
        )
        AnimatedVisibility(
            visible = hasMedications,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            StheticTextField(
                value = form.medications,
                onValueChange = { onUpdate("medications", it) },
                label = "Please list your medications",
                placeholder = "e.g. Metformin 500mg, Vitamin C",
                singleLine = false,
                minLines = 3,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}