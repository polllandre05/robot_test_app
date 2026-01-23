package com.example.robot_test_app_kt

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import kotlinx.coroutines.launch

//class GeminiModelView : ViewModel() {
//    private val _aiResponse = MutableLiveData<String>()
//    val aiResponse: LiveData<String> get() = _aiResponse
//
//    // Initialize the animator with a callback to LiveData
//    private val animator = ThinkingAnimator { currentText ->
//        _aiResponse.value = currentText
//    }
//
//
//
//    fun generateAIContent(promptString: String) {
//        viewModelScope.launch {
//            try {
//                // Simulate AI Thinking in UI
//                animator.start()
//                val response = modelAI.generateContent(promptString)
//                animator.stop()
//
//                _aiResponse.value = response.text ?: "No response"
//
//            } catch (e: Exception) {
//                animator.stop()
//                _aiResponse.value = "Error: ${e.localizedMessage}"
//            }
//        }
//    }
//
//    override fun onCleared() {
//        super.onCleared()
//        animator.stop()
//    }
//}
class GeminiModelView : ViewModel() {
    private val _aiResponse = MutableLiveData<String>()
    val aiResponse: LiveData<String> get() = _aiResponse

    // New LiveData for the title
    private val _titleText = MutableLiveData<String>()
    val titleText: LiveData<String> get() = _titleText

    // Initialize Animator to update the title text
    private val animator = ThinkingAnimator { currentStatus ->
        _titleText.value = currentStatus
    }

    val modelAI = Firebase.ai(backend = GenerativeBackend.googleAI())
        .generativeModel("gemini-2.5-flash")

    fun generateAIContent(promptString: String) {
        viewModelScope.launch {
            try {
                _aiResponse.value = ""
                // Simulate AI Thinking
                animator.start()
                val response = modelAI.generateContent(promptString)
                animator.stop()
                // Response Generated
                _titleText.value = "Response:"
                _aiResponse.value = response.text ?: "No response"
            } catch (e: Exception) {
                animator.stop()
                _titleText.value = "Error"
                _aiResponse.value = e.localizedMessage
            }
        }
    }
}