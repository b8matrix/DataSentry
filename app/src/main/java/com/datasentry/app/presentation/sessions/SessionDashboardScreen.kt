package com.datasentry.app.presentation.sessions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.datasentry.app.data.model.AppSession

@Composable
fun SessionBasedDashboard(
    sessions: List<AppSession>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "APP SESSIONS",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            // Session count badge
            Surface(
                color = Color(0xFF03DAC5).copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    "${sessions.size}",
                    color = Color(0xFF03DAC5),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            "Analyzing app behavior patterns and privacy risks",
            color = Color.Gray,
            fontSize = 13.sp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Sessions list
        if (sessions.isEmpty()) {
            EmptySessionsView()
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(sessions) { session ->
                    SessionCard(session)
                }
            }
        }
    }
}

@Composable
fun SessionCard(session: AppSession) {
    var expanded by remember { mutableStateOf(false) }
    
    val impactColor = when (session.privacyImpact) {
        AppSession.PrivacyImpact.HIGH -> Color(0xFFFF5252)
        AppSession.PrivacyImpact.MEDIUM -> Color(0xFFFFC107)
        AppSession.PrivacyImpact.LOW -> Color(0xFF00E676)
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        onClick = { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        session.appName,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${session.getTimeAgo()} • ${session.getFormattedDuration()}",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
                
                // Privacy Impact Badge
                Surface(
                    color = impactColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        session.privacyImpact.toDisplayString(),
                        color = impactColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Quick Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                QuickStat(
                    label = "Packets",
                    value = "${session.packetCount}",
                    color = Color(0xFF03DAC5)
                )
                
                QuickStat(
                    label = "Domains", 
                    value = "${session.domains.size}",
                    color = Color(0xFFBB86FC)
                )
                
                QuickStat(
                    label = "Trackers",
                    value = "${session.trackers.size}",
                    color = if (session.trackers.isEmpty()) Color(0xFF00E676) else Color(0xFFFF5252)
                )
            }
            
            // Expandable Details
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Divider(color = Color.Gray.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Traffic Breakdown
                    Text(
                        "Traffic Analysis",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    session.packetSizeBreakdown.forEach { (type, count) ->
                        val percentage = (count * 100) / session.packetCount
                        TrafficBreakdownItem(type, percentage, impactColor)
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Connection Pattern
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF2A2A2A), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF03DAC5),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                "Connection Pattern",
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                            Text(
                                session.connectionPattern.toDisplayString(),
                                color = Color.White,
                                fontSize = 13.sp
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Domains
                    Text(
                        "Domains Accessed (${session.domains.size})",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    session.domains.take(5).forEach { domain ->
                        val isTracker = session.trackers.contains(domain)
                        DomainItem(domain, isTracker)
                    }
                    
                    if (session.domains.size > 5) {
                        Text(
                            "+ ${session.domains.size - 5} more domains",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(start = 24.dp, top = 4.dp)
                        )
                    }
                    
                    // Tracker Alert
                    if (session.trackers.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Surface(
                            color = Color(0xFFFF5252).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFFF5252),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        "Privacy Alert",
                                        color = Color(0xFFFF5252),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "${session.trackers.size} tracking ${if (session.trackers.size == 1) "domain" else "domains"} detected",
                                        color = Color.White,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Expand indicator
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                if (expanded) "Tap to collapse" else "Tap for details",
                color = Color.Gray,
                fontSize = 11.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun QuickStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            color = color,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            label,
            color = Color.Gray,
            fontSize = 11.sp
        )
    }
}

@Composable
fun TrafficBreakdownItem(type: String, percentage: Int, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.7f))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            type,
            color = Color.LightGray,
            fontSize = 13.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            "$percentage%",
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun DomainItem(domain: String, isTracker: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            if (isTracker) "⚠️" else "✅",
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                domain,
                color = if (isTracker) Color(0xFFFF5252) else Color.LightGray,
                fontSize = 12.sp
            )
            if (isTracker) {
                Text(
                    "Tracking/Analytics domain",
                    color = Color(0xFFFF5252).copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

@Composable
fun EmptySessionsView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "🔍",
            fontSize = 64.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "No Sessions Yet",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Use some apps and return to see\nintelligent privacy analysis",
            color = Color.Gray,
            fontSize = 14.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
