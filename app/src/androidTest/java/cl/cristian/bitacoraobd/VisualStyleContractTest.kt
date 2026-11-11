package cl.cristian.bitacoraobd

import androidx.compose.foundation.layout.size
import androidx.compose.ui.geometry.Rect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import cl.cristian.bitacoraobd.domain.model.DistanceUnit
import cl.cristian.bitacoraobd.domain.model.MaintenanceRecord
import cl.cristian.bitacoraobd.domain.repository.MaintenanceRepository
import cl.cristian.bitacoraobd.domain.usecase.GetMaintenanceStatusUseCase
import cl.cristian.bitacoraobd.domain.usecase.SaveMaintenanceUseCase
import cl.cristian.bitacoraobd.ui.maintenance.MaintenanceScreen
import cl.cristian.bitacoraobd.ui.maintenance.MaintenanceViewModel
import java.time.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import cl.cristian.bitacoraobd.ui.components.GraphiteCard
import cl.cristian.bitacoraobd.ui.settings.SettingsScreen
import cl.cristian.bitacoraobd.ui.settings.SettingsViewModel
import cl.cristian.bitacoraobd.ui.theme.BitacoraOBDTheme
import cl.cristian.bitacoraobd.ui.vehicles.VehiclesScreen
import cl.cristian.bitacoraobd.ui.vehicles.VehiclesViewModel
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.math.pow

class VisualStyleContractTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun settingsNamesTheSectionNombreWithoutVehiculo() {
        val fixture = TestSessionFixture()
        val viewModel = SettingsViewModel(fixture.preferences)

        composeRule.setContent {
            BitacoraOBDTheme { SettingsScreen(viewModel) {} }
        }
        composeRule.waitUntil(5_000) { viewModel.draft != null }

        composeRule.onNodeWithText("Nombre").assertIsDisplayed()
        assertTrue(composeRule.onAllNodesWithText("Vehículo").fetchSemanticsNodes().isEmpty())
    }

    @Test
    fun graphiteCardUsesFixedFillAndOutlineInBothThemes() {
        val darkTheme = mutableStateOf(value = false)
        composeRule.setContent {
            BitacoraOBDTheme(darkTheme = darkTheme.value) {
                GraphiteCard(Modifier.size(width = 240.dp, height = 120.dp)) {
                    Text("Tarjeta")
                }
            }
        }

        listOf(false, true).forEach { isDark ->
            composeRule.runOnIdle { darkTheme.value = isDark }
            composeRule.waitForIdle()

            val pixels = composeRule.onRoot().captureToImage().toPixelMap()
            assertTrue("graphite fill missing for darkTheme=$isDark", pixels.count { it == Color(0xFF2B2E33) } > 1_000)
            assertTrue("outline missing for darkTheme=$isDark", pixels.count { it == Color(0xFF87919B) } > 20)
        }
    }

    @Test
    fun unselectedMillasTextContrastsWithGraphiteInBothThemes() {
        val fixture = TestSessionFixture()
        val viewModel = SettingsViewModel(fixture.preferences)
        val darkTheme = mutableStateOf(value = false)

        composeRule.setContent {
            BitacoraOBDTheme(darkTheme = darkTheme.value) { SettingsScreen(viewModel) {} }
        }
        composeRule.waitUntil(5_000) { viewModel.draft != null }

        listOf(false, true).forEach { isDark ->
            composeRule.runOnIdle { darkTheme.value = isDark }
            composeRule.waitForIdle()
            val label = composeRule.onNodeWithText("Millas", useUnmergedTree = true)
            label.assertIsDisplayed()

            val pixels = composeRule.onRoot().captureToImage().toPixelMap()
            val bounds = label.fetchSemanticsNode().boundsInRoot
            assertTrue(
                "Millas lacks 4.5:1 contrast in darkTheme=$isDark",
                pixels.count(bounds) { it.contrastRatio(Color(0xFF2B2E33)) >= 4.5f } > 1,
            )
        }
    }

    @Test
    fun maintenanceFieldLabelContrastsWithGraphiteInBothThemes() {
        val fixture = TestSessionFixture()
        val maintenance = EmptyVisualMaintenanceRepository()
        val viewModel = MaintenanceViewModel(
            maintenance,
            GetMaintenanceStatusUseCase(fixture.vehicles, maintenance, Clock.systemDefaultZone()),
            SaveMaintenanceUseCase(fixture.vehicles, maintenance),
            fixture.preferences,
        )
        val darkTheme = mutableStateOf(value = false)

        composeRule.setContent {
            BitacoraOBDTheme(darkTheme = darkTheme.value) {
                MaintenanceScreen(1L, viewModel, DistanceUnit.KILOMETERS)
            }
        }
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("Registrar mantenimiento").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Registrar mantenimiento").performClick()
        composeRule.waitUntil(5_000) { viewModel.draft != null }

        listOf(false, true).forEach { isDark ->
            composeRule.runOnIdle { darkTheme.value = isDark }
            composeRule.waitForIdle()
            val label = composeRule.onNodeWithText("Fecha (AAAA-MM-DD)", useUnmergedTree = true)
            label.assertIsDisplayed()
            val pixels = composeRule.onRoot().captureToImage().toPixelMap()
            val bounds = label.fetchSemanticsNode().boundsInRoot
            assertTrue(
                "maintenance label lacks 4.5:1 contrast in darkTheme=$isDark",
                pixels.count(bounds) { it.contrastRatio(Color(0xFF2B2E33)) >= 4.5f } > 1,
            )
        }
    }

    @Test
    fun vehicleFormUsesGreenSaveAndRedCancelar() {
        val fixture = TestSessionFixture()
        val viewModel = VehiclesViewModel(fixture.session, fixture.preferences)

        composeRule.setContent {
            BitacoraOBDTheme { VehiclesScreen(fixture.session, viewModel) }
        }
        composeRule.onNodeWithText("Agregar vehículo").performClick()
        composeRule.waitUntil(5_000) { viewModel.draft != null }
        composeRule.waitForIdle()

        val save = composeRule.onNodeWithText("Guardar")
        val cancel = composeRule.onNodeWithText("Cancelar")
        save.assertIsDisplayed().assertHasClickAction()
        cancel.assertIsDisplayed().assertHasClickAction()

        val pixels = composeRule.onRoot().captureToImage().toPixelMap()
        val saveBounds = save.fetchSemanticsNode().boundsInRoot
        val cancelBounds = cancel.fetchSemanticsNode().boundsInRoot
        assertTrue("green action button missing", pixels.count(saveBounds) { it == Color(0xFF275D46) } > 100)
        assertTrue(
            "red Cancelar label missing",
            pixels.count(cancelBounds) {
                (it.red > (it.green * 1.35f)) && (it.red > (it.blue * 1.2f))
            } > 5,
        )
    }
}

private inline fun androidx.compose.ui.graphics.PixelMap.count(
    bounds: Rect = Rect(0f, 0f, width.toFloat(), height.toFloat()),
    matches: (Color) -> Boolean,
): Int {
    val left = bounds.left.toInt().coerceIn(0, width)
    val top = bounds.top.toInt().coerceIn(0, height)
    val right = bounds.right.toInt().coerceIn(left, width)
    val bottom = bounds.bottom.toInt().coerceIn(top, height)
    var count = 0
    for (y in top until bottom) {
        for (x in left until right) {
            if (matches(this[x, y])) count++
        }
    }
    return count
}

private fun Color.contrastRatio(background: Color): Float {
    val foregroundLuminance = luminance()
    val backgroundLuminance = background.luminance()
    return (maxOf(foregroundLuminance, backgroundLuminance) + 0.05f) /
        (minOf(foregroundLuminance, backgroundLuminance) + 0.05f)
}

private fun Color.luminance(): Float = listOf(red, green, blue).map { component ->
    if (component <= 0.04045f) component / 12.92f else ((component + 0.055f) / 1.055f).pow(2.4f)
}.let { (it[0] * 0.2126f) + (it[1] * 0.7152f) + (it[2] * 0.0722f) }

private class EmptyVisualMaintenanceRepository : MaintenanceRepository {
    override fun observeMaintenanceByVehicle(vehicleId: Long): Flow<List<MaintenanceRecord>> =
        MutableStateFlow(emptyList())
    override suspend fun getMaintenanceById(recordId: Long): MaintenanceRecord? = null
    override suspend fun saveMaintenanceRecord(record: MaintenanceRecord): Long = 0L
    override suspend fun getLatestRecordsPerAlertableType(vehicleId: Long): List<MaintenanceRecord> = emptyList()
}
