package cl.cristian.bitacoraobd

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppCompositionTest {
    @Test
    fun applicationSharesProcessDependencies() {
        val application = ApplicationProvider.getApplicationContext<BitacoraApplication>()
        val container = application.appContainer

        assertSame(application.appSession, application.appSession)
        assertSame(container.database, container.database)
        assertSame(container.preferences, container.preferences)
        assertSame(container.gateway, container.gateway)
        assertSame(container.vehicles, container.vehicles)
        assertSame(container.diagnostics, container.diagnostics)
        assertSame(container.maintenance, container.maintenance)
    }
}
