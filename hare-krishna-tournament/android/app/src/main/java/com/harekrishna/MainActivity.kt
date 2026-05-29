package com.harekrishna

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.harekrishna.ui.navigation.AppNavigation
import com.harekrishna.ui.preview.ColorLabScreen
import com.harekrishna.ui.preview.HomeWireframeScreen
import com.harekrishna.ui.theme.HareKrishnaTheme
import com.harekrishna.ui.theme.Palettes

// TEMP: which screen to show while designing. Set to APP to restore the real app.
private enum class DevScreen { APP, COLOR_LAB, HOME_WIREFRAME }
private val DEV_SCREEN = DevScreen.APP

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val services = (application as HareKrishnaApp).services
        setContent {
            val paletteName by services.userPrefs.selectedPalette.collectAsStateWithLifecycle(initialValue = null)
            HareKrishnaTheme(palette = Palettes.byName(paletteName)) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color    = MaterialTheme.colorScheme.background,
                ) {
                    when (DEV_SCREEN) {
                        DevScreen.COLOR_LAB      -> ColorLabScreen()
                        DevScreen.HOME_WIREFRAME -> HomeWireframeScreen()
                        DevScreen.APP            -> AppNavigation(services = services)
                    }
                }
            }
        }
    }
}
