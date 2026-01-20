package com.example.robot_test_app_kt

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.transition.AutoTransition
import android.transition.Transition
import android.transition.TransitionManager
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.json.JSONException
import org.json.JSONObject
import java.util.Locale

class MainActivity : AppCompatActivity() {
    companion object {
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 1;
        private const val TAG = "RobotTestApp";
    }

    private lateinit var outputTitleTextView: TextView
    private lateinit var outputTextView: TextView
    private lateinit var promptTextbox: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var micButton: ImageButton
    private lateinit var root: ConstraintLayout
    private lateinit var constraintSet: ConstraintSet

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // UI references
        outputTitleTextView = findViewById(R.id.output_title_textbox)
        outputTextView = findViewById(R.id.output_textbox)
        sendButton = findViewById(R.id.tokenize_button)
        micButton = findViewById(R.id.mic_button)
        promptTextbox = findViewById(R.id.prompt_textbox)
        root = findViewById(R.id.main)
        constraintSet = ConstraintSet()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        requestAudioPermission()
        registerEnterEvent()

        sendButton.setOnClickListener {
            collapseKeyboard()
            val inputText = promptTextbox.text.toString()

            if (validateInput(inputText)) {
                try {
                    val jsonOutput = JSONObject().apply {
                        put("prompt_message", inputText)
                    }
                    outputTextView.text = jsonOutput.toString()
                } catch (e: JSONException) {
                    Log.e(TAG, "Failed to build JSON. inputText=\"$inputText\"", e)
                }

                setTextboxVisibility(outputTitleTextView, View.VISIBLE)
                setTextboxVisibility(outputTextView, View.VISIBLE)
                transitionLayoutPosition()
            }
        }

        micButton.setOnClickListener {
            val speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")
            }

            try {
                startActivityForResult(speechIntent, REQUEST_RECORD_AUDIO_PERMISSION)
            } catch (e: Exception) {
                Toast.makeText(this, e.message ?: "Speech error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun requestAudioPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_RECORD_AUDIO_PERMISSION
            )
        }
    }

    private fun setTextboxVisibility(view: TextView, visibility: Int) {
        view.visibility = visibility
    }

    private fun registerEnterEvent() {
        promptTextbox.setOnEditorActionListener { _, actionId, event ->
            if (
                actionId == EditorInfo.IME_ACTION_DONE ||
                actionId == EditorInfo.IME_ACTION_SEARCH ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER)
            ) {
                sendButton.performClick()
                true
            } else {
                false
            }
        }
    }

    private fun transitionLayoutPosition() {
        constraintSet.clone(root)
        constraintSet.setVerticalBias(R.id.prompt_container, 1.0f)

        val transition: Transition = AutoTransition().apply {
            duration = 600
        }

        TransitionManager.beginDelayedTransition(root, transition)
        constraintSet.applyTo(root)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        @Nullable data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION &&
            resultCode == RESULT_OK &&
            data != null
        ) {
            val results = data.getStringArrayListExtra(
                RecognizerIntent.EXTRA_RESULTS
            )

            if (!results.isNullOrEmpty()) {
                promptTextbox.setText(results[0])
                sendButton.performClick()
            }
        }
    }

    private fun validateInput(inputText: String?): Boolean {
        return !inputText.isNullOrBlank()
    }

    private fun collapseKeyboard() {
        val currentFocus = currentFocus ?: return
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus.windowToken, 0)
    }
}