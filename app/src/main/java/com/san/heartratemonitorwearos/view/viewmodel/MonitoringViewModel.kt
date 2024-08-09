package com.san.heartratemonitorwearos.view.viewmodel

import android.location.Location
import androidx.lifecycle.LiveData
import com.san.heartratemonitorwearos.domain.state.UiState

interface MonitoringViewModel {
    val state: LiveData<UiState>
    var workEnd: Boolean

    fun urgent(location: Location)
    fun setHeartRate(heartRate: Int)
    fun updateWorkStatus()
}