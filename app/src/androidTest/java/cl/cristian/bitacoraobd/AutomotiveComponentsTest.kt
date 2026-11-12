package cl.cristian.bitacoraobd

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import cl.cristian.bitacoraobd.ui.components.AutomotiveButton
import cl.cristian.bitacoraobd.ui.components.AutomotiveButtonKind
import cl.cristian.bitacoraobd.ui.components.AutomotiveStatus
import cl.cristian.bitacoraobd.ui.components.StatusBadge
import cl.cristian.bitacoraobd.ui.components.VehicleCard
import cl.cristian.bitacoraobd.ui.theme.AutomotiveTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AutomotiveComponentsTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun primaryAndSecondaryButtonsExposeEnabledStateAndRejectDisabledTouch() {
        var clicks = 0
        compose.setContent {
            AutomotiveTheme {
                Column {
                    AutomotiveButton("Primario", { clicks++ })
                    AutomotiveButton("Secundario", { clicks++ }, kind = AutomotiveButtonKind.Secondary)
                    AutomotiveButton("Deshabilitado", { clicks++ }, enabled = false)
                    AutomotiveButton("Secundario deshabilitado", { clicks++ }, kind = AutomotiveButtonKind.Secondary, enabled = false)
                }
            }
        }
        listOf("Primario", "Secundario").forEach {
            compose.onNodeWithText(it).assertIsEnabled().assertHeightIsAtLeast(48.dp).performClick()
        }
        listOf("Deshabilitado", "Secundario deshabilitado").forEach {
            compose.onNodeWithText(it).assertIsNotEnabled().performTouchInput { click() }
        }
        compose.runOnIdle { assertEquals(2, clicks) }
    }

    @Test
    fun narrowVehicleCardGrowsWithFontAndKeepsTitleWithinTwoLines() {
        val scale = mutableStateOf(1f)
        val title = "Toyota Corolla vehículo con un nombre muy largo para dos líneas"
        compose.setContent {
            val density = LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(density, scale.value)) {
                AutomotiveTheme {
                    Column(Modifier.width(280.dp)) {
                        VehicleCard(title, "VEHÍCULO ACTIVO", "Kilometraje actual · 125.400 km", Modifier.width(280.dp))
                    }
                }
            }
        }
        val originalHeight = compose.onNodeWithText(title).fetchSemanticsNode().boundsInRoot.height
        compose.runOnIdle { scale.value = 1.5f }
        compose.onNodeWithText(title).assertIsDisplayed().assert(
            SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading)
        )
        val results = mutableListOf<TextLayoutResult>()
        compose.onNodeWithText(title).performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(results) }
        assertTrue(results.single().lineCount <= 2)
        assertTrue(compose.onNodeWithText(title).fetchSemanticsNode().boundsInRoot.height > originalHeight)
        compose.onNodeWithText("Kilometraje actual · 125.400 km").assertIsDisplayed()
    }

    @Test
    fun badgesExposeStateTextWithoutClickActions() {
        compose.setContent {
            AutomotiveTheme {
                Column {
                    StatusBadge("Correcto", AutomotiveStatus.Success)
                    StatusBadge("Atención", AutomotiveStatus.Warning)
                    StatusBadge("Vencido", AutomotiveStatus.Error)
                }
            }
        }
        listOf("Correcto", "Atención", "Vencido").forEach {
            compose.onNodeWithText(it).assertIsDisplayed().assert(
                SemanticsMatcher.keyNotDefined(SemanticsActions.OnClick)
            )
        }
    }
}
