package com.example.darts501

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {

    private val voice: VoiceController by lazy { VoiceController(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            val vm: GameViewModel = viewModel()

            DisposableEffect(vm) {
                voice.onResult = { variants -> vm.onVoice(variants) }
                onDispose { voice.onResult = {} }
            }

            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { granted ->
                if (granted) voice.start()
            }

            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFF2962FF),
                    background = Color(0xFF0B0B1E),
                    surface = Color(0xFF1E1E4B)
                )
            ) {
                DartsScreen(
                    vm = vm,
                    voice = voice,
                    onMicToggle = {
                        if (voice.isListening) {
                            voice.stop()
                        } else {
                            val granted = ContextCompat.checkSelfPermission(
                                this, Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED
                            if (granted) voice.start()
                            else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                )
            }
        }
    }

    override fun onStop() {
        super.onStop()
        voice.stop()
    }

    override fun onDestroy() {
        voice.stop()
        super.onDestroy()
    }
}
