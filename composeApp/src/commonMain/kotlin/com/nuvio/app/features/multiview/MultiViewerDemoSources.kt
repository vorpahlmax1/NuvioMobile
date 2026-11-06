package com.nuvio.app.features.multiview

/**
 * Public demo HLS streams only (same URLs as the F1 scraper demo package).
 * No pirated / official F1TV feeds.
 */
object MultiViewerDemoSources {
    val demoSessionId: String = "2026-demo-race"

    val cameras: List<MultiSlotSource> = listOf(
        MultiSlotSource(
            url = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
            title = "[main] International",
            providerName = "F1 Cams",
            f1Role = "main",
            f1CamId = "main",
            f1SessionId = demoSessionId,
        ),
        MultiSlotSource(
            url = "https://devstreaming-cdn.apple.com/videos/streaming/examples/img_bipbop_adv_example_fmp4/master.m3u8",
            title = "[onboard:VER] Verstappen",
            providerName = "F1 Cams",
            f1Role = "onboard",
            f1DriverCode = "VER",
            f1CamId = "onboard-ver",
            f1SessionId = demoSessionId,
        ),
        MultiSlotSource(
            url = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8",
            title = "[onboard:NOR] Norris",
            providerName = "F1 Cams",
            f1Role = "onboard",
            f1DriverCode = "NOR",
            f1CamId = "onboard-nor",
            f1SessionId = demoSessionId,
        ),
        MultiSlotSource(
            url = "https://test-streams.mux.dev/test_001/stream.m3u8",
            title = "[pit] Pit Lane",
            providerName = "F1 Cams",
            f1Role = "pit",
            f1CamId = "pit",
            f1SessionId = demoSessionId,
        ),
        MultiSlotSource(
            url = "https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_4x3/bipbop_4x3_variant.m3u8",
            title = "[track] Turn 1",
            providerName = "F1 Cams",
            f1Role = "track",
            f1CamId = "track-t1",
            f1SessionId = demoSessionId,
        ),
        MultiSlotSource(
            url = "https://test-streams.mux.dev/pts_shift/master.m3u8",
            title = "[timing] Timing Tower",
            providerName = "F1 Cams",
            f1Role = "timing",
            f1CamId = "timing",
            f1SessionId = demoSessionId,
        ),
    )

    fun forPreset(preset: MultiViewPreset): List<MultiSlotSource> =
        cameras.take(preset.slotCount)
}
