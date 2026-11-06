package com.nuvio.app.features.multiview

import com.nuvio.app.features.streams.StreamItem

fun StreamItem.toMultiSlotSource(): MultiSlotSource? {
    val playable = playableDirectUrl ?: return null
    return MultiSlotSource(
        url = playable,
        title = title ?: name,
        headers = behaviorHints.proxyHeaders?.request.orEmpty(),
        providerName = addonName,
        f1Role = f1Role,
        f1DriverCode = f1DriverCode,
        f1CamId = f1CamId,
        f1SessionId = f1SessionId,
    )
}

fun List<StreamItem>.toMultiSlotSources(): List<MultiSlotSource> =
    mapNotNull { it.toMultiSlotSource() }
