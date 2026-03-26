package com.example.skinconsultform.ui.screens

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.skinconsultform.ui.components.*
import com.example.skinconsultform.ui.navigation.Screen
import com.example.skinconsultform.ui.screens.steps.*
import com.example.skinconsultform.ui.theme.StheticColors
import com.example.skinconsultform.ui.viewmodel.*

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ConsultationScreen(
    navController: NavController,
    viewModel: ConsultationViewModel = hiltViewModel()
) {
    val form        by viewModel.form.collectAsState()
    val currentStep by viewModel.currentStep.collectAsState()
    val submitState by viewModel.submitState.collectAsState()
    val canProceed by viewModel.canProceed.collectAsState()

    // Handle hardware back button
    BackHandler(enabled = currentStep > 1) {
        viewModel.previousStep()
    }

    // Navigate to results when submission succeeds
    LaunchedEffect(submitState) {
        if (submitState is SubmitState.Success) {
            val id = (submitState as SubmitState.Success).consultationId
            navController.navigate(Screen.Results.createRoute(id)) {
                popUpTo(Screen.Consultation.route) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StheticColors.Cream50)
    ) {
        // ── Top bar ───────────────────────────────────────────────────
        Surface(
            color = StheticColors.White,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "S'thetic",
                        style = MaterialTheme.typography.titleLarge,
                        color = StheticColors.Gold700
                    )
                    Text(
                        text = "Step $currentStep of 5",
                        style = MaterialTheme.typography.bodyMedium,
                        color = StheticColors.Charcoal500
                    )
                }
                StheticStepProgressBar(currentStep = currentStep)
            }
        }

        // ── Animated step content ─────────────────────────────────────
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                val direction = if (targetState > initialState) 1 else -1
                slideInHorizontally(
                    tween(350),
                    initialOffsetX = { it * direction }
                ) + fadeIn(tween(350)) togetherWith
                        slideOutHorizontally(
                            tween(350),
                            targetOffsetX = { -it * direction }
                        ) + fadeOut(tween(350))
            },
            label = "stepTransition",
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { step ->
            when (step) {
                1 -> Step1PersonalInfo(
                    form = form,
                    onUpdate = { field, value ->
                        viewModel.updateStep1Field(field, value)
                    }
                )
                2 -> Step2MedicalHistory(
                    form = form,
                    onUpdate = { field, value ->
                        viewModel.updateStep2Field(field, value)
                    }
                )
                3 -> Step3SkinHistory(
                    form = form,
                    onUpdate = { field, value ->
                        viewModel.updateStep3Field(field, value)
                    }
                )
                4 -> Step4Lifestyle(
                    form = form,
                    onUpdate = { field, value ->
                        viewModel.updateStep4Field(field, value)
                    }
                )
                5 -> Step5Consent(
                    form = form,
                    viewModel = viewModel,
                    isLoading = submitState is SubmitState.Loading
                )
            }
        }

        // ── Bottom navigation bar ─────────────────────────────────────
        Surface(
            color = StheticColors.White,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep > 1) {
                    StheticSecondaryButton(
                        text = "← Back",
                        onClick = { viewModel.previousStep() },
                        modifier = Modifier.width(160.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.width(160.dp))
                }

                if (currentStep < 5) {
                    StheticPrimaryButton(
                        text = "Continue →",
                        onClick = { viewModel.nextStep() },
                        enabled = canProceed,
                        modifier = Modifier.width(200.dp)
                    )
                }
                // Step 5 submit button is inside Step5Consent
            }
        }
    }
}