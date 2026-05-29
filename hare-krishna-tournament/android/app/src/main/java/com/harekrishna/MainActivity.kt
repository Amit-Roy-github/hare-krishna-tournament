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
import com.harekrishna.ui.theme.HareKrishnaTheme
import com.harekrishna.ui.theme.Palettes

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
                    AppNavigation(services = services)
                }
            }
        }
    }
}
