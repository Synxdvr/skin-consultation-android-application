package com.example.skinconsultform.ui.screens.steps

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.skinconsultform.domain.model.ConsultationForm
import com.example.skinconsultform.ui.components.*
import kotlin.math.roundToInt

@Composable
fun Step4Lifestyle(
    form: ConsultationForm,
    onUpdate: (String, Any) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 32.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        StheticSectionHeader(
            title = "Lifestyle & Habits",
            subtitle = "This helps us tailor your treatment plan"
        )

        // ── Sleep slider ──────────────────────────────────────────────
        StheticLabeledSlider(
            label = "Hours of sleep per night",
            value = form.sleepHours,
            onValueChange = { onUpdate("sleepHours", it) },
            valueRange = 1f..15f,
            steps = 15,
            displayValue = "${form.sleepHours.roundToInt()} hrs"
        )

        // ── Water slider ──────────────────────────────────────────────
        StheticLabeledSlider(
            label = "Water intake per day",
            value = form.waterLiters,
            onValueChange = { onUpdate("waterLiters", it) },
            valueRange = 1f..5f,
            steps = 7,
            displayValue = "${"%.1f".format(form.waterLiters)} L"
        )

        GoldDivider()

        // ── Yes / No questions ────────────────────────────────────────
        StheticYesNoRow(
            question = "Do you smoke?",
            value = form.smokes,
            onValueChange = { onUpdate("smokes", it) },
            warningIfYes = true
        )

        StheticYesNoRow(
            question = "Do you consume alcohol?",
            value = form.drinksAlcohol,
            onValueChange = { onUpdate("drinksAlcohol", it) }
        )

        StheticYesNoRow(
            question = "Do you experience high stress levels?",
            value = form.highStress,
            onValueChange = { onUpdate("highStress", it) },
            warningIfYes = true
        )

        StheticYesNoRow(
            question = "Are you regularly exposed to the sun?",
            value = form.sunExposure,
            onValueChange = { onUpdate("sunExposure", it) }
        )

        StheticYesNoRow(
            question = "Do you wear SPF daily?",
            value = form.wearsSpfDaily,
            onValueChange = { onUpdate("wearsSpfDaily", it) }
        )

        Spacer(modifier = Modifier.height(80.dp))
    }
}