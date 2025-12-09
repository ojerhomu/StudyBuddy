package com.example.studybuddy.ui

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studybuddy.network.*
import kotlinx.coroutines.launch

class OnboardingViewModel : ViewModel() {

    // schedule state

    // map Course Name -> SubjectDetails(color + list of meeting times)
    val subjectDetailsMap = mutableStateMapOf<String, SubjectDetails>()

    val asyncSubjects = mutableStateMapOf<String, Boolean>()

    fun clearData() {
        subjectDetailsMap.clear()
        asyncSubjects.clear()
        firstName.value = null
        lastName.value = null
        educationLevel.value = null
        email.value = null
        pomodoroStudyMinutes.value = null
        pomodoroShortBreakMinutes.value = null
        pomodoroLongBreakMinutes.value = null
    }

    fun loadUserSchedule(context: Context) {
        viewModelScope.launch {
            try {
                Log.d("OnboardingViewModel", "Attempting to load user schedule...")
                val response = RetrofitInstance.getAuthApi(context).getSchedule()
                if (response.isSuccessful && response.body() != null) {
                    val scheduleResponse = response.body()!!.schedule
                    subjectDetailsMap.clear()
                    subjectDetailsMap.putAll(scheduleResponse)
                    Log.d(
                        "OnboardingViewModel",
                        "Schedule loaded successfully with ${scheduleResponse.size} subjects."
                    )
                } else {
                    Log.e(
                        "OnboardingViewModel",
                        "Failed to load schedule. Code: ${response.code()}, Message: ${response.message()}"
                    )
                }
            } catch (e: Exception) {
                Log.e("OnboardingViewModel", "Exception while loading schedule", e)
            }
        }
    }

    fun saveUserSchedule(context: Context) {
        viewModelScope.launch {
            try {
                Log.d(
                    "OnboardingViewModel",
                    "Attempting to save user schedule with ${subjectDetailsMap.size} subjects..."
                )
                val request = ApiScheduleSaveRequest(schedule = subjectDetailsMap)
                val response = RetrofitInstance.getAuthApi(context).saveSchedule(request)
                if (response.isSuccessful) {
                    Log.d("OnboardingViewModel", "Schedule saved successfully.")
                } else {
                    Log.e(
                        "OnboardingViewModel",
                        "Failed to save schedule. Code: ${response.code()}, Message: ${response.errorBody()?.string()}"
                    )
                }
            } catch (e: Exception) {
                Log.e("OnboardingViewModel", "Exception while saving schedule", e)
            }
        }
    }

    fun setSubjects(newSubjectList: List<String>) {
        val currentSubjects = subjectDetailsMap.keys.toSet()
        val newSubjects = newSubjectList.toSet()

        // remove subjects that are no longer in the list
        (currentSubjects - newSubjects).forEach {
            subjectDetailsMap.remove(it)
            asyncSubjects.remove(it)
        }

        // add new subjects with a default color + empty schedule
        (newSubjects - currentSubjects).forEach { subject ->
            val defaultColorHex = String.format("#%08X", Color.LightGray.toArgb())
            subjectDetailsMap[subject] = SubjectDetails(
                schedule = emptyList(),
                color = defaultColorHex
            )
            asyncSubjects[subject] = false
        }
    }

    fun removeSubject(subject: String) {
        subjectDetailsMap.remove(subject)
        asyncSubjects.remove(subject)
    }

    // userprofile (first_name / last_name / education_level)

    // hold what came back from GET /profile observed by compose ui
    val firstName = mutableStateOf<String?>(null)
    val lastName = mutableStateOf<String?>(null)
    val educationLevel = mutableStateOf<String?>(null)
    val email = mutableStateOf<String?>(null)
    val pomodoroStudyMinutes = mutableStateOf<Int?>(null)
    val pomodoroShortBreakMinutes = mutableStateOf<Int?>(null)
    val pomodoroLongBreakMinutes = mutableStateOf<Int?>(null)

//backend returns:
//        id": 1,
//        "email": "test@gmail.com",
//        "first_name": "Test",
//        "last_name": "User",
//        "education_level": "College

    fun loadUserProfile(context: Context) {
        viewModelScope.launch {
            try {
                Log.d("OnboardingViewModel", "Attempting to load user profile...")
                val response = RetrofitInstance.getAuthApi(context).getProfile()
                if (response.isSuccessful && response.body() != null) {
                    val profile = response.body()!!
                    firstName.value = profile.firstName
                    lastName.value = profile.lastName
                    educationLevel.value = profile.educationLevel
                    email.value = profile.email
                    pomodoroStudyMinutes.value = profile.pomodoroStudyMinutes
                    pomodoroShortBreakMinutes.value = profile.pomodoroShortBreakMinutes
                    pomodoroLongBreakMinutes.value = profile.pomodoroLongBreakMinutes
                    Log.d(
                        "OnboardingViewModel",
                        "Profile loaded. firstName=${profile.firstName}, lastName=${profile.lastName}, educationLevel=${profile.educationLevel}, email=${profile.email}"
                    )
                } else {
                    Log.e(
                        "OnboardingViewModel",
                        "Failed to load profile. Code: ${response.code()}, Message: ${response.message()}"
                    )
                }
            } catch (e: Exception) {
                Log.e("OnboardingViewModel", "Exception while loading profile", e)
            }
        }
    }
// save user's name via PATCH /profile
//backend wants json keys: first_name, last_name, education_level
    fun saveUserName(
        context: Context,
        firstName: String,
        lastName: String,
        educationLevel: String? = null
    ) {
        viewModelScope.launch {
            try {
                Log.d(
                    "OnboardingViewModel",
                    "Attempting to save user name: $firstName $lastName, educationLevel=$educationLevel"
                )

                val request = ProfileUpdateRequest(
                    firstName = firstName,
                    lastName = lastName,
                    educationLevel = educationLevel
                )

                val response = RetrofitInstance.getAuthApi(context).updateProfile(request)

                if (response.isSuccessful && response.body() != null) {
                    val updatedProfile = response.body()!!
                    // Update local state from server response to keep UI in sync
                    this@OnboardingViewModel.firstName.value = updatedProfile.firstName
                    this@OnboardingViewModel.lastName.value = updatedProfile.lastName
                    this@OnboardingViewModel.educationLevel.value = updatedProfile.educationLevel
                    this@OnboardingViewModel.email.value = updatedProfile.email

                    Log.d(
                        "OnboardingViewModel",
                        "User name saved successfully. Updated firstName=${updatedProfile.firstName}, lastName=${updatedProfile.lastName}"
                    )
                } else {
                    Log.e(
                        "OnboardingViewModel",
                        "Failed to save user name. Code: ${response.code()}, Message: ${response.errorBody()?.string()}"
                    )
                }
            } catch (e: Exception) {
                Log.e("OnboardingViewModel", "Exception while saving user name", e)
            }
        }
    }

    fun savePomodoroPreferences(context: Context, studyMinutes: Int, shortBreakMinutes: Int, longBreakMinutes: Int) {
        // update the local state
        pomodoroStudyMinutes.value = studyMinutes
        pomodoroShortBreakMinutes.value = shortBreakMinutes
        pomodoroLongBreakMinutes.value = longBreakMinutes

        viewModelScope.launch {
            try {
                val request = PomodoroPreferenceRequest(studyMinutes, shortBreakMinutes, longBreakMinutes)
                val response = RetrofitInstance.getAuthApi(context).updatePomodoroPreferences(request)
                if (response.isSuccessful && response.body() != null) {
                    //update from the server response to ensure consistency
                    val profile = response.body()!!
                    pomodoroStudyMinutes.value = profile.pomodoroStudyMinutes
                    pomodoroShortBreakMinutes.value = profile.pomodoroShortBreakMinutes
                    pomodoroLongBreakMinutes.value = profile.pomodoroLongBreakMinutes
                } else {
                    Log.e("OnboardingViewModel", "Failed to save pomodoro preferences. Code: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("OnboardingViewModel", "Exception while saving pomodoro preferences", e)
            }
        }
    }
}