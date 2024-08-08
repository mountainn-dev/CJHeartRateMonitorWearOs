package com.san.heartratemonitorwearos.view.viewmodelfactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.san.heartratemonitorwearos.data.repository.HeartRateRepository
import com.san.heartratemonitorwearos.view.viewmodelimpl.MonitoringViewModelImpl

class MonitoringViewModelFactory(
    private val repository: HeartRateRepository,
    private val userId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MonitoringViewModelImpl::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MonitoringViewModelImpl(repository, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}