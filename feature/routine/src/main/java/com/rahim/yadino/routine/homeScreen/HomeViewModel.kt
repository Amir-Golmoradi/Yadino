package com.rahim.yadino.routine.homeScreen

import androidx.lifecycle.viewModelScope
import com.rahim.yadino.base.viewmodel.BaseViewModel
import com.rahim.yadino.routine.RepositoryRoutine
import com.rahim.yadino.base.Resource
import com.rahim.yadino.base.enums.error.ErrorMessageCode
import com.rahim.yadino.routine.useCase.AddReminderUseCase
import com.rahim.yadino.base.db.model.RoutineModel
import com.rahim.yadino.sharedPreferences.SharedPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val routineRepository: RepositoryRoutine,
    private val addReminderUseCase: AddReminderUseCase,
    private val sharedPreferencesRepository: SharedPreferencesRepository
) :
    BaseViewModel() {

    private var _flowRoutines = MutableStateFlow<Resource<List<RoutineModel>>>(Resource.Loading())
    val flowRoutines: StateFlow<Resource<List<RoutineModel>>> = _flowRoutines

    private val _addRoutine =
        MutableStateFlow<Resource<Nothing?>?>(null)
    val addRoutine = _addRoutine

    private val _updateRoutineModel =
        MutableStateFlow<Resource<RoutineModel?>?>(null)
    val updateRoutine = _updateRoutineModel


    init {
        getCurrentRoutines()
    }

    private fun getCurrentRoutines() {
        viewModelScope.launch {
            routineRepository.getRoutines(currentMonth, currentDay, currentYear)
                .catch { _flowRoutines.value = Resource.Error(ErrorMessageCode.ERROR_GET_PROCESS) }
                .collect {
                    _flowRoutines.value = Resource.Success(it.sortedBy {
                        it.timeHours?.replace(":", "")?.toInt()
                    })
                }
        }
    }

    fun deleteRoutine(routineModel: RoutineModel) {
        viewModelScope.launch {
            routineRepository.removeRoutine(routineModel)
        }
    }

    fun updateRoutine(routineModel: RoutineModel) {
        viewModelScope.launch {
            routineRepository.updateRoutine(routineModel).catch {}.collectLatest {
                _updateRoutineModel.value = it
            }
        }
    }

    fun checkedRoutine(routineModel: RoutineModel) {
        viewModelScope.launch {
            routineRepository.checkedRoutine(routineModel)
        }
    }

    fun addRoutine(routineModel: RoutineModel) {
        viewModelScope.launch {
            addReminderUseCase(routineModel).catch {
                _addRoutine.value = Resource.Error(ErrorMessageCode.ERROR_SAVE_PROSES)
            }.collect {
                Timber.tag("routineAdd")
                    .d("view model ->${if (it is Resource.Success) "success" else if (it is Resource.Error) "fail" else "loading"}")
                _addRoutine.value = it
            }
        }
    }

    fun clearAddRoutine() {
        _addRoutine.value = null
    }

    fun clearUpdateRoutine() {
        _updateRoutineModel.value = null
    }

    fun searchItems(searchText: String) {
        viewModelScope.launch {
            if (searchText.isNotEmpty()) {
                _flowRoutines.value = Resource.Loading()
                Timber.tag("searchRoutine").d("searchText:$searchText")
                routineRepository.searchRoutine(searchText, currentMonth, currentDay).catch {
                    _flowRoutines.value = Resource.Error(ErrorMessageCode.ERROR_GET_PROCESS)
                }.collect {
                    if (it.isNotEmpty()) {
                        val firstRoutine = it.first()
                        if (firstRoutine.dayNumber == currentDay && firstRoutine.yerNumber == currentYear && firstRoutine.monthNumber == currentMonth)
                            _flowRoutines.value =
                                Resource.Success(
                                    it.sortedBy {
                                        it.timeHours?.replace(":", "")?.toInt()
                                    })
                    } else {
                        _flowRoutines.value = Resource.Success(emptyList())
                    }
                }
            } else {
                getCurrentRoutines()
            }
        }
    }
    fun showSampleRoutine(isShow: Boolean = true) {
        viewModelScope.launch {
            sharedPreferencesRepository.isShowSampleRoutine(isShow)
        }
    }

}
