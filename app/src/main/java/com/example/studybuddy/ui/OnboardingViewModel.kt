package com.example.studybuddy.ui

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studybuddy.network.*
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

class OnboardingViewModel : ViewModel() {
    val subjectDetailsMap = mutableStateMapOf<String, SubjectDetails>()
    val asyncSubjects = mutableStateMapOf<String, Boolean>()

    fun loadUserSchedule(context: Context) {
        viewModelScope.launch {
            try {
                Log.d("OnboardingViewModel", "Attempting to load user schedule...")
                val response = RetrofitInstance.getAuthApi(context).getSchedule()
                if (response.isSuccessful && response.body() != null) {
                    val scheduleResponse = response.body()!!.schedule
                    subjectDetailsMap.clear()
                    subjectDetailsMap.putAll(scheduleResponse)
                    Log.d("OnboardingViewModel", "Schedule loaded successfully with ${scheduleResponse.size} subjects.")
                } else {
                    Log.e("OnboardingViewModel", "Failed to load schedule. Code: ${response.code()}, Message: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("OnboardingViewModel", "Exception while loading schedule", e)
            }
        }
    }

    fun saveUserSchedule(context: Context) {
        viewModelScope.launch {
            try {
                Log.d("OnboardingViewModel", "Attempting to save user schedule...")
                val flatScheduleList = subjectDetailsMap.flatMap { (subjectName, details) ->
                    details.schedule.map { scheduleInfo ->
                        ApiClassSchedule(
                            course_name = subjectName,
                            day_of_week = scheduleInfo.day.name,
                            start_time = scheduleInfo.startTime,
                            end_time = scheduleInfo.endTime
                        )
                    }
                }
                val request = ApiScheduleSaveRequest(schedules = flatScheduleList)
                val response = RetrofitInstance.getAuthApi(context).saveSchedule(request)
                if (response.isSuccessful) {
                    Log.d("OnboardingViewModel", "Schedule saved successfully.")
                } else {
                    Log.e("OnboardingViewModel", "Failed to save schedule. Code: ${response.code()}, Message: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("OnboardingViewModel", "Exception while saving schedule", e)
            }
        }
    }

    // FIX: remove backend mimicking logic and revert to a default color for new subjects
    fun setSubjects(newSubjectList: List<String>) {
        val currentSubjects = subjectDetailsMap.keys.toSet()
        val newSubjects = newSubjectList.toSet()
        val subjectsToRemove = currentSubjects - newSubjects
        subjectsToRemove.forEach { subject ->
            subjectDetailsMap.remove(subject)
            asyncSubjects.remove(subject)
        }
        val subjectsToAdd = newSubjects - currentSubjects
        subjectsToAdd.forEach { subject ->
            // assign a default gray color, which the user can then change.
            val defaultColorHex = String.format("#%08X", Color.LightGray.value)
            subjectDetailsMap[subject] = SubjectDetails(emptyList(), defaultColorHex)
            asyncSubjects[subject] = false
        }
    }

    fun removeSubject(subject: String) {
        subjectDetailsMap.remove(subject)
        asyncSubjects.remove(subject)
    }

    fun saveUserName(context: Context, firstName: String, lastName: String) {

    }
}