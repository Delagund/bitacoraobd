package cl.cristian.bitacoraobd.ui.navigation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppRouteTest {
    @Test
    fun allowsOnlyKnownRoutesAndPositiveEntityIds() {
        assertTrue(AppRoute.isAllowedTarget(AppRoute.HOME))
        assertTrue(AppRoute.isAllowedTarget(AppRoute.SETTINGS))
        assertTrue(AppRoute.isAllowedTarget(AppRoute.history(1)))
        assertTrue(AppRoute.isAllowedTarget(AppRoute.diagnosticDetail(2)))
        assertFalse(AppRoute.isAllowedTarget(AppRoute.history(0)))
        assertFalse(AppRoute.isAllowedTarget(AppRoute.diagnosticDetail(-1)))
        assertFalse(AppRoute.isAllowedTarget("unknown"))
    }
}
