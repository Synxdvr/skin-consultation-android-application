package com.example.skinconsultform.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.skinconsultform.BuildConfig
import com.example.skinconsultform.data.db.ConsultationEntity
import com.example.skinconsultform.ui.components.*
import com.example.skinconsultform.ui.theme.StheticColors
import com.example.skinconsultform.ui.viewmodel.AdminViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminScreen(
    navController: NavController,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val isAuthenticated   by viewModel.isAuthenticated.collectAsState()
    val consultations     by viewModel.consultations.collectAsState()
    val selectedConsult   by viewModel.selectedConsultation.collectAsState()

    AnimatedContent(
        targetState = isAuthenticated,
        transitionSpec = {
            fadeIn(tween(400)) togetherWith fadeOut(tween(400))
        },
        label = "adminAuth"
    ) { authenticated ->
        if (!authenticated) {
            AdminPinGate(
                onPinEntered = { pin -> viewModel.authenticate(pin) },
                onBack = { navController.navigateUp() }
            )
        } else {
            AnimatedContent(
                targetState = selectedConsult,
                transitionSpec = {
                    slideInHorizontally(tween(350)) { it } + fadeIn(tween(350)) togetherWith
                            slideOutHorizontally(tween(350)) { -it } + fadeOut(tween(350))
                },
                label = "adminDetail"
            ) { selected ->
                if (selected == null) {
                    AdminConsultationList(
                        consultations = consultations,
                        onSelect = { viewModel.selectConsultation(it.id) },
                        onLogout = { viewModel.logout() },
                        onBack = { navController.navigateUp() }
                    )
                } else {
                    AdminConsultationDetail(
                        entity = selected,
                        onBack = { viewModel.selectConsultation(-1L) },
                        navController = navController
                    )
                }
            }
        }
    }
}

// ── PIN Gate ───────────────────────────────────────────────────────────

@Composable
fun AdminPinGate(
    onPinEntered: (String) -> Boolean,
    onBack: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    val pinLength = 4

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StheticColors.Cream50),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = StheticColors.Charcoal500
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier
                .fillMaxWidth(0.45f)
                .padding(32.dp)
        ) {
            Text(
                text = "Staff Access",
                style = MaterialTheme.typography.headlineMedium,
                color = StheticColors.Gold700
            )
            Text(
                text = "Enter your 4-digit PIN to continue",
                style = MaterialTheme.typography.bodyMedium,
                color = StheticColors.Charcoal500,
                textAlign = TextAlign.Center
            )

            // ── PIN dots ──────────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                repeat(pinLength) { index ->
                    val filled = index < pin.length
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (filled) StheticColors.Gold500
                                else StheticColors.Cream300
                            )
                            .border(
                                1.dp,
                                if (isError) StheticColors.Error
                                else StheticColors.Gold300,
                                RoundedCornerShape(50)
                            )
                    )
                }
            }

            if (isError) {
                Text(
                    text = "Incorrect PIN. Please try again.",
                    style = MaterialTheme.typography.bodySmall,
                    color = StheticColors.Error
                )
            }

            // ── Number pad ────────────────────────────────────────────
            val keys = listOf(
                listOf("1","2","3"),
                listOf("4","5","6"),
                listOf("7","8","9"),
                listOf("","0","⌫")
            )
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                keys.forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        row.forEach { key ->
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (key.isBlank()) StheticColors.Cream50
                                        else StheticColors.White
                                    )
                                    .border(
                                        1.dp,
                                        if (key.isBlank()) StheticColors.Cream50
                                        else StheticColors.Cream300,
                                        RoundedCornerShape(14.dp)
                                    )
                                    .clickable(enabled = key.isNotBlank()) {
                                        isError = false
                                        when (key) {
                                            "⌫" -> if (pin.isNotEmpty())
                                                pin = pin.dropLast(1)
                                            else -> if (pin.length < pinLength) {
                                                pin += key
                                                if (pin.length == pinLength) {
                                                    val success = onPinEntered(pin)
                                                    if (!success) {
                                                        isError = true
                                                        pin = ""
                                                    }
                                                }
                                            }
                                        }
                                    }
                            ) {
                                Text(
                                    text = key,
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = if (key.isBlank())
                                        StheticColors.Cream50
                                    else StheticColors.Charcoal900
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Consultation list ──────────────────────────────────────────────────

@Composable
fun AdminConsultationList(
    consultations: List<ConsultationEntity>,
    onSelect: (ConsultationEntity) -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StheticColors.Cream50)
    ) {
        // Top bar
        Surface(
            color = StheticColors.White,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = StheticColors.Charcoal500
                        )
                    }
                    Column {
                        Text(
                            text = "Admin Panel",
                            style = MaterialTheme.typography.titleLarge,
                            color = StheticColors.Gold700
                        )
                        Text(
                            text = "${consultations.size} consultations",
                            style = MaterialTheme.typography.bodySmall,
                            color = StheticColors.Charcoal500
                        )
                    }
                }
                StheticSecondaryButton(
                    text = "Log Out",
                    onClick = onLogout,
                    modifier = Modifier.width(120.dp)
                )
            }
        }

        if (consultations.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "No consultations yet",
                        style = MaterialTheme.typography.headlineMedium,
                        color = StheticColors.Charcoal300
                    )
                    Text(
                        text = "Completed consultations will appear here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = StheticColors.Charcoal300
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // ── Summary stat cards ────────────────────────────────
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AdminStatCard(
                            label = "Total",
                            value = consultations.size.toString(),
                            modifier = Modifier.weight(1f)
                        )
                        AdminStatCard(
                            label = "Today",
                            value = consultations.count { entity ->
                                val today = Calendar.getInstance().apply {
                                    set(Calendar.HOUR_OF_DAY, 0)
                                    set(Calendar.MINUTE, 0)
                                    set(Calendar.SECOND, 0)
                                }.timeInMillis
                                entity.submittedAt >= today
                            }.toString(),
                            modifier = Modifier.weight(1f)
                        )
                        AdminStatCard(
                            label = "This week",
                            value = consultations.count { entity ->
                                val weekAgo = System.currentTimeMillis() -
                                        (7 * 24 * 60 * 60 * 1000L)
                                entity.submittedAt >= weekAgo
                            }.toString(),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    GoldDivider(modifier = Modifier.padding(vertical = 4.dp))
                }

                // ── Consultation rows ─────────────────────────────────
                items(
                    items = consultations,
                    key = { it.id }
                ) { entity ->
                    AdminConsultationRow(
                        entity = entity,
                        onClick = { onSelect(entity) }
                    )
                }
            }
        }
    }
}

@Composable
fun AdminStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(StheticColors.White)
            .border(1.dp, StheticColors.Cream300, RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineLarge,
            color = StheticColors.Gold700
        )
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = StheticColors.Charcoal500
        )
    }
}

@Composable
fun AdminConsultationRow(
    entity: ConsultationEntity,
    onClick: () -> Unit
) {
    val dateFormat = remember {
        SimpleDateFormat("dd MMM yyyy  •  hh:mm a", Locale.getDefault())
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(StheticColors.White)
            .border(1.dp, StheticColors.Cream300, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Initials avatar
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(StheticColors.Gold50)
                .border(1.dp, StheticColors.Gold300, RoundedCornerShape(12.dp))
        ) {
            val initials = entity.clientName
                .split(" ")
                .mapNotNull { it.firstOrNull()?.toString() }
                .take(2)
                .joinToString("")
                .uppercase()
            Text(
                text = initials,
                style = MaterialTheme.typography.titleMedium,
                color = StheticColors.Gold700
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entity.clientName,
                style = MaterialTheme.typography.titleMedium,
                color = StheticColors.Charcoal900
            )
            Text(
                text = dateFormat.format(Date(entity.submittedAt)),
                style = MaterialTheme.typography.bodySmall,
                color = StheticColors.Charcoal500
            )
            if (entity.consultantName.isNotBlank()) {
                Text(
                    text = "Consultant: ${entity.consultantName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = StheticColors.Gold500
                )
            }
        }

        // Chevron
        Text(
            text = "›",
            style = MaterialTheme.typography.headlineLarge,
            color = StheticColors.Charcoal300
        )
    }
}

// ── Consultation detail view ───────────────────────────────────────────

@Composable
fun AdminConsultationDetail(
    entity: ConsultationEntity,
    onBack: () -> Unit,
    navController: NavController,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val pdfShareIntent by viewModel.pdfShareIntent.collectAsState()
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.clearPdfIntent()
    }

    LaunchedEffect(pdfShareIntent) {
        pdfShareIntent?.let { intent ->
            launcher.launch(
                Intent.createChooser(intent, "Share Consultation PDF")
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StheticColors.Cream50)
    ) {
        Surface(
            color = StheticColors.White,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = StheticColors.Charcoal500
                        )
                    }
                    Text(
                        text = entity.clientName,
                        style = MaterialTheme.typography.titleLarge,
                        color = StheticColors.Gold700
                    )
                }
                StheticPrimaryButton(
                    text = "Export PDF",
                    onClick = { /* Phase 7 */ },
                    modifier = Modifier.width(160.dp)
                )
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ResultSectionCard(title = "Client Details") {
                    ResultRow("Name", entity.clientName)
                    ResultRow("Date of birth", entity.dateOfBirth)
                    ResultRow("Age", "${entity.age} years old")
                    ResultRow("Gender", entity.gender)
                    ResultRow("Phone", entity.phone)
                    ResultRow("Email", entity.email)
                    ResultRow("Occupation", entity.occupation)
                    ResultRow("Source", entity.source)
                }
            }

            item {
                ResultSectionCard(title = "Submitted") {
                    ResultRow(
                        "Date & time",
                        SimpleDateFormat(
                            "dd MMM yyyy  •  hh:mm a",
                            Locale.getDefault()
                        ).format(Date(entity.submittedAt))
                    )
                    if (entity.consultantName.isNotBlank())
                        ResultRow("Consultant", entity.consultantName)
                }
            }

            item {
                val conditions = try {
                    com.google.gson.Gson()
                        .fromJson<List<String>>(
                            entity.medicalConditions,
                            object : com.google.gson.reflect.TypeToken
                                    <List<String>>() {}.type
                        )
                } catch (e: Exception) { emptyList() }

                if (conditions.isNotEmpty() ||
                    entity.isPregnantOrBreastfeeding ||
                    entity.medications.isNotBlank()
                ) {
                    ResultSectionCard(title = "Medical") {
                        conditions.forEach {
                            ResultRow("Condition", it)
                        }
                        if (entity.isPregnantOrBreastfeeding)
                            ResultRow("Note", "Pregnant / Breastfeeding")
                        if (entity.medications.isNotBlank())
                            ResultRow("Medications", entity.medications)
                        if (entity.allergyDetails.isNotBlank())
                            ResultRow("Allergies", entity.allergyDetails)
                    }
                }
            }

            item {
                val concerns = try {
                    com.google.gson.Gson()
                        .fromJson<List<String>>(
                            entity.skinConcerns,
                            object : com.google.gson.reflect.TypeToken
                                    <List<String>>() {}.type
                        )
                } catch (e: Exception) { emptyList() }

                ResultSectionCard(title = "Skin Concerns") {
                    if (concerns.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            concerns.forEach { concern ->
                                StheticChip(
                                    label = concern,
                                    isSelected = true,
                                    onClick = {}
                                )
                            }
                        }
                    } else {
                        Text(
                            "None reported",
                            style = MaterialTheme.typography.bodyMedium,
                            color = StheticColors.Charcoal300
                        )
                    }
                }
            }

            item {
                ResultSectionCard(title = "Lifestyle") {
                    ResultRow("Smokes", if (entity.smokes) "Yes" else "No")
                    ResultRow(
                        "Alcohol",
                        if (entity.drinksAlcohol) "Yes" else "No"
                    )
                    ResultRow("Sleep", "${entity.sleepHours} hrs/night")
                    ResultRow("Water", "${entity.waterLiters} L/day")
                    ResultRow(
                        "High stress",
                        if (entity.highStress) "Yes" else "No"
                    )
                    ResultRow(
                        "Sun exposure",
                        if (entity.sunExposure) "Yes" else "No"
                    )
                    ResultRow(
                        "Wears SPF daily",
                        if (entity.wearsSpfDaily) "Yes" else "No"
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }

        Text(
            text = "S\'thetic v${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.labelSmall,
            color = StheticColors.Charcoal300,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            textAlign = TextAlign.Center
        )
    }
}