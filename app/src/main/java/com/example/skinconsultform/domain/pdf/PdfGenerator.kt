package com.example.skinconsultform.domain.pdf

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import com.example.skinconsultform.data.db.ConsultationEntity
import com.example.skinconsultform.domain.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.*
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.*
import com.itextpdf.layout.properties.*
import com.itextpdf.layout.borders.SolidBorder
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.*

@Singleton
class PdfGenerator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {
    // ── Brand colors ──────────────────────────────────────────────────
    private val goldColor    = DeviceRgb(212, 175, 55)   // Gold500
    private val creamColor   = DeviceRgb(250, 246, 238)  // Cream100
    private val charcoalColor = DeviceRgb(26, 22, 18)    // Charcoal900
    private val mutedColor   = DeviceRgb(107, 95, 87)    // Charcoal500
    private val lightGold    = DeviceRgb(247, 237, 202)  // Gold100
    private val warningColor = DeviceRgb(181, 130, 10)   // Warning
    private val whiteColor   = DeviceRgb(255, 255, 255)

    fun generateConsultationPdf(entity: ConsultationEntity): File {
        val fileName = "sthetic_consultation_${entity.clientName
            .replace(" ", "_")}_${System.currentTimeMillis()}.pdf"
        val outputFile = File(context.cacheDir, fileName)

        val writer   = PdfWriter(outputFile)
        val pdfDoc   = PdfDocument(writer)
        val document = Document(pdfDoc, PageSize.A4)

        document.setMargins(40f, 50f, 40f, 50f)

        // ── Header ────────────────────────────────────────────────────
        addHeader(document)

        // ── Client info ───────────────────────────────────────────────
        addSectionTitle(document, "Client Information")
        addInfoTable(
            document,
            listOf(
                "Full name"    to entity.clientName,
                "Date of birth" to entity.dateOfBirth,
                "Age"          to "${entity.age} years old",
                "Gender"       to entity.gender,
                "Phone"        to entity.phone,
                "Email"        to entity.email.ifBlank { "—" },
                "Address"      to entity.address.ifBlank { "—" },
                "Occupation"   to entity.occupation.ifBlank { "—" },
                "Source"       to entity.source.ifBlank { "—" }
            )
        )

        // ── Medical history ───────────────────────────────────────────
        val conditions = parseJsonList(entity.medicalConditions)
        addSectionTitle(document, "Medical History")

        if (conditions.isEmpty() &&
            !entity.isPregnantOrBreastfeeding &&
            entity.medications.isBlank()
        ) {
            addBodyText(document, "No significant medical history reported.")
        } else {
            val medicalRows = mutableListOf<Pair<String, String>>()
            conditions.forEach { medicalRows.add("Condition" to it) }
            if (entity.isPregnantOrBreastfeeding)
                medicalRows.add("Note" to "Pregnant / Breastfeeding")
            if (entity.medications.isNotBlank())
                medicalRows.add("Medications" to entity.medications)
            if (entity.allergyDetails.isNotBlank())
                medicalRows.add("Allergies" to entity.allergyDetails)
            if (entity.cancerDetails.isNotBlank())
                medicalRows.add("Cancer history" to entity.cancerDetails)
            addInfoTable(document, medicalRows)
        }

        // ── Skin history ──────────────────────────────────────────────
        val skinConcerns      = parseJsonList(entity.skinConcerns)
        val previousTreatments = parseJsonList(entity.previousTreatments)

        addSectionTitle(document, "Skin History & Concerns")
        addInfoTable(
            document,
            listOf(
                "Main concerns" to skinConcerns
                    .joinToString(", ").ifBlank { "None specified" },
                "Previous treatments" to previousTreatments
                    .joinToString(", ").ifBlank { "None" },
                "Last treatment date" to entity.lastTreatmentDate
                    .ifBlank { "—" },
                "Diagnosed condition" to entity.diagnosedSkinCondition
                    .ifBlank { "None" },
                "Cleanser" to entity.cleanser.ifBlank { "Not specified" },
                "Moisturiser" to entity.moisturizer.ifBlank { "Not specified" },
                "SPF" to entity.spfProduct.ifBlank { "Not specified" },
                "Other products" to entity.otherProducts.ifBlank { "None" }
            )
        )

        // ── Lifestyle ─────────────────────────────────────────────────
        addSectionTitle(document, "Lifestyle & Habits")
        addInfoTable(
            document,
            listOf(
                "Smokes"         to if (entity.smokes) "Yes" else "No",
                "Alcohol"        to if (entity.drinksAlcohol) "Yes" else "No",
                "Sleep"          to "${entity.sleepHours} hours/night",
                "Water intake"   to "${entity.waterLiters} L/day",
                "High stress"    to if (entity.highStress) "Yes" else "No",
                "Sun exposure"   to if (entity.sunExposure) "Yes" else "No",
                "Wears SPF daily" to if (entity.wearsSpfDaily) "Yes" else "No"
            )
        )

        // ── AI Recommendations ────────────────────────────────────────
        val recommendations = parseRecommendations(entity.aiRecommendations)
        if (recommendations.isNotEmpty()) {
            addSectionTitle(document, "Treatment Recommendations")

            // AI narrative paragraph
            if (entity.aiRecommendations.isNotBlank()) {
                addNarrativeBox(document, entity)
            }

            // Active recommendations
            val active = recommendations.filter { !it.isContraindicated }
            val contraindicated = recommendations.filter { it.isContraindicated }

            active.forEachIndexed { index, rec ->
                addRecommendationRow(document, rec, index + 1)
            }

            // Contraindicated — shown as caution
            if (contraindicated.isNotEmpty()) {
                addSectionTitle(document, "Treatments Requiring Specialist Review")
                contraindicated.forEach { rec ->
                    addCautionRow(document, rec)
                }
            }
        }

        // ── Consent ───────────────────────────────────────────────────
        addSectionTitle(document, "Consent & Acknowledgement")
        addConsentSection(document, entity)

        // ── Signature ─────────────────────────────────────────────────
        if (entity.signatureBase64.isNotBlank()) {
            addSignatureSection(document, entity)
        }

        // ── Footer ────────────────────────────────────────────────────
        addFooter(document, entity)

        document.close()
        return outputFile
    }

    // ── Header ────────────────────────────────────────────────────────
    private fun addHeader(document: Document) {
        // Gold top bar
        val headerTable = Table(UnitValue.createPercentArray(floatArrayOf(1f)))
            .useAllAvailableWidth()
            .setBackgroundColor(goldColor)
            .setMarginBottom(4f)

        val headerCell = Cell()
            .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
            .setPadding(20f)

        headerCell.add(
            Paragraph("S'thetic")
                .setFontSize(28f)
                .setFontColor(whiteColor)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
        )
        headerCell.add(
            Paragraph("WHERE BEAUTY BEGINS")
                .setFontSize(9f)
                .setFontColor(DeviceRgb(255, 245, 200))
                .setCharacterSpacing(3f)
                .setTextAlignment(TextAlignment.CENTER)
        )

        headerTable.addCell(headerCell)
        document.add(headerTable)

        // Consultation title
        document.add(
            Paragraph("Skin Consultation Form")
                .setFontSize(16f)
                .setFontColor(charcoalColor)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(16f)
                .setMarginBottom(4f)
        )

        // Date line
        val dateStr = SimpleDateFormat(
            "dd MMMM yyyy",
            Locale.getDefault()
        ).format(Date())
        document.add(
            Paragraph("Date: $dateStr")
                .setFontSize(10f)
                .setFontColor(mutedColor)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20f)
        )

        addGoldLine(document)
    }

    // ── Section title ─────────────────────────────────────────────────
    private fun addSectionTitle(document: Document, title: String) {
        document.add(
            Paragraph(title.uppercase())
                .setFontSize(10f)
                .setFontColor(goldColor)
                .setBold()
                .setCharacterSpacing(1.5f)
                .setMarginTop(20f)
                .setMarginBottom(6f)
        )
        addGoldLine(document)
        document.add(Paragraph("").setMarginBottom(6f))
    }

    // ── Gold divider line ─────────────────────────────────────────────
    private fun addGoldLine(document: Document) {
        val lineTable = Table(UnitValue.createPercentArray(floatArrayOf(1f)))
            .useAllAvailableWidth()
            .setHeight(1f)
            .setBackgroundColor(goldColor)
        lineTable.addCell(
            Cell()
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                .setHeight(1f)
        )
        document.add(lineTable)
    }

    // ── Two-column info table ─────────────────────────────────────────
    private fun addInfoTable(
        document: Document,
        rows: List<Pair<String, String>>
    ) {
        val borderColor = DeviceRgb(232, 223, 192)
        val border = SolidBorder(borderColor, 0.5f)

        val table = Table(
            UnitValue.createPercentArray(floatArrayOf(35f, 65f))
        ).useAllAvailableWidth()
            .setMarginBottom(8f)

        rows.forEach { (label, value) ->
            // Label cell
            table.addCell(
                Cell()
                    .add(
                        Paragraph(label)
                            .setFontSize(9f)
                            .setFontColor(mutedColor)
                            .setBold()
                    )
                    .setBackgroundColor(creamColor)
                    .setBorder(border)
                    .setPaddingTop(8f)
                    .setPaddingBottom(8f)
                    .setPaddingLeft(10f)
                    .setPaddingRight(6f)
            )
            // Value cell
            table.addCell(
                Cell()
                    .add(
                        Paragraph(value.ifBlank { "—" })
                            .setFontSize(10f)
                            .setFontColor(charcoalColor)
                    )
                    .setBackgroundColor(whiteColor)
                    .setBorder(border)
                    .setPaddingTop(8f)
                    .setPaddingBottom(8f)
                    .setPaddingLeft(10f)
                    .setPaddingRight(10f)
            )
        }
        document.add(table)
    }

    // ── Plain body text ───────────────────────────────────────────────
    private fun addBodyText(document: Document, text: String) {
        document.add(
            Paragraph(text)
                .setFontSize(10f)
                .setFontColor(mutedColor)
                .setMarginBottom(8f)
        )
    }

    // ── AI narrative box ──────────────────────────────────────────────
    private fun addNarrativeBox(
        document: Document,
        entity: ConsultationEntity
    ) {
        // Parse the narrative from recommendations JSON
        // For now we add a placeholder — wire in Phase 6 result
        val narrativeTable = Table(
            UnitValue.createPercentArray(floatArrayOf(1f))
        ).useAllAvailableWidth()
            .setMarginBottom(12f)

        narrativeTable.addCell(
            Cell()
                .add(
                    Paragraph("Specialist Assessment")
                        .setFontSize(9f)
                        .setFontColor(goldColor)
                        .setBold()
                        .setCharacterSpacing(1f)
                        .setMarginBottom(6f)
                )
                .add(
                    Paragraph(
                        "Based on your consultation, our specialist has " +
                                "prepared personalised treatment recommendations " +
                                "below tailored specifically for your skin profile."
                    )
                        .setFontSize(10f)
                        .setFontColor(charcoalColor)
                        .setItalic()
                )
                .setBackgroundColor(lightGold)
                .setBorder(SolidBorder(goldColor, 1f))
                .setPadding(16f)
        )
        document.add(narrativeTable)
    }

    // ── Recommendation row ────────────────────────────────────────────
    private fun addRecommendationRow(
        document: Document,
        rec: TreatmentRecommendation,
        index: Int
    ) {
        val table = Table(
            UnitValue.createPercentArray(floatArrayOf(8f, 92f))
        ).useAllAvailableWidth()
            .setMarginBottom(8f)

        // Number badge
        table.addCell(
            Cell()
                .add(
                    Paragraph(index.toString())
                        .setFontSize(12f)
                        .setFontColor(whiteColor)
                        .setBold()
                        .setTextAlignment(TextAlignment.CENTER)
                )
                .setBackgroundColor(goldColor)
                .setVerticalAlignment(
                    VerticalAlignment.MIDDLE
                )
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                .setPadding(10f)
        )

        // Treatment content
        val categoryLabel = when (rec.category) {
            RecommendationCategory.FACIAL_TREATMENT   -> "Treatment"
            RecommendationCategory.SKIN_CONCERN       -> "Skin care"
            RecommendationCategory.LIFESTYLE_ADVISORY -> "Lifestyle"
            RecommendationCategory.PRODUCT_SUGGESTION -> "Product"
            RecommendationCategory.CAUTION            -> "Caution"
        }

        val contentCell = Cell()
            .add(
                Paragraph(rec.treatmentName)
                    .setFontSize(11f)
                    .setFontColor(charcoalColor)
                    .setBold()
                    .setMarginBottom(2f)
            )
            .add(
                Paragraph("[$categoryLabel]  ${rec.reason}")
                    .setFontSize(9f)
                    .setFontColor(mutedColor)
            )
            .setBackgroundColor(creamColor)
            .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
            .setPaddingTop(10f)
            .setPaddingBottom(10f)
            .setPaddingLeft(14f)
            .setPaddingRight(10f)

        table.addCell(contentCell)
        document.add(table)
    }

    // ── Caution row ───────────────────────────────────────────────────
    private fun addCautionRow(
        document: Document,
        rec: TreatmentRecommendation
    ) {
        val table = Table(
            UnitValue.createPercentArray(floatArrayOf(8f, 92f))
        ).useAllAvailableWidth()
            .setMarginBottom(8f)

        table.addCell(
            Cell()
                .add(
                    Paragraph("!")
                        .setFontSize(14f)
                        .setFontColor(whiteColor)
                        .setBold()
                        .setTextAlignment(TextAlignment.CENTER)
                )
                .setBackgroundColor(warningColor)
                .setVerticalAlignment(
                    VerticalAlignment.MIDDLE
                )
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                .setPadding(10f)
        )

        table.addCell(
            Cell()
                .add(
                    Paragraph(rec.treatmentName)
                        .setFontSize(11f)
                        .setFontColor(charcoalColor)
                        .setBold()
                        .setMarginBottom(2f)
                )
                .add(
                    Paragraph(rec.contraindicationNote)
                        .setFontSize(9f)
                        .setFontColor(warningColor)
                )
                .setBackgroundColor(DeviceRgb(250, 237, 196))
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                .setPaddingTop(10f)
                .setPaddingBottom(10f)
                .setPaddingLeft(14f)
                .setPaddingRight(10f)
        )

        document.add(table)
    }

    // ── Consent section ───────────────────────────────────────────────
    private fun addConsentSection(
        document: Document,
        entity: ConsultationEntity
    ) {
        val consentItems = listOf(
            entity.consentAccurate to
                    "I confirm the information provided is accurate and " +
                    "complete to the best of my knowledge.",
            entity.consentNotMedical to
                    "I acknowledge that this consultation is not a medical " +
                    "diagnosis and agree to follow pre- and post-care guidelines.",
            entity.consentTreatment to
                    "I consent to the proposed aesthetic treatments and " +
                    "understand the potential risks, side effects, and " +
                    "expected results."
        )

        consentItems.forEach { (agreed, text) ->
            val checkMark = if (agreed) "☑" else "☐"
            document.add(
                Paragraph("$checkMark  $text")
                    .setFontSize(10f)
                    .setFontColor(charcoalColor)
                    .setMarginBottom(8f)
            )
        }
    }

    // ── Signature section ─────────────────────────────────────────────
    private fun addSignatureSection(
        document: Document,
        entity: ConsultationEntity
    ) {
        addSectionTitle(document, "Signatures")

        val sigTable = Table(
            UnitValue.createPercentArray(floatArrayOf(50f, 50f))
        ).useAllAvailableWidth()
            .setMarginTop(8f)

        // Client signature
        val clientCell = Cell()
            .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
            .setPaddingRight(20f)

        try {
            val bytes  = Base64.decode(entity.signatureBase64, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            if (bitmap != null) {
                val stream = ByteArrayOutputStream()
                bitmap.compress(
                    android.graphics.Bitmap.CompressFormat.PNG,
                    100,
                    stream
                )
                val imageData = ImageDataFactory.create(stream.toByteArray())
                val sigImage  = Image(imageData)
                    .setWidth(160f)
                    .setHeight(60f)
                clientCell.add(sigImage)
            }
        } catch (e: Exception) {
            clientCell.add(
                Paragraph("  [Signature on file]  ")
                    .setFontSize(9f)
                    .setFontColor(mutedColor)
            )
        }

        // Signature line + name
        clientCell.add(
            Paragraph("_________________________________")
                .setFontSize(10f)
                .setFontColor(goldColor)
                .setMarginTop(4f)
        )
        clientCell.add(
            Paragraph(entity.clientName)
                .setFontSize(9f)
                .setFontColor(mutedColor)
        )
        clientCell.add(
            Paragraph("Client Signature")
                .setFontSize(8f)
                .setFontColor(mutedColor)
        )

        // Consultant signature
        val consultantCell = Cell()
            .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
            .setPaddingLeft(20f)

        // Space for consultant to sign on printed copy
        consultantCell.add(
            Paragraph("\n\n")
                .setFontSize(10f)
        )
        consultantCell.add(
            Paragraph("_________________________________")
                .setFontSize(10f)
                .setFontColor(goldColor)
        )
        consultantCell.add(
            Paragraph(
                entity.consultantName.ifBlank { "Consultant" }
            )
                .setFontSize(9f)
                .setFontColor(mutedColor)
        )
        consultantCell.add(
            Paragraph("Consultant Signature")
                .setFontSize(8f)
                .setFontColor(mutedColor)
        )

        // Date column
        val dateStr = SimpleDateFormat(
            "dd MMMM yyyy",
            Locale.getDefault()
        ).format(Date(entity.submittedAt))

        clientCell.add(
            Paragraph("Date: $dateStr")
                .setFontSize(9f)
                .setFontColor(mutedColor)
                .setMarginTop(6f)
        )

        sigTable.addCell(clientCell)
        sigTable.addCell(consultantCell)
        document.add(sigTable)
    }

    // ── Footer ────────────────────────────────────────────────────────
    private fun addFooter(document: Document, entity: ConsultationEntity) {
        addGoldLine(document)
        document.add(
            Paragraph(
                "S'thetic Spa  •  Consultation ID: ${entity.id}  " +
                        "•  Confidential Client Record"
            )
                .setFontSize(8f)
                .setFontColor(mutedColor)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(12f)
        )
    }

    // ── Helpers ───────────────────────────────────────────────────────
    private fun parseJsonList(json: String): List<String> {
        return try {
            gson.fromJson(
                json,
                object : TypeToken<List<String>>() {}.type
            ) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseRecommendations(
        json: String
    ): List<TreatmentRecommendation> {
        return try {
            gson.fromJson(
                json,
                object : TypeToken<List<TreatmentRecommendation>>() {}.type
            ) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}