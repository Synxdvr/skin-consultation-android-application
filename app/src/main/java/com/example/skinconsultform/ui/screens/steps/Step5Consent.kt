package com.example.skinconsultform.ui.screens.steps

import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.skinconsultform.domain.model.ConsultationForm
import com.example.skinconsultform.ui.components.*
import com.example.skinconsultform.ui.theme.StheticColors
import com.example.skinconsultform.ui.viewmodel.ConsultationViewModel
import java.io.ByteArrayOutputStream
import java.util.Base64
import kotlin.math.roundToInt
import androidx.core.graphics.createBitmap

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Step5Consent(
    form: ConsultationForm,
    viewModel: ConsultationViewModel,
    isLoading: Boolean
) {
    val scrollState = rememberScrollState()

    // Signature state
    val paths = remember { mutableStateListOf<Pair<Path, Float>>() }
    var currentPath by remember { mutableStateOf(Path()) }
    var lastPoint by remember { mutableStateOf<Offset?>(null) }
    var canvasSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }

    val allConsentsChecked = form.consentAccurate &&
            form.consentNotMedical && form.consentTreatment

    val canSubmit = allConsentsChecked && form.signatureBase64.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 32.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StheticSectionHeader(
            title = "Consent & Acknowledgement",
            subtitle = "Please read and agree to the following"
        )

        // ── Consent toggles ───────────────────────────────────────────
        StheticToggleRow(
            label = "I confirm my information is accurate",
            subtitle = "The information provided is accurate and complete " +
                    "to the best of my knowledge.",
            checked = form.consentAccurate,
            onCheckedChange = { viewModel.updateStep5(consentAccurate = it) }
        )

        StheticToggleRow(
            label = "I understand this is not a medical diagnosis",
            subtitle = "I agree to follow pre- and post-care guidelines " +
                    "as advised by the consultant.",
            checked = form.consentNotMedical,
            onCheckedChange = { viewModel.updateStep5(consentNotMedical = it) }
        )

        StheticToggleRow(
            label = "I consent to the proposed treatments",
            subtitle = "I understand the potential risks, side effects, " +
                    "and expected results of aesthetic treatments.",
            checked = form.consentTreatment,
            onCheckedChange = { viewModel.updateStep5(consentTreatment = it) }
        )

        GoldDivider(modifier = Modifier.padding(vertical = 4.dp))

        // ── Consultant name ───────────────────────────────────────────
        StheticTextField(
            value = form.consultantName,
            onValueChange = { viewModel.updateStep5(consultantName = it) },
            label = "Consultant Name (staff)",
            placeholder = "Enter consultant's name"
        )

        // ── Signature canvas ──────────────────────────────────────────
        Text(
            text = "Client Signature *",
            style = MaterialTheme.typography.labelLarge,
            color = StheticColors.Charcoal700
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(StheticColors.White)
                .border(
                    width = if (form.signatureBase64.isNotBlank()) 2.dp else 1.dp,
                    color = if (form.signatureBase64.isNotBlank())
                        StheticColors.Gold500 else StheticColors.Cream300,
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                currentPath = Path().apply { moveTo(offset.x, offset.y) }
                                lastPoint = offset
                            },
                            onDrag = { change, _ ->
                                val newPoint = change.position
                                lastPoint?.let { last ->
                                    // Smooth quadratic bezier
                                    val mid = Offset(
                                        (last.x + newPoint.x) / 2f,
                                        (last.y + newPoint.y) / 2f
                                    )
                                    currentPath.quadraticTo(
                                        last.x, last.y,
                                        mid.x, mid.y
                                    )
                                }
                                lastPoint = newPoint
                                paths.add(Pair(Path().apply { addPath(currentPath) }, 2f))
                            },
                            onDragEnd = {
                                // Save signature as base64 bitmap
                                val bitmap = createBitmap(
                                    canvasSize.width.roundToInt().coerceAtLeast(1),
                                    canvasSize.height.roundToInt().coerceAtLeast(1)
                                )
                                val canvas = android.graphics.Canvas(bitmap)
                                canvas.drawColor(android.graphics.Color.WHITE)
                                val paint = android.graphics.Paint().apply {
                                    color = android.graphics.Color.BLACK
                                    strokeWidth = 4f
                                    style = android.graphics.Paint.Style.STROKE
                                    isAntiAlias = true
                                    strokeCap = android.graphics.Paint.Cap.ROUND
                                    strokeJoin = android.graphics.Paint.Join.ROUND
                                }
                                paths.forEach { (path, _) ->
                                    canvas.drawPath(path.asAndroidPath(), paint)
                                }
                                val stream = ByteArrayOutputStream()
                                bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
                                val encoded = Base64.getEncoder()
                                    .encodeToString(stream.toByteArray())
                                viewModel.updateStep5(signatureBase64 = encoded)
                                lastPoint = null
                            }
                        )
                    }
            ) {
                canvasSize = size

                // Draw all captured paths
                paths.forEach { (path, strokeWidth) ->
                    drawPath(
                        path = path,
                        color = StheticColors.Charcoal900,
                        style = Stroke(
                            width = strokeWidth * 2,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }

                // Placeholder line
                if (paths.isEmpty()) {
                    drawLine(
                        color = StheticColors.Cream300,
                        start = Offset(40f, size.height * 0.75f),
                        end = Offset(size.width - 40f, size.height * 0.75f),
                        strokeWidth = 1f
                    )
                }
            }

            // Placeholder text
            if (paths.isEmpty()) {
                Text(
                    text = "Sign here",
                    style = MaterialTheme.typography.bodySmall,
                    color = StheticColors.Charcoal300,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 44.dp, bottom = 28.dp)
                )
            }
        }

        // Clear signature button
        if (paths.isNotEmpty()) {
            TextButton(
                onClick = {
                    paths.clear()
                    viewModel.updateStep5(signatureBase64 = "")
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    text = "Clear signature",
                    style = MaterialTheme.typography.labelMedium,
                    color = StheticColors.Error
                )
            }
        }

        // ── Date (auto) ───────────────────────────────────────────────
        Text(
            text = "Date: ${java.time.LocalDate.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd MMMM yyyy"))}",
            style = MaterialTheme.typography.bodyMedium,
            color = StheticColors.Charcoal500
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ── Submit button ─────────────────────────────────────────────
        StheticPrimaryButton(
            text = "Submit & Get Recommendations",
            onClick = { viewModel.triggerAiRecommendations() },
            enabled = canSubmit,
            isLoading = isLoading,
            modifier = Modifier.fillMaxWidth()
        )

        if (!allConsentsChecked) {
            Text(
                text = "Please agree to all consent items above to proceed.",
                style = MaterialTheme.typography.bodySmall,
                color = StheticColors.Error,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}