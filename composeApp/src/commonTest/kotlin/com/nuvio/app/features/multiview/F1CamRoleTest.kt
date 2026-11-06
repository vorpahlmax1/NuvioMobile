package com.nuvio.app.features.multiview

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class F1CamRoleTest {
    @Test
    fun parsesMainTitle() {
        val identity = parseTitleRole("[main] International")
        assertEquals(F1CamRole.Main, identity?.role)
        assertEquals("International", identity?.label)
        assertNull(identity?.driverCode)
    }

    @Test
    fun parsesOnboardWithDriverCode() {
        val identity = parseTitleRole("[onboard:VER] Verstappen")
        assertEquals(F1CamRole.Onboard, identity?.role)
        assertEquals("VER", identity?.driverCode)
        assertEquals("Verstappen", identity?.label)
    }

    @Test
    fun structuredFieldsWinOverTitle() {
        val identity = resolveF1CamIdentity(
            title = "[pit] Pit Lane",
            f1Role = "onboard",
            f1DriverCode = "nor",
            f1CamId = "onboard-nor",
            f1SessionId = "2026-demo-race",
        )
        assertEquals(F1CamRole.Onboard, identity?.role)
        assertEquals("NOR", identity?.driverCode)
        assertEquals("onboard-nor", identity?.camId)
        assertEquals("2026-demo-race", identity?.sessionId)
    }

    @Test
    fun fallsBackToTitleWhenNoStructuredRole() {
        val identity = resolveF1CamIdentity(title = "[timing] Timing Tower")
        assertEquals(F1CamRole.Timing, identity?.role)
        assertEquals("Timing Tower", identity?.label)
    }

    @Test
    fun rejectsUnknownPrefix() {
        assertNull(parseTitleRole("[foo] Bar"))
        assertNull(parseTitleRole("International"))
    }

    @Test
    fun displayLabelIncludesRoleAndName() {
        val source = MultiSlotSource(
            url = "https://example.com/a.m3u8",
            title = "[onboard:VER] Verstappen",
            f1Role = "onboard",
            f1DriverCode = "VER",
        )
        assertEquals("ONB VER · Verstappen", source.displayLabel)
    }

    @Test
    fun audioFocusMutesOtherSlots() {
        val layout = MultiLayoutState.empty(MultiViewPreset.Dual)
            .withSource("slot-1", MultiSlotSource(url = "https://a", title = "[main] A", f1Role = "main"))
            .withSource("slot-2", MultiSlotSource(url = "https://b", title = "[pit] B", f1Role = "pit"))
            .withAudioFocus("slot-2")
        assertEquals("slot-2", layout.audioFocusSlotId)
        assertEquals(true, layout.slots.first { it.id == "slot-1" }.muted)
        assertEquals(false, layout.slots.first { it.id == "slot-2" }.muted)
    }
}
