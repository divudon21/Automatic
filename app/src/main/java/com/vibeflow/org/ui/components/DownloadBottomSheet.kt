package com.vibeflow.org.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.vibeflow.org.data.models.AudioQuality
import com.vibeflow.org.data.models.MusicSource
import com.vibeflow.org.data.models.Track
import com.vibeflow.org.data.models.getFormattedDuration
import com.vibeflow.org.data.models.getQualityLabel
import com.vibeflow.org.download.estimatedSizeMB
import com.vibeflow.org.download.estimatedSizeMBYt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadBottomSheet(
    track: Track,
    onDismiss: () -> Unit,
    onDownload: (AudioQuality) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .background(
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        RoundedCornerShape(2.dp)
                    )
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
        ) {
            // ── Track header ─────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(10.dp))
                ) {
                    if (track.thumbnailUrl.isNotBlank()) {
                        AsyncImage(
                            model = track.thumbnailUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(0.3f))
                                    )
                                )
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.MusicNote, null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = track.artist,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AccessTime, null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(
                            text = track.getFormattedDuration(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (track.source == MusicSource.YOUTUBE) {
                            Spacer(Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .background(
                                        Color(0xFFFF0000).copy(0.15f),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    "YT Music", color = Color(0xFFFF0000),
                                    fontSize = 9.sp, fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Spacer(Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(0.12f),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    "JioSaavn",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 9.sp, fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close, "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(0.2f))
            Spacer(Modifier.height(16.dp))

            // ── Section title ────────────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Download, null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Select Download Quality",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(Modifier.height(14.dp))

            // ── Quality Tiles ────────────────────────────────────────────────
            if (track.source == MusicSource.JIOSAAVN) {
                // JioSaavn: 3 real quality options
                val lowLabel  = track.getQualityLabel(AudioQuality.LOW)
                val midLabel  = track.getQualityLabel(AudioQuality.MEDIUM)
                val highLabel = track.getQualityLabel(AudioQuality.HIGH)

                DownloadQualityTile(
                    icon = Icons.Default.Speed,
                    label = "Low",
                    detail = "$lowLabel · Saves data",
                    size = estimatedSizeMB(track.durationSec, AudioQuality.LOW),
                    accentColor = Color(0xFF4CAF50),
                    onClick = { onDownload(AudioQuality.LOW) }
                )
                Spacer(Modifier.height(10.dp))
                DownloadQualityTile(
                    icon = Icons.Default.GraphicEq,
                    label = "Medium",
                    detail = "$midLabel · Balanced",
                    size = estimatedSizeMB(track.durationSec, AudioQuality.MEDIUM),
                    accentColor = Color(0xFF2196F3),
                    onClick = { onDownload(AudioQuality.MEDIUM) }
                )
                Spacer(Modifier.height(10.dp))
                DownloadQualityTile(
                    icon = Icons.Default.HighQuality,
                    label = "High",
                    detail = "$highLabel · Best quality",
                    size = estimatedSizeMB(track.durationSec, AudioQuality.HIGH),
                    accentColor = MaterialTheme.colorScheme.primary,
                    onClick = { onDownload(AudioQuality.HIGH) }
                )
            } else {
                // YouTube: 3 quality options via InnerTube/Piped
                // Low ≈ 48kbps, Medium ≈ 128kbps, High ≈ best available
                DownloadQualityTile(
                    icon = Icons.Default.Speed,
                    label = "Low",
                    detail = "~48 kbps · Saves data",
                    size = estimatedSizeMBYt(track.durationSec, "low"),
                    accentColor = Color(0xFF4CAF50),
                    onClick = { onDownload(AudioQuality.LOW) }
                )
                Spacer(Modifier.height(10.dp))
                DownloadQualityTile(
                    icon = Icons.Default.GraphicEq,
                    label = "Medium",
                    detail = "~128 kbps · Balanced",
                    size = estimatedSizeMBYt(track.durationSec, "medium"),
                    accentColor = Color(0xFF2196F3),
                    onClick = { onDownload(AudioQuality.MEDIUM) }
                )
                Spacer(Modifier.height(10.dp))
                DownloadQualityTile(
                    icon = Icons.Default.HighQuality,
                    label = "High",
                    detail = "Best available · M4A",
                    size = estimatedSizeMBYt(track.durationSec, "high"),
                    accentColor = Color(0xFFFF0000),
                    onClick = { onDownload(AudioQuality.HIGH) }
                )
            }

            Spacer(Modifier.height(12.dp))
            Text(
                text = "⬇ Saved to Music/VibeFlow/",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.55f),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun DownloadQualityTile(
    icon: ImageVector,
    label: String,
    detail: String,
    size: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = accentColor.copy(alpha = 0.07f),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = accentColor.copy(alpha = 0.22f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(accentColor.copy(0.13f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = accentColor, modifier = Modifier.size(22.dp))
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // File size badge
            Box(
                modifier = Modifier
                    .background(accentColor.copy(0.13f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = size,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }

            Spacer(Modifier.width(6.dp))
            Icon(
                Icons.Default.ChevronRight, null,
                tint = accentColor.copy(0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
