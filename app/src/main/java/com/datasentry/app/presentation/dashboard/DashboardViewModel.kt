package com.datasentry.app.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.datasentry.app.data.repository.PacketRepository
import com.datasentry.app.domain.model.TrafficSimulator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val repository: PacketRepository
) : ViewModel() {

    // Simulator
    private val simulator = TrafficSimulator(repository)
    
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

    fun startSimulation() {
        viewModelScope.launch {
            simulator.startSimulation()
        }
    }

    fun stopSimulation() {
        simulator.stopSimulation()
    }
    
    fun clearLogs() {
        viewModelScope.launch {
            repository.clearLogs()
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
