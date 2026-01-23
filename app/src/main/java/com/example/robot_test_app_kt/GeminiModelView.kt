package com.example.robot_test_app_kt

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import kotlinx.coroutines.launch

class GeminiModelView : ViewModel() {
    private val _aiResponse = MutableLiveData<String>()
    val aiResponse: LiveData<String> get() = _aiResponse
    val modelAI = Firebase.ai(backend = GenerativeBackend.googleAI())
        .generativeModel("gemini-2.5-flash")

    fun generateAIContent(promptString: String) {
        viewModelScope.launch {
            try {
                // 3. Ensure you handle the response correctly
                val response = modelAI.generateContent(promptString)
                _aiResponse.value = response.text ?: "No response received"
            } catch (e: Exception) {
                _aiResponse.value = "Error: ${e.localizedMessage}"
            }
        }
    }
}