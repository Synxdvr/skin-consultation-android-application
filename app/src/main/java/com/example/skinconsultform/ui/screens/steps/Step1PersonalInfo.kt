package com.example.skinconsultform.ui.screens.steps

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.skinconsultform.domain.model.ConsultationForm
import com.example.skinconsultform.domain.validation.FormValidators
import com.example.skinconsultform.ui.components.*
import com.example.skinconsultform.ui.theme.StheticColors
import java.time.*
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step1PersonalInfo(
    form: ConsultationForm,
    onUpdate: (String, Any) -> Unit
) {
    val scrollState = rememberScrollState()

    val genderOptions = listOf("Female", "Male", "Non-binary", "Prefer not to say")
    val sourceOptions = listOf(
        "Walk-in", "Instagram", "Facebook",
        "Friend / Referral", "Google", "Other"
    )

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    // ── Touched state — only show errors after user interacts ─────────
    var nameTouched  by remember { mutableStateOf(false) }
    var phoneTouched by remember { mutableStateOf(false) }
    var emailTouched by remember { mutableStateOf(false) }

    // ── Validation error messages ─────────────────────────────────────
    val nameError  = if (nameTouched)
        FormValidators.nameErrorMessage(form.clientName) else null
    val phoneError = if (phoneTouched)
        FormValidators.phoneErrorMessage(form.phone) else null
    val emailError = if (emailTouched)
        FormValidators.emailErrorMessage(form.email) else null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 32.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        StheticSectionHeader(
            title = "Personal Information",
            subtitle = "Please fill in your details below"
        )

        // ── Name ──────────────────────────────────────────────────────
        StheticValidatedTextField(
            value = form.clientName,
            onValueChange = {
                val filtered = it.filter { c ->
                    c.isLetter() || c == ' ' || c == '-' || c == '\''
                }
                if (filtered.length <= 100) {
                    nameTouched = true
                    onUpdate("clientName", filtered)
                }
            },
            label = "Full Name",
            placeholder = "e.g. Maria Santos",
            isRequired = true,
            isError = nameError != null,
            errorMessage = nameError
        )

        // ── Date of birth + Age ───────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Date of Birth *",
                    style = MaterialTheme.typography.labelLarge,
                    color = StheticColors.Charcoal700,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = StheticColors.Cream200,
                        contentColor   = if (form.dateOfBirth.isBlank())
                            StheticColors.Charcoal300
                        else StheticColors.Charcoal900
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = form.dateOfBirth.ifBlank { "Select date" },
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            StheticTextField(
                value = if (form.age > 0) form.age.toString() else "",
                onValueChange = {},
                label = "Age",
                placeholder = "",
                modifier = Modifier.weight(0.4f),
                keyboardType = KeyboardType.Number
            )
        }

        // ── Date picker dialog ────────────────────────────────────────
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = Instant
                                .ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            val formatted = date.format(
                                DateTimeFormatter.ofPattern("dd MMM yyyy")
                            )
                            val calculatedAge = Period.between(
                                date,
                                LocalDate.now()
                            ).years
                            onUpdate("dateOfBirth", formatted)
                            onUpdate("age", calculatedAge)
                        }
                        showDatePicker = false
                    }) { Text("Confirm") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        // ── Gender ────────────────────────────────────────────────────
        Column {
            Text(
                text = "Gender",
                style = MaterialTheme.typography.labelLarge,
                color = StheticColors.Charcoal700,
                modifier = Modifier.padding(bottom = 10.dp)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                genderOptions.forEach { option ->
                    StheticChip(
                        label = option,
                        isSelected = form.gender == option,
                        onClick = { onUpdate("gender", option) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // ── Phone ─────────────────────────────────────────────────────────
        Column {
            Text(
                text = "Phone *",
                style = MaterialTheme.typography.labelLarge,
                color = if (phoneError != null) StheticColors.Error
                else StheticColors.Charcoal700,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Fixed +63 prefix box
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(StheticColors.Gold50)
                        .border(
                            1.dp,
                            if (phoneError != null) StheticColors.Error
                            else StheticColors.Gold300,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "+63",
                        style = MaterialTheme.typography.bodyLarge,
                        color = StheticColors.Gold700
                    )
                }

                // 10 digit input only
                OutlinedTextField(
                    value = form.phone,
                    onValueChange = { input ->
                        val digitsOnly = input.filter { it.isDigit() }
                        if (digitsOnly.length <= 10) {
                            phoneTouched = true
                            onUpdate("phone", digitsOnly)
                        }
                    },
                    placeholder = {
                        Text(
                            text = "9XX XXX XXXX",
                            style = MaterialTheme.typography.bodyLarge,
                            color = StheticColors.Charcoal300
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    singleLine = true,
                    isError = phoneError != null,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor      = if (phoneError != null)
                            StheticColors.Error else StheticColors.Gold500,
                        unfocusedBorderColor    = if (phoneError != null)
                            StheticColors.Error else StheticColors.Cream300,
                        focusedContainerColor   = StheticColors.Cream50,
                        unfocusedContainerColor = StheticColors.Cream200,
                        cursorColor             = StheticColors.Gold500,
                        errorContainerColor     = StheticColors.ErrorLight
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                )
            }
            if (phoneError != null) {
                Text(
                    text = phoneError,
                    style = MaterialTheme.typography.labelSmall,
                    color = StheticColors.Error,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }
            Text(
                text = "${form.phone.filter { it.isDigit() }.length}/10",
                style = MaterialTheme.typography.labelSmall,
                color = if (phoneError != null) StheticColors.Error
                else StheticColors.Charcoal300,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.End)
                    .padding(top = 4.dp)
            )
        }

        // ── Email ─────────────────────────────────────────────────────────
        StheticValidatedTextField(
            value = form.email,
            onValueChange = { input ->
                if (input.length <= 254) {
                    emailTouched = true
                    onUpdate("email", input)
                }
            },
            label = "Email",
            placeholder = "name@email.com",
            keyboardType = KeyboardType.Email,
            isError = emailError != null,
            errorMessage = emailError
        )

        // ── Address ───────────────────────────────────────────────────
        StheticTextField(
            value = form.address,
            onValueChange = { onUpdate("address", it) },
            label = "Address",
            placeholder = "Street, City, Province",
            singleLine = false,
            minLines = 2,
            maxLines = 3
        )

        // ── Occupation ────────────────────────────────────────────────
        StheticTextField(
            value = form.occupation,
            onValueChange = { onUpdate("occupation", it) },
            label = "Occupation",
            placeholder = "e.g. Teacher, Nurse, Business Owner"
        )

        // ── Source ────────────────────────────────────────────────────
        Column {
            Text(
                text = "How did you hear about us?",
                style = MaterialTheme.typography.labelLarge,
                color = StheticColors.Charcoal700,
                modifier = Modifier.padding(bottom = 10.dp)
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                sourceOptions.forEach { option ->
                    StheticChip(
                        label = option,
                        isSelected = form.source == option,
                        onClick = { onUpdate("source", option) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp)) // bottom nav clearance
    }
}