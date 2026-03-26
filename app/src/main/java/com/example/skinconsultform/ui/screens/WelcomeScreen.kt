package com.example.skinconsultform.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.example.skinconsultform.R
import com.example.skinconsultform.ui.components.*
import com.example.skinconsultform.ui.navigation.Screen
import com.example.skinconsultform.ui.theme.StheticColors
import kotlinx.coroutines.delay

@Composable
fun WelcomeScreen(navController: NavController) {

    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300)
        visible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StheticColors.Cream50),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(1000)) + slideInVertically(
                tween(1000),
                initialOffsetY = { it / 8 }
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(0.dp),
                modifier = Modifier
                    .fillMaxWidth(0.72f)
                    .padding(vertical = 32.dp)
            ) {
                // ── Logo ──────────────────────────────────────────────
                Image(
                    painter = painterResource(id = R.drawable.welcome_logo),
                    contentDescription = "S'thetic Logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(220.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // ── Gold divider ──────────────────────────────────────
                GoldDivider(modifier = Modifier.width(140.dp))

                Spacer(modifier = Modifier.height(24.dp))

                // ── Tagline ───────────────────────────────────────────
                Text(
                    text = "WHERE BEAUTY BEGINS",
                    style = MaterialTheme.typography.labelLarge.copy(
                        letterSpacing = 4.sp,
                        fontSize = 13.sp
                    ),
                    color = StheticColors.Charcoal500,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // ── Title ─────────────────────────────────────────────
                Text(
                    text = "Skin Consultation",
                    style = MaterialTheme.typography.headlineLarge,
                    color = StheticColors.Charcoal900,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ── Description ───────────────────────────────────────
                Text(
                    text = "Please complete this short form so our\nspecialist can prepare your personalised\ntreatment plan.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 18.sp,
                        lineHeight = 30.sp
                    ),
                    color = StheticColors.Charcoal500,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(40.dp))

                // ── Begin button ──────────────────────────────────────
                StheticPrimaryButton(
                    text = "Begin Consultation",
                    onClick = {
                        navController.navigate(Screen.Consultation.route)
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(68.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ── Staff admin link ──────────────────────────────────
                TextButton(
                    onClick = { navController.navigate(Screen.Admin.route) }
                ) {
                    Text(
                        text = "Staff Admin",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 13.sp
                        ),
                        color = StheticColors.Charcoal300
                    )
                }
            }
        }
    }
}