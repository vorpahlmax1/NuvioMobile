package com.nuvio.app.features.multiview

internal expect object MultiViewerLayoutStorage {
    fun loadPayload(): String?
    fun savePayload(payload: String)
}
