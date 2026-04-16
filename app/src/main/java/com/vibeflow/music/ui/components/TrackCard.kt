package com.vibeflow.music.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.vibeflow.music.data.models.MusicSource
import com.vibeflow.music.data.models.Track
import com.vibeflow.music.data.models.getFormattedDuration

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TrackListItem(
    track: Track,
    isPlaying: Boolean = false,
    isBlurred: Boolean = false,
    onClick: () -> Unit,
    onDownloadClick: (Track) -> Unit,
    modifier: Modifier = Modifier
) {
    val alphaVal by animateFloatAsState(
        targetValue = if (isBlurred) 0.3f else 1f,
        animationSpec = tween(200), label = "alpha"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = { onDownloadClick(track) })
            .alpha(alphaVal)
            .then(if (isBlurred) Modifier.blur(3.dp) else Modifier)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(56.dp)) {
            if (track.thumbnailUrl.isNotBlank()) {
                AsyncImage(
                    model = track.thumbnailUrl,
                    contentDescription = track.title,
                    modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.MusicNote, null, tint = MaterialTheme.colorScheme.primary) }
            }
            if (isPlaying) {
                Box(
                    modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PlayArrow, "Playing",
                        tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                }
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Normal,
                color = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = track.artist,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.width(4.dp))

        // Download icon button
        IconButton(onClick = { onDownloadClick(track) }, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.Download, "Download",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f),
                modifier = Modifier.size(18.dp))
        }

        Column(horizontalAlignment = Alignment.End) {
            when (track.source) {
                MusicSource.YOUTUBE -> {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFF0000).copy(0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) { Text("YT", color = Color(0xFFFF0000), fontSize = 9.sp, fontWeight = FontWeight.Bold) }
                    Spacer(Modifier.height(2.dp))
                }
                MusicSource.AUDIOMACK -> {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFF6600).copy(0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) { Text("DZ", color = Color(0xFFFF6600), fontSize = 9.sp, fontWeight = FontWeight.Bold) }
                    Spacer(Modifier.height(2.dp))
                }
                else -> {}
            }
            Text(
                text = track.getFormattedDuration(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TrackHorizontalCard(
    track: Track,
    isPlaying: Boolean = false,
    isBlurred: Boolean = false,
    onClick: () -> Unit,
    onDownloadClick: (Track) -> Unit,
    modifier: Modifier = Modifier
) {
    val alphaVal by animateFloatAsState(
        targetValue = if (isBlurred) 0.3f else 1f,
        animationSpec = tween(200), label = "alpha"
    )

    Card(
        modifier = modifier
            .width(140.dp)
            .combinedClickable(onClick = onClick, onLongClick = { onDownloadClick(track) })
            .alpha(alphaVal)
            .then(if (isBlurred) Modifier.blur(3.dp) else Modifier),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Box(modifier = Modifier.height(140.dp)) {
                if (track.thumbnailUrl.isNotBlank()) {
                    AsyncImage(
                        model = track.thumbnailUrl,
                        contentDescription = track.title,
                        modifier = Modifier.size(140.dp)
                            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.size(140.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.MusicNote, null,
                            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                    }
                }

                // Duration badge — bottom right
                Box(
                    modifier = Modifier.align(Alignment.BottomEnd).padding(6.dp)
                        .background(Color.Black.copy(0.72f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) { Text(track.getFormattedDuration(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Medium) }

                // Source badge — top left
                when (track.source) {
                    MusicSource.YOUTUBE -> Box(
                        modifier = Modifier.align(Alignment.TopStart).padding(6.dp)
                            .background(Color(0xFFFF0000), RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) { Text("YT", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold) }
                    MusicSource.AUDIOMACK -> Box(
                        modifier = Modifier.align(Alignment.TopStart).padding(6.dp)
                            .background(Color(0xFFFF6600), RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) { Text("DZ", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold) }
                    else -> {}
                }

                // Download button — top right
                Box(
                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                        .size(28.dp)
                        .background(Color.Black.copy(0.55f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = { onDownloadClick(track) }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Download, "Download",
                            tint = Color.White, modifier = Modifier.size(15.dp))
                    }
                }

                if (isPlaying) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.35f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PlayArrow, "Playing",
                            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
                    }
                }
            }

            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                    color = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = track.artist,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
