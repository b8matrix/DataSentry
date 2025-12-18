package com.datasentry.app

import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.ViewModelProvider
import com.datasentry.app.data.local.AppDatabase
import com.datasentry.app.data.repository.PacketRepository
import com.datasentry.app.presentation.dashboard.DashboardScreen
import com.datasentry.app.presentation.dashboard.DashboardViewModel
import com.datasentry.app.presentation.dashboard.DashboardViewModelFactory

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: DashboardViewModel

    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            startVpnService()
        } else {
            Toast.makeText(this, "Permission Required for Firewall", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Manual DI (Hackathon Style)
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = PacketRepository(database.packetDao())
        val factory = DashboardViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[DashboardViewModel::class.java]

        setContent {
            MaterialTheme {
                // Hackathon Dark Theme override
                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    DashboardScreen(
                        viewModel = viewModel,
                        onStartVpn = { requestVpnPermission() },
                        onStopVpn = { stopVpnService() }
                    )
                }
            }
        }
    }

    private fun requestVpnPermission() {
        val intent = VpnService.prepare(this)
        if (intent != null) {
            vpnPermissionLauncher.launch(intent)
        } else {
            startVpnService()
        }
    }

    private fun startVpnService() {
        android.util.Log.d("MainActivity", "=== startVpnService() CALLED ===")
        val intent = Intent(this, DataSentryService::class.java)
        startForegroundService(intent)
        android.util.Log.d("MainActivity", "=== startForegroundService() DONE ===")
        Toast.makeText(this, "Firewall Activated", Toast.LENGTH_SHORT).show()
    }

    private fun stopVpnService() {
        android.util.Log.d("MainActivity", "=== stopVpnService() CALLED ===")
        val intent = Intent(this, DataSentryService::class.java).apply {
            action = DataSentryService.ACTION_STOP
        }
        startService(intent) // Send STOP action
        android.util.Log.d("MainActivity", "=== STOP intent sent ===")
        Toast.makeText(this, "Firewall Deactivated", Toast.LENGTH_SHORT).show()
    }
}

