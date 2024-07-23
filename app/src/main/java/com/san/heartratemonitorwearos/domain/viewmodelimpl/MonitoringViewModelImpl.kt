package com.san.heartratemonitorwearos.domain.viewmodelimpl

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.san.heartratemonitorwearos.data.Success
import com.san.heartratemonitorwearos.data.repository.HeartRateRepository
import com.san.heartratemonitorwearos.domain.viewmodel.MonitoringViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MonitoringViewModelImpl(
    private val repository: HeartRateRepository
) : MonitoringViewModel, ViewModel() {
    override val viewModelError: LiveData<Boolean>
        get() = this.isViewModelError
    private val isViewModelError = MutableLiveData<Boolean>()

    private val heartRateData = mutableListOf<Int>()

    override fun addHeartRateData(data: Int) {
        heartRateData.add(data)

        if (heartRateData.size >= MAX_HEART_RATE_DATA_COUNT) {
            sendHeartRateData(avgHeartRateData())
            heartRateData.clear()
        }
    }

    private fun sendHeartRateData(data: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                setHeartRateData(data)
            }
        }
    }

    private suspend fun setHeartRateData(data: Int) {
        val result = repository.setHeartRate(data)

        if (result is Success) isViewModelError.postValue(false)
        else isViewModelError.postValue(true)
    }

    private fun avgHeartRateData() = heartRateData.filter { it != 0 }.average().toInt()

    companion object {
        private const val MAX_HEART_RATE_DATA_COUNT = 60
    }
}