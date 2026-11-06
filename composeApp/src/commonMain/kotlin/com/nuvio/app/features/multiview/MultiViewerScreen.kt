package com.nuvio.app.features.multiview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nuvio.app.features.player.PlayerEngineController
import com.nuvio.app.features.player.PlatformPlayerSurface
import com.nuvio.app.navigation.LocalNativeNavigationBarHidden

@Composable
fun MultiViewerScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(Unit) {
        MultiViewerRepository.ensureLoaded()
    }
    val layout by MultiViewerRepository.layout.collectAsStateWithLifecycle()
    val controllers = remember { mutableMapOf<String, PlayerEngineController>() }

    LaunchedEffect(layout.audioFocusSlotId, layout.slots) {
        layout.slots.forEach { slot ->
            controllers[slot.id]?.setMuted(slot.muted || slot.source == null)
        }
    }

    CompositionLocalProviderHiddenNav {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            MultiViewerTopBar(
                preset = layout.preset,
                onBack = onBack,
                onPresetSelected = MultiViewerRepository::setPreset,
                onLoadDemo = { MultiViewerRepository.loadDemoBoard(layout.preset) },
                onClear = MultiViewerRepository::clearAll,
            )
            Spacer(Modifier.height(8.dp))
            MultiViewerGrid(
                layout = layout,
                onFocus = MultiViewerRepository::setAudioFocus,
                onControllerReady = { slotId, controller ->
                    controllers[slotId] = controller
                    val slot = layout.slots.firstOrNull { it.id == slotId }
                    controller.setMuted(slot?.muted != false || slot.source == null)
                },
                onControllerDisposed = { slotId -> controllers.remove(slotId) },
                modifier = Modifier.weight(1f).fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun CompositionLocalProviderHiddenNav(content: @Composable () -> Unit) {
    // MultiViewer is immersive like the single player; hide native nav chrome when present.
    CompositionLocalProvider(
        LocalNativeNavigationBarHidden provides true,
        content = content,
    )
}

@Composable
private fun MultiViewerTopBar(
    preset: MultiViewPreset,
    onBack: () -> Unit,
    onPresetSelected: (MultiViewPreset) -> Unit,
    onLoadDemo: () -> Unit,
    onClear: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
            )
        }
        Text(
            text = "Multiview",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
        )
        MultiViewPreset.entries.forEach { option ->
            TextButton(onClick = { onPresetSelected(option) }) {
                Text(
                    text = option.label,
                    color = if (option == preset) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.7f),
                    fontWeight = if (option == preset) FontWeight.Bold else FontWeight.Normal,
                )
            }
        }
        TextButton(onClick = onLoadDemo) {
            Text(text = "Demo F1", color = Color.White)
        }
        TextButton(onClick = onClear) {
            Text(text = "Clear", color = Color.White.copy(alpha = 0.7f))
        }
    }
}

@Composable
private fun MultiViewerGrid(
    layout: MultiLayoutState,
    onFocus: (String) -> Unit,
    onControllerReady: (String, PlayerEngineController) -> Unit,
    onControllerDisposed: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (layout.preset) {
        MultiViewPreset.Dual -> {
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                layout.slots.forEach { slot ->
                    MultiViewerSlotCell(
                        slot = slot,
                        focused = slot.id == layout.audioFocusSlotId,
                        onFocus = { onFocus(slot.id) },
                        onControllerReady = onControllerReady,
                        onControllerDisposed = onControllerDisposed,
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                    )
                }
            }
        }
        MultiViewPreset.Grid2x2 -> {
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    layout.slots.getOrNull(0)?.let { slot ->
                        MultiViewerSlotCell(
                            slot = slot,
                            focused = slot.id == layout.audioFocusSlotId,
                            onFocus = { onFocus(slot.id) },
                            onControllerReady = onControllerReady,
                            onControllerDisposed = onControllerDisposed,
                            modifier = Modifier.weight(1f).fillMaxSize(),
                        )
                    }
                    layout.slots.getOrNull(1)?.let { slot ->
                        MultiViewerSlotCell(
                            slot = slot,
                            focused = slot.id == layout.audioFocusSlotId,
                            onFocus = { onFocus(slot.id) },
                            onControllerReady = onControllerReady,
                            onControllerDisposed = onControllerDisposed,
                            modifier = Modifier.weight(1f).fillMaxSize(),
                        )
                    }
                }
                Row(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    layout.slots.getOrNull(2)?.let { slot ->
                        MultiViewerSlotCell(
                            slot = slot,
                            focused = slot.id == layout.audioFocusSlotId,
                            onFocus = { onFocus(slot.id) },
                            onControllerReady = onControllerReady,
                            onControllerDisposed = onControllerDisposed,
                            modifier = Modifier.weight(1f).fillMaxSize(),
                        )
                    }
                    layout.slots.getOrNull(3)?.let { slot ->
                        MultiViewerSlotCell(
                            slot = slot,
                            focused = slot.id == layout.audioFocusSlotId,
                            onFocus = { onFocus(slot.id) },
                            onControllerReady = onControllerReady,
                            onControllerDisposed = onControllerDisposed,
                            modifier = Modifier.weight(1f).fillMaxSize(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MultiViewerSlotCell(
    slot: MultiSlotState,
    focused: Boolean,
    onFocus: () -> Unit,
    onControllerReady: (String, PlayerEngineController) -> Unit,
    onControllerDisposed: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(10.dp)
    val borderColor = if (focused) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.18f)
    Box(
        modifier = modifier
            .clip(shape)
            .border(width = if (focused) 2.dp else 1.dp, color = borderColor, shape = shape)
            .background(Color(0xFF121212))
            .clickable(enabled = slot.source != null, onClick = onFocus),
    ) {
        val source = slot.source
        if (source == null) {
            Column(
                modifier = Modifier.align(Alignment.Center).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.GridView,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.35f),
                    modifier = Modifier.size(28.dp),
                )
                Text(
                    text = "Empty slot",
                    color = Color.White.copy(alpha = 0.45f),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        } else {
            var error by remember(source.url) { mutableStateOf<String?>(null) }
            DisposableEffect(slot.id) {
                onDispose { onControllerDisposed(slot.id) }
            }
            PlatformPlayerSurface(
                sourceUrl = source.url,
                sourceHeaders = source.headers,
                modifier = Modifier.fillMaxSize(),
                playWhenReady = true,
                useNativeController = false,
                onControllerReady = { controller -> onControllerReady(slot.id, controller) },
                onSnapshot = {},
                onError = { message -> error = message },
            )
            if (error != null) {
                Text(
                    text = error ?: "Playback error",
                    color = Color(0xFFFF8A80),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(12.dp),
                )
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.55f))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = slot.source?.displayLabel ?: slot.id,
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = if (slot.muted || slot.source == null) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                contentDescription = if (slot.muted) "Muted" else "Audio focus",
                tint = if (focused) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(16.dp),
            )
        }
    }
}
