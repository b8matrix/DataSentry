package com.datasentry.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import com.datasentry.app.data.local.AppDatabase
import com.datasentry.app.data.model.FlowStats
import com.datasentry.app.data.model.RiskLevel
import com.datasentry.app.data.model.SuspiciousEvent
import com.datasentry.app.data.local.entity.PacketEntity
import com.datasentry.app.data.repository.FlowStatsRepositoryImpl
import com.datasentry.app.data.repository.PacketRepository
import com.datasentry.app.data.repository.SuspiciousEventRepositoryImpl
import com.datasentry.app.demo.DemoScenarioEngine
import com.datasentry.app.inspector.TrafficInspector
import com.datasentry.app.vpn.DnsOnlyHandler
import com.datasentry.app.vpn.VpnPacketHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.FileInputStream
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

class DataSentryService : VpnService() {

    companion object {
        const val ACTION_STOP = "com.datasentry.app.STOP_VPN"
        const val LOG_EVENT = "com.datasentry.app.VPN_LOG_EVENT"
        const val STATS_EVENT = "com.datasentry.app.VPN_STATS_EVENT"
        const val EXTRA_SUSPICIOUS_COUNT = "suspiciousCount"
        const val EXTRA_LAST_ALERT = "lastAlert"
        
        private const val CHANNEL_ID = "vpn_channel"
        private const val NOTIFICATION_ID = 1
        private const val TAG = "DataSentryVPN"
        
        // Set to true for REAL packet capture, false for demo simulation
        // Toggle this for semi-finals demo!
        const val REAL_MODE = true
    }

    private var vpnInterface: ParcelFileDescriptor? = null
    private var isRunning = false
    private var tunReaderThread: Thread? = null
    private val stopSignal = AtomicBoolean(false)
    
    // Real mode packet handler
    private var packetHandler: VpnPacketHandler? = null
    private var dnsHandler: DnsOnlyHandler? = null
    
    private val statsLock = Any()
    private var suspiciousCount: Int = 0
    private var lastAlert: String? = null
    private var lastStatsBroadcastMs: Long = 0L
    
    // Traffic analysis
    private val trafficInspector = TrafficInspector()
    
    // Repositories
    private lateinit var flowStatsRepository: FlowStatsRepositoryImpl
    private lateinit var suspiciousEventRepository: SuspiciousEventRepositoryImpl
    private lateinit var packetRepository: PacketRepository
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    // ================= START / STOP =================

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        
        // Initialize repositories
        val db = AppDatabase.getDatabase(this)
        flowStatsRepository = FlowStatsRepositoryImpl(db.flowStatsDao())
        suspiciousEventRepository = SuspiciousEventRepositoryImpl(db.suspiciousEventDao())
        packetRepository = PacketRepository(db.packetDao())
        
        // ---- STOP MONITORING ----
        if (intent?.action == ACTION_STOP) {
            broadcastLog("User stopped monitoring")
            stopVpn()
            return START_NOT_STICKY
        }

        // ---- START MONITORING ----
        startForeground(NOTIFICATION_ID, createNotification())
        startVpn()
        broadcastLog("User started monitoring")

        return START_STICKY
    }

    // ================= VPN SETUP =================

    private fun startVpn() {
        Log.e(TAG, "=== startVpn() CALLED ===")  // Using Log.e so it definitely shows
        
        try {
            stopSignal.set(false)
            
            synchronized(statsLock) {
                suspiciousCount = 0
                lastAlert = null
            }
            lastStatsBroadcastMs = 0L
            broadcastStats()

            Log.e(TAG, "Building VPN interface, REAL_MODE=$REAL_MODE")
            
            val builder = Builder()
                .setSession("DataSentry Traffic Monitor")
                .addAddress("10.0.0.2", 32)
                .setMtu(1500)
                .setBlocking(false)
            
            if (REAL_MODE) {
                Log.e(TAG, "Adding DNS routes...")
                // DNS-ONLY MODE: Route DNS server IPs through VPN
                builder.addRoute("8.8.8.8", 32)   // Google DNS
                builder.addRoute("8.8.4.4", 32)   // Google DNS secondary
                builder.addRoute("1.1.1.1", 32)   // Cloudflare DNS
                builder.addRoute("1.0.0.1", 32)   // Cloudflare DNS secondary
                
                // FORCE device to use these DNS servers (this is key!)
                // This makes DNS queries go TO 8.8.8.8 which we're routing through VPN
                builder.addDnsServer("8.8.8.8")
                builder.addDnsServer("8.8.4.4")
                
                // Also allow the VPN to access the internet
                builder.allowFamily(android.system.OsConstants.AF_INET)
            }

            Log.e(TAG, "Calling builder.establish()...")
            vpnInterface = builder.establish()
            Log.e(TAG, "VPN established, vpnInterface=${vpnInterface != null}")
            
            isRunning = true

            if (REAL_MODE) {
                Log.e(TAG, "Starting DnsOnlyHandler...")
                broadcastLog("🛡️ DataSentry Active - Monitoring DNS")
                
                vpnInterface?.let { pfd ->
                    Log.e(TAG, "Creating DnsOnlyHandler with pfd=$pfd")
                    dnsHandler = DnsOnlyHandler(this, pfd, packetRepository)
                    dnsHandler?.start()
                    Log.e(TAG, "DnsOnlyHandler started!")
                } ?: run {
                    Log.e(TAG, "ERROR: vpnInterface is null!")
                }
            } else {
                Log.e(TAG, "DEMO MODE - starting simulation")
                broadcastLog("🛡️ DataSentry Protection Active (Demo)")
                startDemoSimulation()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to start VPN: ${e.message}", e)
            broadcastLog("VPN failed to start: ${e.message}")
            stopSelf()
        }
    }

    // ================= DEMO SIMULATION =================
    
    private var demoThread: Thread? = null
    
    /**
     * Starts a timer-based demo simulation that inserts fake traffic data into the database.
     * The UI observes the database via Flow, so packets appear in the list automatically.
     */
    private fun startDemoSimulation() {
        demoThread?.interrupt()
        demoThread = Thread {
            try {
                val scenarios = DemoScenarioEngine.getAllScenarios()
                var index = 0
                
                // Initial delay - wait 5 seconds before first log
                Thread.sleep(5000)
                
                while (!stopSignal.get() && !Thread.currentThread().isInterrupted) {
                    val scenario = scenarios[index % scenarios.size]
                    
                    // Get app name from package
                    val appName = when (scenario.packageName) {
                        "com.google.android.youtube" -> "YouTube"
                        "com.snapchat.android" -> "Snapchat"
                        "com.android.chrome" -> "Chrome"
                        "android" -> "System"
                        else -> "Unknown"
                    }
                    
                    // Get content type from traffic type
                    val contentType = when (scenario.trafficType) {
                        "VIDEO_STREAM_4K" -> "Video 4K"
                        "EPHEMERAL_MEDIA" -> "Image"
                        "WEB_NAVIGATION" -> "Text"
                        "BACKGROUND_TELEMETRY" -> "Telemetry"
                        else -> "Data"
                    }
                    
                    // Get destination IP from server description
                    val destIp = when (scenario.packageName) {
                        "com.google.android.youtube" -> "172.217.${(1..255).random()}.${(1..255).random()}"
                        "com.snapchat.android" -> "52.${(1..255).random()}.${(1..255).random()}.${(1..255).random()}"
                        "com.android.chrome" -> "104.16.${(1..255).random()}.${(1..255).random()}"
                        else -> "142.251.${(1..255).random()}.${(1..255).random()}"
                    }
                    
                    // Generate size based on content type
                    val sizeBytes = when (contentType) {
                        "Video 4K" -> (500000..1500000).random()
                        "Image" -> (50000..200000).random()
                        "Text" -> (1000..10000).random()
                        else -> (100..5000).random()
                    }
                    
                    // Create packet entity
                    val packet = PacketEntity(
                        timestamp = System.currentTimeMillis(),
                        sourceIp = "192.168.1.${(1..255).random()}",
                        destIp = destIp,
                        protocol = if (scenario.trafficType == "VIDEO_STREAM_4K") "UDP" else "TCP",
                        sizeBytes = sizeBytes,
                        appName = appName,
                        contentType = contentType,
                        isRisk = scenario.riskScore > 30
                    )
                    
                    // Insert into database (using runBlocking since we're on a thread)
                    kotlinx.coroutines.runBlocking {
                        packetRepository.logPacket(packet)
                    }
                    
                    // Also broadcast log for debugging
                    val riskEmoji = if (scenario.riskScore <= 30) "✅" else "⚠️"
                    broadcastLog("$riskEmoji $appName: ${contentType} (${sizeBytes/1024}KB)")
                    
                    Log.d(TAG, "Demo packet inserted: $appName")
                    
                    // Increment and wait
                    index++
                    
                    // Random delay between 1.5-3 seconds
                    val delay = (1500L..3000L).random()
                    Thread.sleep(delay)
                }
            } catch (e: InterruptedException) {
                Log.d(TAG, "Demo simulation interrupted")
            } catch (t: Throwable) {
                Log.e(TAG, "Demo simulation error", t)
            }
        }.apply {
            name = "DataSentryDemoSimulation"
            isDaemon = true
            start()
        }
        
        Log.d(TAG, "Demo simulation started")
    }

    private fun startTunReader() {
        val pfd = vpnInterface ?: return
        tunReaderThread?.interrupt()
        tunReaderThread = Thread {
            try {
                FileInputStream(pfd.fileDescriptor).use { input ->
                    val buffer = ByteArray(32767)
                    var lastDemoLogTime = 0L
                    
                    while (!stopSignal.get() && !Thread.currentThread().isInterrupted) {
                        val len = input.read(buffer)
                        if (len <= 0) continue
                        
                        // Inspect packet with TrafficInspector
                        trafficInspector.inspect(buffer.copyOf(len), 0) // UID 0 for now
                        
                        // Extract destination IP for demo scenario matching
                        val destIp = extractDestIp(buffer, len)
                        val demoScenario = if (destIp != null) {
                            DemoScenarioEngine.getScenarioByIp(destIp)
                        } else null
                        
                        if (demoScenario != null) {
                            // Throttle demo logs to avoid spam (max once per 2 seconds per scenario)
                            val now = System.currentTimeMillis()
                            if (now - lastDemoLogTime > 2000) {
                                lastDemoLogTime = now
                                val demoLog = "📊 ${demoScenario.trafficType} | ${demoScenario.server} | Risk: ${demoScenario.riskScore}/100 (${demoScenario.riskLabel})"
                                broadcastLog(demoLog)
                                Log.d(TAG, "Demo scenario triggered: ${demoScenario.packageName}")
                            }
                        } else {
                            // Fallback to real packet summary
                            val summary = summarizePacket(buffer, len)
                            if (summary != null) {
                                broadcastLog(summary)
                            }
                        }
                        
                        // Periodically save flows to database
                        if (System.currentTimeMillis() % 5000 < 100) {
                            saveFlowsToDatabase()
                        }
                    }
                }
            } catch (t: Throwable) {
                if (!stopSignal.get()) {
                    Log.e(TAG, "TUN reader stopped unexpectedly", t)
                    broadcastLog("TUN reader stopped: ${t.javaClass.simpleName}")
                }
            }
        }.apply {
            name = "DataSentryTunReader"
            isDaemon = true
            start()
        }
    }

    /**
     * Extract destination IP from an IPv4 packet for demo scenario matching.
     */
    private fun extractDestIp(buf: ByteArray, len: Int): String? {
        if (len < 20) return null
        val versionIhl = buf[0].toInt() and 0xFF
        val version = versionIhl ushr 4
        if (version != 4) return null
        return ipv4(buf, 16) // Destination IP is at offset 16
    }

    private fun summarizePacket(buf: ByteArray, len: Int): String? {
        if (len < 20) return null
        val versionIhl = buf[0].toInt() and 0xFF
        val version = versionIhl ushr 4
        if (version != 4) return "Packet v$version len=$len"
        val ihl = (versionIhl and 0x0F) * 4
        if (ihl < 20 || len < ihl) return null
        val protocol = buf[9].toInt() and 0xFF
        val src = ipv4(buf, 12)
        val dst = ipv4(buf, 16)

        return when (protocol) {
            6 -> {  // TCP
                if (len < ihl + 4) return "TCP $src -> $dst len=$len"
                val srcPort = u16(buf, ihl)
                val dstPort = u16(buf, ihl + 2)
                val line = "TCP $src:$srcPort -> $dst:$dstPort len=$len"
                maybeRecordSuspicious(protocol = "TCP", src = src, dst = dst, srcPort = srcPort, dstPort = dstPort)
                line
            }
            17 -> {  // UDP
                if (len < ihl + 4) return "UDP $src -> $dst len=$len"
                val srcPort = u16(buf, ihl)
                val dstPort = u16(buf, ihl + 2)
                val line = "UDP $src:$srcPort -> $dst:$dstPort len=$len"
                maybeRecordSuspicious(protocol = "UDP", src = src, dst = dst, srcPort = srcPort, dstPort = dstPort)
                line
            }
            1 -> "ICMP $src -> $dst len=$len"
            else -> "IP proto=$protocol $src -> $dst len=$len"
        }
    }

    private fun maybeRecordSuspicious(
        protocol: String,
        src: String,
        dst: String,
        srcPort: Int,
        dstPort: Int
    ) {
        val reason = suspiciousReason(protocol = protocol, dstPort = dstPort) ?: return
        val alert = String.format(
            Locale.US,
            "%s %s:%d -> %s:%d (%s)",
            protocol,
            src,
            srcPort,
            dst,
            dstPort,
            reason
        )

        synchronized(statsLock) {
            suspiciousCount += 1
            lastAlert = alert
        }
        
        // Save to database
        serviceScope.launch {
            val event = SuspiciousEvent(
                appUid = 0,  // TODO: Extract UID from packet
                trafficType = com.datasentry.app.data.model.TrafficType.UNKNOWN,
                riskLevel = getRiskLevel(dstPort),
                totalBytes = 0,
                reason = reason,
                timestamp = System.currentTimeMillis(),
                protocol = protocol,
                destinationPort = dstPort
            )
            suspiciousEventRepository.save(event)
        }
        
        broadcastStatsThrottled()
    }

    private fun suspiciousReason(protocol: String, dstPort: Int): String? {
        return when (dstPort) {
            21 -> "FTP"
            23 -> "TELNET"
            25, 465, 587 -> "SMTP"
            53 -> "DNS"
            80 -> "HTTP (cleartext)"
            110 -> "POP3"
            143 -> "IMAP"
            445 -> "SMB"
            3389 -> "RDP"
            5900 -> "VNC"
            6667 -> "IRC"
            1883 -> "MQTT"
            else -> {
                if (protocol == "TCP" && dstPort in 1..1023) "Low privileged port" else null
            }
        }
    }
    
    private fun getRiskLevel(dstPort: Int): RiskLevel {
        return when (dstPort) {
            21, 23, 3389, 5900 -> RiskLevel.CRITICAL
            80, 445 -> RiskLevel.HIGH
            25, 465, 587, 110, 143 -> RiskLevel.MEDIUM
            else -> RiskLevel.LOW
        }
    }

    private fun saveFlowsToDatabase() {
        serviceScope.launch {
            val flows = trafficInspector.getFlows()
            flows.forEach { flow ->
                flowStatsRepository.save(flow)
            }
        }
    }

    private fun broadcastStatsThrottled() {
        val now = System.currentTimeMillis()
        if (now - lastStatsBroadcastMs < 750L) return
        lastStatsBroadcastMs = now
        broadcastStats()
    }

    private fun broadcastStats() {
        val (count, alert) = synchronized(statsLock) {
            suspiciousCount to lastAlert
        }
        val intent = Intent(STATS_EVENT)
        intent.putExtra(EXTRA_SUSPICIOUS_COUNT, count)
        intent.putExtra(EXTRA_LAST_ALERT, alert)
        intent.setPackage(packageName)
        sendBroadcast(intent)
    }

    private fun ipv4(buf: ByteArray, off: Int): String {
        if (off + 3 >= buf.size) return "0.0.0.0"
        val b0 = buf[off].toInt() and 0xFF
        val b1 = buf[off + 1].toInt() and 0xFF
        val b2 = buf[off + 2].toInt() and 0xFF
        val b3 = buf[off + 3].toInt() and 0xFF
        return "$b0.$b1.$b2.$b3"
    }

    private fun u16(buf: ByteArray, off: Int): Int {
        val hi = buf[off].toInt() and 0xFF
        val lo = buf[off + 1].toInt() and 0xFF
        return (hi shl 8) or lo
    }

    private fun stopVpn() {
        isRunning = false
        stopSignal.set(true)

        // Stop real mode packet handler
        try {
            packetHandler?.stop()
        } catch (_: Exception) {}
        packetHandler = null

        // Stop DNS handler
        try {
            dnsHandler?.stop()
        } catch (_: Exception) {}
        dnsHandler = null

        try {
            demoThread?.interrupt()
        } catch (_: Exception) {}
        demoThread = null

        try {
            tunReaderThread?.interrupt()
        } catch (_: Exception) {}
        tunReaderThread = null

        try {
            vpnInterface?.close()
        } catch (_: Exception) {}

        vpnInterface = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    // ================= LOG BROADCAST =================

    private fun broadcastLog(message: String) {
        val intent = Intent(LOG_EVENT)
        intent.putExtra("log", message)
        intent.setPackage(packageName)
        sendBroadcast(intent)
    }

    // ================= NOTIFICATION =================

    private fun createNotification(): Notification {
        
        val channel = NotificationChannel(
            CHANNEL_ID,
            "DataSentry Monitoring",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Network traffic monitoring service"
        }
        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("DataSentry Protection Active")
            .setContentText("Monitoring all network traffic")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    // ================= CLEANUP =================

    override fun onDestroy() {
        stopVpn()
        super.onDestroy()
    }

    override fun onRevoke() {
        stopVpn()
        super.onRevoke()
    }
}
