package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ClearDataDialog(
    onClearData: (clearHistory: Boolean, clearCache: Boolean, clearAlerts: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var clearHistory by remember { mutableStateOf(true) }
    var clearCache by remember { mutableStateOf(true) }
    var clearAlerts by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CleaningServices,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Pembersih Privasi Sekali Klik", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Pilih data penjelajahan yang ingin dibersihkan dari perangkat Anda:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = clearHistory,
                        onCheckedChange = { clearHistory = it },
                        modifier = Modifier.testTag("checkbox_clear_history")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Riwayat Penjelajahan Web", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text("Menghapus daftar halaman web yang pernah dibuka", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = clearCache,
                        onCheckedChange = { clearCache = it },
                        modifier = Modifier.testTag("checkbox_clear_cache")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Cache & Cookie Sesi Web", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text("Menghapus berkas sementara & cookie web", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = clearAlerts,
                        onCheckedChange = { clearAlerts = it },
                        modifier = Modifier.testTag("checkbox_clear_alerts")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Log Peringatan Jaringan", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text("Menghapus catatan iklan & tracker yang diblokir", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onClearData(clearHistory, clearCache, clearAlerts) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.testTag("confirm_clear_data_button")
            ) {
                Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Bersihkan Sekarang")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_clear_data_button")
            ) {
                Text("Batal")
            }
        }
    )
}
