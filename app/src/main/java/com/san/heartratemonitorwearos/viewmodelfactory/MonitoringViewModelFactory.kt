package com.san.heartratemonitorwearos.viewmodelfactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.san.heartratemonitorwearos.data.repository.HeartRateRepository
import com.san.heartratemonitorwearos.viewmodelimpl.MonitoringViewModelImpl

class MonitoringViewModelFactory(
    private val repository: HeartRateRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MonitoringViewModelImpl::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MonitoringViewModelImpl(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}