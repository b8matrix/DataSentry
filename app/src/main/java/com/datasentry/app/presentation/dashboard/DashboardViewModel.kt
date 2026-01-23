package com.datasentry.app.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.datasentry.app.analysis.SessionAggregator
import com.datasentry.app.data.model.AppSession
import com.datasentry.app.data.repository.PacketRepository
import com.datasentry.app.domain.model.TrafficSimulator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val repository: PacketRepository
) : ViewModel() {

    // Simulator
    private val simulator = TrafficSimulator(repository)
    
    // Session Aggregator
    private val sessionAggregator = SessionAggregator(repository)
    
    // UI State
    val packets = repository.allPackets.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val riskCount = repository.riskCount.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )
    
    // Sessions state
    private val _sessions = MutableStateFlow<List<AppSession>>(emptyList())
    val sessions: StateFlow<List<AppSession>> = _sessions.asStateFlow()
    
    init {
        // Refresh sessions periodically
        startSessionRefresh()
    }
    
    private fun startSessionRefresh() {
        viewModelScope.launch {
            while (true) {
                refreshSessions()
                delay(5000) // Refresh every 5 seconds
            }
        }
    }
    
    private suspend fun refreshSessions() {
        try {
            _sessions.value = sessionAggregator.getAppSessions()
        } catch (e: Exception) {
            // Handle error silently for now
        }
    }

    fun startSimulation() {
        viewModelScope.launch {
            // simulator.startSimulation() // DISABLED: Using Real DNS Traffic
        }
    }

    fun stopSimulation() {
        simulator.stopSimulation()
    }
    
    fun clearLogs() {
        viewModelScope.launch {
            repository.clearLogs()
            refreshSessions() // Refresh sessions after clearing
        }
    }
}

class DashboardViewModelFactory(private val repository: PacketRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
