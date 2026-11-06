package com.nuvio.app.features.multiview

import kotlinx.serialization.Serializable

@Serializable
enum class MultiViewPreset {
    Dual,
    Grid2x2,
    ;

    val slotCount: Int
        get() = when (this) {
            Dual -> 2
            Grid2x2 -> 4
        }

    val label: String
        get() = when (this) {
            Dual -> "1+1"
            Grid2x2 -> "2×2"
        }
}

@Serializable
data class MultiSlotSource(
    val url: String,
    val title: String? = null,
    val headers: Map<String, String> = emptyMap(),
    val providerName: String? = null,
    val f1Role: String? = null,
    val f1DriverCode: String? = null,
    val f1CamId: String? = null,
    val f1SessionId: String? = null,
) {
    val identity: F1CamIdentity?
        get() = resolveF1CamIdentity(
            title = title,
            f1Role = f1Role,
            f1DriverCode = f1DriverCode,
            f1CamId = f1CamId,
            f1SessionId = f1SessionId,
        )

    val displayLabel: String
        get() {
            val identity = identity
            val roleLabel = identity?.let { cam ->
                when (cam.role) {
                    F1CamRole.Onboard -> cam.driverCode?.let { "ONB $it" } ?: "Onboard"
                    else -> cam.role.wireName.uppercase()
                }
            }
            val name = identity?.label ?: title?.trim().orEmpty()
            return when {
                roleLabel != null && name.isNotEmpty() -> "$roleLabel · $name"
                roleLabel != null -> roleLabel
                name.isNotEmpty() -> name
                else -> "Empty"
            }
        }
}

@Serializable
data class MultiSlotState(
    val id: String,
    val source: MultiSlotSource? = null,
    val muted: Boolean = true,
)

@Serializable
data class MultiLayoutState(
    val preset: MultiViewPreset = MultiViewPreset.Grid2x2,
    val slots: List<MultiSlotState> = emptyList(),
    val audioFocusSlotId: String? = null,
) {
    fun withPreset(preset: MultiViewPreset): MultiLayoutState {
        val resized = ensureSlotCount(preset.slotCount)
        return resized.copy(preset = preset)
    }

    fun ensureSlotCount(count: Int): MultiLayoutState {
        if (slots.size == count) return this
        if (slots.size > count) {
            val kept = slots.take(count)
            val focus = audioFocusSlotId?.takeIf { id -> kept.any { it.id == id } }
            return copy(slots = kept, audioFocusSlotId = focus)
        }
        val extra = (slots.size until count).map { index ->
            MultiSlotState(id = "slot-${index + 1}")
        }
        return copy(slots = slots + extra)
    }

    fun withSource(slotId: String, source: MultiSlotSource?): MultiLayoutState =
        copy(
            slots = slots.map { slot ->
                if (slot.id == slotId) slot.copy(source = source) else slot
            },
        )

    fun withAudioFocus(slotId: String?): MultiLayoutState {
        val focusId = slotId?.takeIf { id -> slots.any { it.id == id && it.source != null } }
        return copy(
            audioFocusSlotId = focusId,
            slots = slots.map { slot ->
                slot.copy(muted = focusId == null || slot.id != focusId)
            },
        )
    }

    companion object {
        fun empty(preset: MultiViewPreset = MultiViewPreset.Grid2x2): MultiLayoutState {
            val slots = (1..preset.slotCount).map { index ->
                MultiSlotState(id = "slot-$index")
            }
            return MultiLayoutState(preset = preset, slots = slots, audioFocusSlotId = null)
        }
    }
}
