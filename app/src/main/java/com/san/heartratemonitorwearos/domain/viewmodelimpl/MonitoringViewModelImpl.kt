package com.san.heartratemonitorwearos.domain.viewmodelimpl

import android.location.Location
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

    override fun urgent(location: Location) {
        // TODO: urgent
    }
}