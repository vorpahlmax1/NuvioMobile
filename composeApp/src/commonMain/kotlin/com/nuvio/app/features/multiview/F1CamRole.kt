package com.nuvio.app.features.multiview

/**
 * F1 camera role tags used by the F1 scraper / MultiNutz handoff.
 *
 * Title grammar (normative):
 *   `[main] International`
 *   `[onboard:VER] Verstappen`
 *   `[pit] Pit Lane`
 */
enum class F1CamRole {
    Main,
    Onboard,
    Pit,
    Track,
    Timing,
    Custom,
    ;

    val wireName: String
        get() = when (this) {
            Main -> "main"
            Onboard -> "onboard"
            Pit -> "pit"
            Track -> "track"
            Timing -> "timing"
            Custom -> "custom"
        }

    companion object {
        fun fromWire(value: String?): F1CamRole? {
            val normalized = value?.trim()?.lowercase().orEmpty()
            if (normalized.isEmpty()) return null
            return entries.firstOrNull { it.wireName == normalized }
        }
    }
}

data class F1CamIdentity(
    val role: F1CamRole,
    val driverCode: String? = null,
    val label: String? = null,
    val camId: String? = null,
    val sessionId: String? = null,
)

private val titleRoleRegex = Regex(
    """^\s*\[(?<role>[A-Za-z]+)(?::(?<driver>[A-Za-z0-9]{1,4}))?]\s*(?<label>.*)$""",
)

/**
 * Resolve F1 camera identity from optional structured fields and/or a bracketed stream title.
 * Structured fields win when present; the title grammar is the fallback for stock addons.
 */
fun resolveF1CamIdentity(
    title: String?,
    f1Role: String? = null,
    f1DriverCode: String? = null,
    f1CamId: String? = null,
    f1SessionId: String? = null,
): F1CamIdentity? {
    val structuredRole = F1CamRole.fromWire(f1Role)
    if (structuredRole != null) {
        return F1CamIdentity(
            role = structuredRole,
            driverCode = f1DriverCode?.trim()?.uppercase()?.takeIf { it.isNotEmpty() }
                ?: if (structuredRole == F1CamRole.Onboard) parseTitleRole(title)?.driverCode else null,
            label = parseTitleRole(title)?.label ?: title?.trim()?.takeIf { it.isNotEmpty() },
            camId = f1CamId?.trim()?.takeIf { it.isNotEmpty() },
            sessionId = f1SessionId?.trim()?.takeIf { it.isNotEmpty() },
        )
    }
    return parseTitleRole(title)?.copy(
        camId = f1CamId?.trim()?.takeIf { it.isNotEmpty() },
        sessionId = f1SessionId?.trim()?.takeIf { it.isNotEmpty() },
    )
}

fun parseTitleRole(title: String?): F1CamIdentity? {
    val raw = title?.trim().orEmpty()
    if (raw.isEmpty()) return null
    val match = titleRoleRegex.matchEntire(raw) ?: return null
    val role = F1CamRole.fromWire(match.groups["role"]?.value) ?: return null
    val driver = match.groups["driver"]?.value?.uppercase()?.takeIf { it.isNotEmpty() }
    val label = match.groups["label"]?.value?.trim()?.takeIf { it.isNotEmpty() }
    return F1CamIdentity(
        role = role,
        driverCode = driver,
        label = label,
    )
}
