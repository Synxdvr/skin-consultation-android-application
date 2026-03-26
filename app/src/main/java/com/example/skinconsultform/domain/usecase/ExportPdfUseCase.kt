// domain/usecase/ExportPdfUseCase.kt
package com.example.skinconsultform.domain.usecase

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.skinconsultform.data.db.ConsultationEntity
import com.example.skinconsultform.domain.pdf.PdfGenerator
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExportPdfUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pdfGenerator: PdfGenerator
) {
    operator fun invoke(entity: ConsultationEntity): Intent {
        val pdfFile = pdfGenerator.generateConsultationPdf(entity)
        val uri     = getFileUri(pdfFile)

        return Intent(Intent.ACTION_SEND).apply {
            type     = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(
                Intent.EXTRA_SUBJECT,
                "S'thetic Consultation — ${entity.clientName}"
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    fun getFileUri(file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    }
}