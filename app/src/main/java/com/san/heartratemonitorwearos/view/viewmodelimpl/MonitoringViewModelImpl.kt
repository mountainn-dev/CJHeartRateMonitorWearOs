package com.san.heartratemonitorwearos.view.viewmodelimpl

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.san.heartratemonitorwearos.data.Success
import com.san.heartratemonitorwearos.data.repository.HeartRateRepository
import com.san.heartratemonitorwearos.domain.state.UiState
import com.san.heartratemonitorwearos.view.viewmodel.MonitoringViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MonitoringViewModelImpl(
    private val repository: HeartRateRepository,
    private val userId: String
) : MonitoringViewModel, ViewModel() {
    override val state: LiveData<UiState>
        get() = viewModelState
    private val viewModelState = MutableLiveData<UiState>(UiState.Loading)
    override var workEnd = false
    private var lastHeartRate = 0

    override fun urgent(location: Location) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                receiveReport(location, lastHeartRate)
            }
        }
    }

    private suspend fun receiveReport(location: Location, lastHeartRate: Int) {
        val result = repository.urgent(location, lastHeartRate)

        if (result is Success) viewModelState.postValue(UiState.Success)
        else viewModelState.postValue(UiState.ServiceError)
    }

    override fun setHeartRate(heartRate: Int) {
        lastHeartRate = heartRate
    }

    override fun updateWorkStatus() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                updateWorkNow()
            }
        }
    }

    private suspend fun updateWorkNow() {
        val result = repository.updateWorkNow(userId)

        if (result is Success) {
            workEnd = true
            viewModelState.postValue(UiState.Success)
        } else {
            workEnd = false
            viewModelState.postValue(UiState.ServiceError)
        }
    }
}