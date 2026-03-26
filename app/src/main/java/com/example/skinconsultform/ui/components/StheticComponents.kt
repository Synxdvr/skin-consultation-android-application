package com.example.skinconsultform.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.example.skinconsultform.ui.theme.*

// ── Step progress bar ─────────────────────────────────────────────────
@Composable
fun StheticStepProgressBar(
    currentStep: Int,
    totalSteps: Int = 5,
    stepLabels: List<String> = listOf(
        "Personal", "Medical", "Skin", "Lifestyle", "Consent"
    ),
    modifier: Modifier = Modifier
) {
    val extended = StheticTheme.extendedColors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        stepLabels.forEachIndexed { index, label ->
            val step = index + 1
            val isComplete = step < currentStep
            val isActive = step == currentStep

            val circleColor by animateColorAsState(
                targetValue = when {
                    isComplete -> extended.stepComplete
                    isActive   -> extended.stepActive
                    else       -> extended.stepInactive
                },
                animationSpec = tween(300),
                label = "stepColor"
            )

            val textColor = when {
                isComplete || isActive -> StheticColors.Charcoal900
                else                   -> StheticColors.Charcoal300
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(circleColor)
                ) {
                    if (isComplete) {
                        Text(
                            text = "✓",
                            style = MaterialTheme.typography.labelLarge,
                            color = StheticColors.White
                        )
                    } else {
                        Text(
                            text = step.toString(),
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isActive) StheticColors.White
                            else StheticColors.Charcoal500
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor,
                    textAlign = TextAlign.Center
                )
            }

            // Connector line between steps
            if (index < totalSteps - 1) {
                Box(
                    modifier = Modifier
                        .weight(0.5f)
                        .height(1.dp)
                        .background(
                            if (step < currentStep) extended.stepComplete
                            else extended.stepInactive
                        )
                )
            }
        }
    }
}

// ── Gold divider ──────────────────────────────────────────────────────
@Composable
fun GoldDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp
) {
    HorizontalDivider(
        modifier = modifier,
        thickness = thickness,
        color = StheticColors.Gold300
    )
}

// ── Section header (serif) ────────────────────────────────────────────
@Composable
fun StheticSectionHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = StheticColors.Charcoal900
        )
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = StheticColors.Charcoal500
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        GoldDivider()
    }
}

// ── Touch-friendly text field ─────────────────────────────────────────
@Composable
fun StheticTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else 4,
    isRequired: Boolean = false,
    isError: Boolean = false
) {
    Column(modifier = modifier) {
        Text(
            text = if (isRequired) "$label *" else label,
            style = MaterialTheme.typography.labelLarge,
            color = StheticColors.Charcoal700,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyLarge,
                    color = StheticColors.Charcoal300
                )
            },
            textStyle = MaterialTheme.typography.bodyLarge,
            singleLine = singleLine,
            minLines = minLines,
            maxLines = maxLines,
            isError = isError,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = StheticColors.Gold500,
                unfocusedBorderColor = StheticColors.Cream300,
                focusedContainerColor   = StheticColors.Cream50,
                unfocusedContainerColor = StheticColors.Cream200,
                cursorColor          = StheticColors.Gold500
            ),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)     // touch-friendly minimum
        )
    }
}

@Composable
fun StheticValidatedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else 4,
    isRequired: Boolean = false,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    Column(modifier = modifier) {
        Text(
            text = if (isRequired) "$label *" else label,
            style = MaterialTheme.typography.labelLarge,
            color = if (isError) StheticColors.Error
            else StheticColors.Charcoal700,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyLarge,
                    color = StheticColors.Charcoal300
                )
            },
            textStyle = MaterialTheme.typography.bodyLarge,
            singleLine = singleLine,
            minLines = minLines,
            maxLines = maxLines,
            isError = isError,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor      = if (isError) StheticColors.Error
                else StheticColors.Gold500,
                unfocusedBorderColor    = if (isError) StheticColors.Error
                else StheticColors.Cream300,
                focusedContainerColor   = StheticColors.Cream50,
                unfocusedContainerColor = StheticColors.Cream200,
                cursorColor             = StheticColors.Gold500,
                errorBorderColor        = StheticColors.Error,
                errorContainerColor     = StheticColors.ErrorLight
            ),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
        )
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.labelSmall,
                color = StheticColors.Error,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }
}

// ── Yes / No radio row ────────────────────────────────────────────────
@Composable
fun StheticYesNoRow(
    question: String,
    value: Boolean,
    onValueChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    warningIfYes: Boolean = false
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = question,
            style = MaterialTheme.typography.bodyLarge,
            color = StheticColors.Charcoal900,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            listOf(true to "Yes", false to "No").forEach { (btnValue, label) ->
                val isSelected = value == btnValue
                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        if (warningIfYes && btnValue) StheticColors.Warning
                        else StheticColors.Gold500
                    } else StheticColors.Cream200,
                    animationSpec = tween(200),
                    label = "yesNoColor"
                )
                OutlinedButton(
                    onClick = { onValueChange(btnValue) },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) Color.Transparent
                        else StheticColors.Cream300
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = bgColor,
                        contentColor = if (isSelected) StheticColors.White
                        else StheticColors.Charcoal700
                    ),
                    modifier = Modifier.size(width = 90.dp, height = 52.dp)
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
        }
    }
}

// ── Selectable chip (multi-select) ────────────────────────────────────
@Composable
fun StheticChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extended = StheticTheme.extendedColors

    val bgColor by animateColorAsState(
        targetValue = if (isSelected) extended.chipSelected
        else extended.chipUnselected,
        animationSpec = tween(200),
        label = "chipBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) extended.chipSelectedText
        else extended.chipUnselectedText,
        animationSpec = tween(200),
        label = "chipText"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(50.dp))
            .background(bgColor)
            .border(
                width = 1.dp,
                color = if (isSelected) StheticColors.Gold500
                else StheticColors.Cream300,
                shape = RoundedCornerShape(50.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor
        )
    }
}

// ── Toggle checkbox row ───────────────────────────────────────────────
@Composable
fun StheticToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onCheckedChange(!checked) }
            .background(
                if (checked) StheticColors.Gold50
                else StheticColors.Cream100
            )
            .border(
                width = 1.dp,
                color = if (checked) StheticColors.Gold300
                else StheticColors.Cream300,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = StheticColors.Charcoal900
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = StheticColors.Charcoal500,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        Checkbox(
            checked = checked,
            onCheckedChange = null,     // handled by row click
            colors = CheckboxDefaults.colors(
                checkedColor   = StheticColors.Gold500,
                uncheckedColor = StheticColors.Charcoal300,
                checkmarkColor = StheticColors.White
            )
        )
    }
}

// ── Primary CTA button ────────────────────────────────────────────────
@Composable
fun StheticPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor         = StheticColors.Gold500,
            contentColor           = StheticColors.White,
            disabledContainerColor = StheticColors.Cream300,
            disabledContentColor   = StheticColors.Charcoal300
        ),
        modifier = modifier
            .heightIn(min = 60.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = StheticColors.White,
                strokeWidth = 2.dp,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Text(
                text = text.uppercase(),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

// ── Secondary / ghost button ──────────────────────────────────────────
@Composable
fun StheticSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, StheticColors.Gold500),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor           = StheticColors.Gold700,
            disabledContentColor   = StheticColors.Charcoal300
        ),
        modifier = modifier
            .heightIn(min = 60.dp)
            .widthIn(min = 140.dp)
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelLarge
        )
    }
}

// ── Touch slider with label ───────────────────────────────────────────
@Composable
fun StheticLabeledSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
    displayValue: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = StheticColors.Charcoal700
            )
            Text(
                text = displayValue,
                style = MaterialTheme.typography.titleMedium,
                color = StheticColors.Gold700
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor           = StheticColors.Gold500,
                activeTrackColor     = StheticColors.Gold500,
                inactiveTrackColor   = StheticColors.Cream300,
                activeTickColor      = StheticColors.Gold300,
                inactiveTickColor    = StheticColors.Cream300
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)    // touch-friendly track height
        )
    }
}

// ── Warning banner ────────────────────────────────────────────────────
@Composable
fun StheticWarningBanner(
    message: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(StheticColors.WarningLight)
            .border(
                1.dp,
                StheticColors.Warning.copy(alpha = 0.4f),
                RoundedCornerShape(10.dp)
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "⚠",
            style = MaterialTheme.typography.titleMedium,
            color = StheticColors.Warning
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = StheticColors.Warning
        )
    }
}