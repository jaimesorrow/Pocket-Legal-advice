// NativeDocumentScanner.kt
package com.pocketlegal.scanner

import android.app.Activity
import android.content.Context
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.documentscanner.GmsDocumentScanner
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_PDF
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class NativeDocumentScanner(
    private val activity: FragmentActivity,
    private val onPagesRecognized: (pageIndex: Int, rawText: String) -> Unit,
    private val onScanError: (Throwable) -> Unit
) {

    private val scannerOptions: GmsDocumentScannerOptions = GmsDocumentScannerOptions.Builder()
        .setScannerMode(SCANNER_MODE_FULL)
        .setResultFormats(RESULT_FORMAT_JPEG, RESULT_FORMAT_PDF)
        .setPageLimit(Int.MAX_VALUE)
        .build()

    private val scanner: GmsDocumentScanner =
        GmsDocumentScanning.getClient(scannerOptions)

    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    private val ioScope = CoroutineScope(Dispatchers.IO)

    val scanLauncher: ActivityResultLauncher<IntentSenderRequest> =
        activity.registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val scanningResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
                val pageUris: List<Uri> = scanningResult?.pages?.map { it.imageUri } ?: emptyList()
                processScannedPages(pageUris)
            } else {
                onScanError(IllegalStateException("Document scan cancelled or failed (resultCode=${result.resultCode})"))
            }
        }

    fun startScan() {
        scanner.getStartScanIntent(activity)
            .addOnSuccessListener { intentSender ->
                val request = IntentSenderRequest.Builder(intentSender).build()
                scanLauncher.launch(request)
            }
            .addOnFailureListener { exception ->
                onScanError(exception)
            }
    }

    private fun processScannedPages(pageUris: List<Uri>) {
        ioScope.launch {
            pageUris.forEachIndexed { index, uri ->
                val rawText = runCatching { recognizeTextFromUri(activity, uri) }
                    .getOrElse { throwable ->
                        onScanError(throwable)
                        return@forEachIndexed
                    }
                onPagesRecognized(index, rawText)
            }
        }
    }

    private suspend fun recognizeTextFromUri(context: Context, uri: Uri): String =
        suspendCancellableCoroutine { continuation ->
            val image = InputImage.fromFilePath(context, uri)
            textRecognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val rawText = visionText.text
                    continuation.resume(rawText)
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        }

    fun release() {
        textRecognizer.close()
    }
}
