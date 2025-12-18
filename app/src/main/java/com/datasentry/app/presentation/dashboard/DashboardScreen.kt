package com.datasentry.app.presentation.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.datasentry.app.data.local.entity.PacketEntity
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onStartVpn: () -> Unit,
    onStopVpn: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val packets by viewModel.packets.collectAsState()
    val riskCount by viewModel.riskCount.collectAsState()
    var isSimulating by remember { mutableStateOf(false) }
    var isVpnActive by remember { mutableStateOf(false) }

    // Hackathon Visuals
    val privacyScore = (100 - (riskCount * 5)).coerceAtLeast(0)
    val scoreColor = when {
        privacyScore > 80 -> Color(0xFF00E676) // Green
        privacyScore > 50 -> Color(0xFFFFC107) // Amber
        else -> Color(0xFFFF5252) // Red
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)) // Dark BG
            .padding(16.dp)
    ) {
        // App Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "DataSentry",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { viewModel.clearLogs() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Clear", tint = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Privacy Score Card
        Card(
            modifier = Modifier.fillMaxWidth().height(160.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("PRIVACY HEALTH", color = Color.Gray, fontSize = 12.sp)
                    Text(
                        text = "$privacyScore%",
                        color = scoreColor,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = if (privacyScore > 80) "Device Secured" else "Risks Detected",
                        color = scoreColor.copy(alpha = 0.8f),
                        fontSize = 16.sp
                    )
                }
                
                // Visualization: Privacy Pulse Map
                Box(
                    modifier = Modifier.size(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    PrivacyPulseMap(isActive = isSimulating)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Actions
        Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { 
                    isSimulating = !isSimulating
                    if (isSimulating) {
                        viewModel.startSimulation()
                    } else {
                        viewModel.stopSimulation()
                        onStopVpn() // Stop the actual VPN service
                    }
                },
                modifier = Modifier.weight(1f).height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSimulating) Color(0xFFCF6679) else Color(0xFFBB86FC)
                )
            ) {
                Icon(
                    if (isSimulating) Icons.Default.Warning else Icons.Default.PlayArrow,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isSimulating) "STOP MONITOR" else "START MONITOR")
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Button(
                onClick = {
                    android.widget.Toast.makeText(context, "🔴 BUTTON CLICKED!", android.widget.Toast.LENGTH_SHORT).show()
                    isVpnActive = !isVpnActive
                    if (isVpnActive) {
                        android.widget.Toast.makeText(context, "🟢 Starting VPN...", android.widget.Toast.LENGTH_SHORT).show()
                        onStartVpn()
                    } else {
                        android.widget.Toast.makeText(context, "🔴 Stopping VPN...", android.widget.Toast.LENGTH_SHORT).show()
                        onStopVpn()
                    }
                },
                modifier = Modifier.weight(1f).height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isVpnActive) Color(0xFFCF6679) else Color(0xFF03DAC5)
                )
            ) {
                 Text(if (isVpnActive) "DEACTIVATE FIREWALL" else "ACTIVATE FIREWALL")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Live Log
        Text("LIVE TRAFFIC (${packets.size})", color = Color.Gray, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(packets) { packet ->
                PacketItem(packet)
            }
        }
    }
}

@Composable
fun PacketItem(packet: PacketEntity) {
    val color = if (packet.isRisk) Color(0xFFFF5252) else Color(0xFF03DAC5)
    
    // Content Type Badge Color
    val typeColor = when(packet.contentType) {
        "Video 4K", "Video" -> Color(0xFFE91E63) // Pink
        "Audio" -> Color(0xFF00E676) // Green
        "Image" -> Color(0xFFFFC107) // Amber
        else -> Color.Gray
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(packet.appName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("${packet.destIp} • ${packet.protocol}", color = Color.Gray, fontSize = 12.sp)
            }
            
            Column(horizontalAlignment = Alignment.End) {
                 Text(
                    text = packet.contentType, 
                    color = typeColor, 
                    fontSize = 11.sp, 
                    fontWeight = FontWeight.Bold
                )
                Text("${packet.sizeBytes / 1024} KB", color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun PrivacyPulseMap(isActive: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing)
        ),
        label = "pulse"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2
        
        // Draw Radar Circles
        drawCircle(Color.DarkGray, radius, center, style = Stroke(width = 2f))
        drawCircle(Color.DarkGray, radius * 0.6f, center, style = Stroke(width = 2f))
        drawCircle(Color.DarkGray, radius * 0.3f, center, style = Stroke(width = 2f))

        // Draw Sweep
        if (isActive) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF03DAC5).copy(alpha = 0.3f), Color.Transparent),
                    center = center,
                    radius = radius * pulse * 2 // Expanding ripple
                ),
                radius = radius,
                center = center
            )
            
            // Random connection lines
            val randomAngle = Random.nextFloat() * 6.28f
            val endX = center.x + cos(randomAngle) * radius
            val endY = center.y + sin(randomAngle) * radius
            
            drawLine(
                color = Color(0xFF03DAC5),
                start = center,
                end = Offset(endX, endY),
                strokeWidth = 2f,
                alpha = 0.5f
            )
        }
        
        // Center Device
        drawCircle(Color.White, 8.dp.toPx(), center)
    }
}
