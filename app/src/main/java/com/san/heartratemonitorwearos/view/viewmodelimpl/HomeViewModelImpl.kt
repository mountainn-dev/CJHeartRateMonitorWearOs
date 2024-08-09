package com.san.heartratemonitorwearos.view.viewmodelimpl

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.san.heartratemonitorwearos.data.Success
import com.san.heartratemonitorwearos.data.repository.HeartRateRepository
import com.san.heartratemonitorwearos.domain.state.UiState
import com.san.heartratemonitorwearos.view.viewmodel.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModelImpl(
    private val repository: HeartRateRepository
) : HomeViewModel, ViewModel() {
    override val state: LiveData<UiState>
        get() = viewModelState
    private val viewModelState = MutableLiveData<UiState>(UiState.Loading)
    override var idToken = NO_DATA
    override var userId = NO_DATA


    override fun setUserData(idToken: String, userId: String) {
        this.idToken = idToken
        this.userId = userId
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
            if (userDataReady()) viewModelState.postValue(UiState.Success)
            else viewModelState.postValue(UiState.ServiceError)
        }
        else viewModelState.postValue(UiState.ServiceError)
    }

    private fun userDataReady() = idToken != NO_DATA && userId != NO_DATA

    companion object {
        private const val NO_DATA = ""
    }
}