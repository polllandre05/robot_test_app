package com.example.robot_test_app_kt

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.robot_test_app_kt.databinding.ActivityMainBinding // Import generated class

class MainActivity : AppCompatActivity() {
    companion object {
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 1
    }
    private lateinit var binding: ActivityMainBinding
    private lateinit var geminiModel: GeminiModelView
    private val constraintSet = ConstraintSet()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        binding.outputTextbox.movementMethod = android.text.method.ScrollingMovementMethod()
        setContentView(binding.root)

        geminiModel = ViewModelProvider(this)[GeminiModelView::class.java]

        setupSystemBars()
        setupObservers()
        setupListeners()
        registerEnterEvent()
        binding.outputTextbox.scrollTo(0, 0)
    }

    private fun setupObservers() {
        // Observes the "Thinking..." animation or the "Response:" header
        geminiModel.titleText.observe(this) { status ->
            binding.outputTitleTextbox.text = status
        }

        // Observes the actual AI answer
        geminiModel.aiResponse.observe(this) { responseText ->
            binding.outputTextbox.text = responseText
        }
    }

    private fun setupListeners() {
        binding.tokenizeButton.setOnClickListener {
            handleSendAction()
        }

        binding.micButton.setOnClickListener {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            }
            try {
                startActivityForResult(intent, REQUEST_RECORD_AUDIO_PERMISSION)
            } catch (e: Exception) {
                Toast.makeText(this, "Speech recognition not available", Toast.LENGTH_SHORT).show()
            }
        }

        binding.promptTextbox.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                handleSendAction()
                true
            } else false
        }
    }

    private fun handleSendAction() {
        val inputText = binding.promptTextbox.text.toString()

        if (inputText.isNotBlank()) {
            hideKeyboard()

            binding.outputTitleTextbox.visibility = View.VISIBLE
            binding.outputTextbox.visibility = View.VISIBLE

            geminiModel.generateAIContent(inputText)

            binding.main.animateBias(constraintSet, R.id.prompt_container, 1.0f)
        }
    }

    private fun registerEnterEvent() {
        binding.promptTextbox.setOnEditorActionListener { _, actionId, event ->
            // Check for specific IME actions OR the physical Enter key press
            val isKeyboardDone = actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH
            val isPhysicalEnter = event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN

            if (isKeyboardDone || isPhysicalEnter) {
                handleSendAction() // Call the logic directly instead of performClick()
                true // Consume the event
            } else {
                false // Pass the event on
            }
        }
    }

    private fun setupSystemBars() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}