package com.nuvio.app.features.multiview

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object MultiViewerRepository {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val _layout = MutableStateFlow(MultiLayoutState.empty())
    val layout: StateFlow<MultiLayoutState> = _layout.asStateFlow()

    private var loaded = false

    fun ensureLoaded() {
        if (loaded) return
        loaded = true
        val restored = runCatching {
            MultiViewerLayoutStorage.loadPayload()?.let { payload ->
                json.decodeFromString(MultiLayoutState.serializer(), payload)
            }
        }.getOrNull()
        _layout.value = (restored ?: MultiLayoutState.empty())
            .ensureSlotCount((restored?.preset ?: MultiViewPreset.Grid2x2).slotCount)
    }

    fun setPreset(preset: MultiViewPreset) {
        ensureLoaded()
        update { it.withPreset(preset) }
    }

    fun setSlotSource(slotId: String, source: MultiSlotSource?) {
        ensureLoaded()
        update { current ->
            val next = current.withSource(slotId, source)
            if (source != null && next.audioFocusSlotId == null) {
                next.withAudioFocus(slotId)
            } else if (source == null && next.audioFocusSlotId == slotId) {
                next.withAudioFocus(next.slots.firstOrNull { it.source != null }?.id)
            } else {
                next.withAudioFocus(next.audioFocusSlotId)
            }
        }
    }

    fun setAudioFocus(slotId: String) {
        ensureLoaded()
        update { it.withAudioFocus(slotId) }
    }

    fun loadDemoBoard(preset: MultiViewPreset = MultiViewPreset.Grid2x2) {
        ensureLoaded()
        val sources = MultiViewerDemoSources.forPreset(preset)
        val slots = sources.mapIndexed { index, source ->
            MultiSlotState(
                id = "slot-${index + 1}",
                source = source,
                muted = index != 0,
            )
        }
        update {
            MultiLayoutState(
                preset = preset,
                slots = slots,
                audioFocusSlotId = slots.firstOrNull()?.id,
            )
        }
    }

    fun assignStreamsByRole(sources: List<MultiSlotSource>, preset: MultiViewPreset = MultiViewPreset.Grid2x2) {
        ensureLoaded()
        val prioritized = sources.sortedWith(
            compareBy<MultiSlotSource> { rolePriority(it.identity?.role) }
                .thenBy { it.identity?.driverCode.orEmpty() }
                .thenBy { it.title.orEmpty() },
        )
        val selected = prioritized.take(preset.slotCount)
        val slots = (0 until preset.slotCount).map { index ->
            val source = selected.getOrNull(index)
            MultiSlotState(
                id = "slot-${index + 1}",
                source = source,
                muted = index != 0 || source == null,
            )
        }
        val focusId = slots.firstOrNull { it.source != null }?.id
        update {
            MultiLayoutState(
                preset = preset,
                slots = slots,
                audioFocusSlotId = focusId,
            ).withAudioFocus(focusId)
        }
    }

    fun clearAll() {
        ensureLoaded()
        update { MultiLayoutState.empty(it.preset) }
    }

    private fun update(transform: (MultiLayoutState) -> MultiLayoutState) {
        val next = transform(_layout.value)
        _layout.value = next
        persist(next)
    }

    private fun persist(state: MultiLayoutState) {
        runCatching {
            MultiViewerLayoutStorage.savePayload(json.encodeToString(MultiLayoutState.serializer(), state))
        }
    }

    private fun rolePriority(role: F1CamRole?): Int = when (role) {
        F1CamRole.Main -> 0
        F1CamRole.Onboard -> 1
        F1CamRole.Pit -> 2
        F1CamRole.Track -> 3
        F1CamRole.Timing -> 4
        F1CamRole.Custom -> 5
        null -> 6
    }
}
