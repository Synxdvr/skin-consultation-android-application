package com.example.skinconsultform.ui.screens

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.skinconsultform.domain.model.*
import com.example.skinconsultform.ui.components.*
import com.example.skinconsultform.ui.navigation.Screen
import com.example.skinconsultform.ui.theme.StheticColors
import com.example.skinconsultform.ui.viewmodel.*
import kotlin.collections.emptyList

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ResultsScreen(
    navController: NavController,
    consultationId: Long,
    viewModel: ConsultationViewModel = hiltViewModel()
) {
    val adminViewModel: AdminViewModel = hiltViewModel()
    val pdfShareIntent by adminViewModel.pdfShareIntent.collectAsState()
    val selectedEntity by adminViewModel.selectedConsultation.collectAsState()
    val submitState by viewModel.submitState.collectAsState()
    val scrollState = rememberScrollState()

    val result = (submitState as? SubmitState.Success)?.result

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        adminViewModel.clearPdfIntent()
    }

    LaunchedEffect(consultationId) {
        adminViewModel.selectConsultation(consultationId)
    }

    LaunchedEffect(pdfShareIntent) {
        pdfShareIntent?.let { intent ->
            launcher.launch(Intent.createChooser(intent, "Share Consultation PDF"))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StheticColors.Cream50)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {

            // ── Header ────────────────────────────────────────────────
            Surface(
                color = StheticColors.White,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "S'thetic",
                        style = MaterialTheme.typography.titleLarge,
                        color = StheticColors.Gold700
                    )
                    Text(
                        text = "Consultation Complete",
                        style = MaterialTheme.typography.headlineMedium,
                        color = StheticColors.Charcoal900,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    GoldDivider(
                        modifier = Modifier
                            .width(80.dp)
                            .padding(top = 12.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {

                // ── Show loading spinner while entity loads ────────────
                if (selectedEntity == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = StheticColors.Gold500)
                    }
                } else {
                    val ent = selectedEntity!!

                    // ── Client summary ────────────────────────────────
                    ResultSectionCard(title = "Client Summary") {
                        ResultRow("Name", ent.clientName)
                        ResultRow("Date of birth", ent.dateOfBirth)
                        ResultRow("Age", "${ent.age} years old")
                        ResultRow("Gender", ent.gender)
                        ResultRow("Phone", ent.phone)
                        if (ent.email.isNotBlank())
                            ResultRow("Email", ent.email)
                        if (ent.occupation.isNotBlank())
                            ResultRow("Occupation", ent.occupation)
                    }

                    // ── Skin concerns ─────────────────────────────────
                    val skinConcerns = try {
                        com.google.gson.Gson().fromJson<List<String>>(
                            ent.skinConcerns,
                            object : com.google.gson.reflect.TypeToken<List<String>>() {}.type
                        ) ?: emptyList()
                    } catch (e: Exception) { emptyList() }

                    if (skinConcerns.isNotEmpty()) {
                        ResultSectionCard(title = "Reported Skin Concerns") {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                skinConcerns.forEach { concern ->
                                    StheticChip(
                                        label = concern,
                                        isSelected = true,
                                        onClick = {}
                                    )
                                }
                            }
                        }
                    }

                    // ── Medical flags ─────────────────────────────────
                    val medicalConditions = try {
                        com.google.gson.Gson().fromJson<List<String>>(
                            ent.medicalConditions,
                            object : com.google.gson.reflect.TypeToken<List<String>>() {}.type
                        ) ?: emptyList()
                    } catch (e: Exception) { emptyList() }

                    val medicalFlags = buildList {
                        addAll(medicalConditions)
                        if (ent.isPregnantOrBreastfeeding) add("Pregnant / Breastfeeding")
                        if (ent.medications.isNotBlank()) add("On medication")
                    }
                    if (medicalFlags.isNotEmpty()) {
                        ResultSectionCard(title = "Medical Notes") {
                            medicalFlags.forEach { flag ->
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "•",
                                        color = StheticColors.Warning,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.padding(end = 8.dp, top = 2.dp)
                                    )
                                    Text(
                                        text = flag,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = StheticColors.Charcoal900
                                    )
                                }
                            }
                            if (ent.medications.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Medications: ${ent.medications}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = StheticColors.Charcoal500
                                )
                            }
                        }
                    }

                    // ── AI Narrative + Recommendations ────────────────
                    val recommendations = remember(ent.aiRecommendations) {
                        try {
                            com.google.gson.Gson().fromJson<List<TreatmentRecommendation>>(
                                ent.aiRecommendations,
                                object : com.google.gson.reflect.TypeToken
                                        <List<TreatmentRecommendation>>() {}.type
                            ) ?: emptyList()
                        } catch (e: Exception) { emptyList() }
                    }

                    // ── Parse AI narrative from saved entity ──────────────────────────
                    // The narrative is stored separately — we need to add it to the entity
                    // For now read it from the in-memory result if available,
                    // otherwise fall back to a generic message
                    val aiNarrative = ent.aiNarrative.ifBlank {
                        "Thank you for completing your consultation. Our specialist " +
                                "has reviewed your skin profile and prepared the " +
                                "recommendations below tailored specifically for you."
                    }

                    // ── Specialist Assessment ─────────────────────────────────────────
                    if (aiNarrative.isNotBlank()) {
                        ResultSectionCard(
                            title = "Specialist Assessment",
                            accentColor = StheticColors.Gold50
                        ) {
                            Text(
                                text = aiNarrative,
                                style = MaterialTheme.typography.bodyLarge,
                                color = StheticColors.Charcoal700,
                                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                            )
                        }
                    }

                    // ── Active recommendations ────────────────────────────────────────
                    val activeRecs = recommendations
                        .filter { !it.isContraindicated }
                        .sortedBy { it.priority }

                    val contraindicated = recommendations
                        .filter { it.isContraindicated }

                    if (activeRecs.isNotEmpty()) {
                        ResultSectionCard(title = "Recommended Treatments") {
                            activeRecs.forEach { rec ->
                                RecommendationCard(recommendation = rec)
                                if (rec != activeRecs.last()) {
                                    GoldDivider(
                                        modifier = Modifier.padding(vertical = 12.dp)
                                    )
                                }
                            }
                        }
                    }

                    // ── Contraindicated ───────────────────────────────────────────────
                    if (contraindicated.isNotEmpty()) {
                        ResultSectionCard(
                            title = "Treatments to Discuss with Specialist",
                            accentColor = StheticColors.WarningLight
                        ) {
                            contraindicated.forEach { rec ->
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "⚠",
                                        color = StheticColors.Warning,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Column {
                                        Text(
                                            text = rec.treatmentName,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = StheticColors.Charcoal900
                                        )
                                        Text(
                                            text = rec.contraindicationNote,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = StheticColors.Warning
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ── Signature ─────────────────────────────────────
                    if (ent.signatureBase64.isNotBlank()) {
                        ResultSectionCard(title = "Client Signature") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column {
                                    val bytes = Base64.decode(
                                        ent.signatureBase64,
                                        Base64.DEFAULT
                                    )
                                    val bitmap = BitmapFactory
                                        .decodeByteArray(bytes, 0, bytes.size)
                                    bitmap?.let {
                                        Image(
                                            bitmap = it.asImageBitmap(),
                                            contentDescription = "Client signature",
                                            modifier = Modifier
                                                .height(80.dp)
                                                .widthIn(max = 240.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(StheticColors.White)
                                                .border(
                                                    1.dp,
                                                    StheticColors.Cream300,
                                                    RoundedCornerShape(8.dp)
                                                )
                                        )
                                    }
                                    GoldDivider(
                                        modifier = Modifier
                                            .width(200.dp)
                                            .padding(top = 8.dp)
                                    )
                                    Text(
                                        text = ent.clientName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = StheticColors.Charcoal500
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = java.time.LocalDate.now().format(
                                            java.time.format.DateTimeFormatter
                                                .ofPattern("dd MMMM yyyy")
                                        ),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = StheticColors.Charcoal500
                                    )
                                    if (ent.consultantName.isNotBlank()) {
                                        Text(
                                            text = "Consultant: ${ent.consultantName}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = StheticColors.Charcoal500
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ── Action buttons ────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StheticSecondaryButton(
                        text = "New Consultation",
                        onClick = {
                            viewModel.resetForm()
                            navController.navigate(Screen.Welcome.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    StheticPrimaryButton(
                        text = "Export PDF",
                        onClick = {
                            adminViewModel.exportPdfById(consultationId)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

// ── Supporting composables ─────────────────────────────────────────────

@Composable
fun ResultSectionCard(
    title: String,
    accentColor: androidx.compose.ui.graphics.Color = StheticColors.White,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(accentColor)
            .border(1.dp, StheticColors.Cream300, RoundedCornerShape(16.dp))
            .padding(24.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = StheticColors.Gold800
        )
        GoldDivider(modifier = Modifier.padding(vertical = 10.dp))
        content()
    }
}

@Composable
fun ResultRow(label: String, value: String) {
    if (value.isBlank()) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = StheticColors.Charcoal500,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = StheticColors.Charcoal900,
            modifier = Modifier.weight(0.6f),
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun RecommendationCard(recommendation: TreatmentRecommendation) {
    val categoryColor = when (recommendation.category) {
        RecommendationCategory.FACIAL_TREATMENT   -> StheticColors.Gold100
        RecommendationCategory.SKIN_CONCERN       -> StheticColors.Rose50
        RecommendationCategory.LIFESTYLE_ADVISORY -> StheticColors.Sage50
        RecommendationCategory.PRODUCT_SUGGESTION -> StheticColors.Cream200
        RecommendationCategory.CAUTION            -> StheticColors.WarningLight
    }

    val categoryLabel = when (recommendation.category) {
        RecommendationCategory.FACIAL_TREATMENT   -> "Treatment"
        RecommendationCategory.SKIN_CONCERN       -> "Skin care"
        RecommendationCategory.LIFESTYLE_ADVISORY -> "Lifestyle"
        RecommendationCategory.PRODUCT_SUGGESTION -> "Product"
        RecommendationCategory.CAUTION            -> "Caution"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(StheticColors.Gold500)
                .border(
                    1.dp,
                    StheticColors.Gold700,
                    RoundedCornerShape(10.dp)
                )
        ) {
            Text(
                text = "#${recommendation.priority}",
                style = MaterialTheme.typography.labelMedium,
                color = StheticColors.White
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = recommendation.treatmentName,
                    style = MaterialTheme.typography.titleMedium,
                    color = StheticColors.Charcoal900
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(categoryColor)
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = categoryLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = StheticColors.Gold800
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = recommendation.reason,
                style = MaterialTheme.typography.bodyMedium,
                color = StheticColors.Charcoal500
            )
        }
    }
}