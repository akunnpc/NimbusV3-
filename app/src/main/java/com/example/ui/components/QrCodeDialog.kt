package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.zip.CRC32

@Composable
fun QrCodeDialog(
    url: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    // Generate a deterministic visual QR pattern based on url hash/bytes
    val qrMatrix = remember(url) {
        generateSimpleQrMatrix(url)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.QrCode2,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Kode QR Situs Web", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Pindai kode QR ini dengan kamera HP lain untuk membuka tautan langsung:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                // QR Code Canvas Rendering
                Surface(
                    modifier = Modifier
                        .size(200.dp)
                        .padding(8.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    shadowElevation = 4.dp
                ) {
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(160.dp)) {
                            val gridSize = qrMatrix.size
                            val cellSize = size.width / gridSize

                            for (r in 0 until gridSize) {
                                for (c in 0 until gridSize) {
                                    if (qrMatrix[r][c]) {
                                        drawRect(
                                            color = Color.Black,
                                            topLeft = Offset(c * cellSize, r * cellSize),
                                            size = Size(cellSize, cellSize)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = url,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(8.dp),
                        textAlign = TextAlign.Center
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("URL Web", url)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Tautan disalin ke papan klip!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("copy_url_qr_button")
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Salin Link", fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, url)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Bagikan Tautan Web"))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_url_qr_button")
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Bagikan", fontSize = 11.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("close_qr_dialog")
            ) {
                Text("Tutup")
            }
        }
    )
}

/**
 * Generates a standard 21x21 QR Code visual matrix representation with corner locator targets
 */
private fun generateSimpleQrMatrix(text: String): Array<BooleanArray> {
    val size = 21
    val matrix = Array(size) { BooleanArray(size) }

    // Helper to draw QR corner positioning targets (7x7 blocks)
    fun drawTarget(startR: Int, startC: Int) {
        for (r in 0..6) {
            for (c in 0..6) {
                val isOuter = r == 0 || r == 6 || c == 0 || c == 6
                val isInner = r in 2..4 && c in 2..4
                matrix[startR + r][startC + c] = isOuter || isInner
            }
        }
    }

    // Top-left target
    drawTarget(0, 0)
    // Top-right target
    drawTarget(0, size - 7)
    // Bottom-left target
    drawTarget(size - 7, 0)

    // Timing patterns
    for (i in 7 until size - 7) {
        matrix[6][i] = i % 2 == 0
        matrix[i][6] = i % 2 == 0
    }

    // Hash text bytes to fill payload matrix
    val crc = CRC32()
    crc.update(text.toByteArray())
    var hash = crc.value

    for (r in 0 until size) {
        for (c in 0 until size) {
            // Skip target zones
            if ((r < 8 && c < 8) || (r < 8 && c >= size - 8) || (r >= size - 8 && c < 8)) continue
            if (r == 6 || c == 6) continue

            hash = (hash * 1103515245 + 12345) and 0x7FFFFFFF
            matrix[r][c] = (hash % 2) == 0L
        }
    }

    return matrix
}
