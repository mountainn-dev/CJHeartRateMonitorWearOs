package com.san.heartratemonitorwearos.view.viewmodelimpl

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.san.heartratemonitorwearos.data.Success
import com.san.heartratemonitorwearos.data.repository.HeartRateRepository
import com.san.heartratemonitorwearos.view.viewmodel.MonitoringViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MonitoringViewModelImpl(
    private val repository: HeartRateRepository
) : MonitoringViewModel, ViewModel() {
    override val viewModelError: LiveData<Boolean>
        get() = this.isViewModelError
    private val isViewModelError = MutableLiveData<Boolean>()
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

        if (result is Success) isViewModelError.postValue(false)
        else isViewModelError.postValue(true)
    }

    override fun setHeartRate(heartRate: Int) {
        lastHeartRate = heartRate
    }
}