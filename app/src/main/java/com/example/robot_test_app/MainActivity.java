package com.example.robot_test_app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.transition.AutoTransition;
import android.transition.Transition;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.transition.TransitionManager;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 1;

    TextView outputTitleTextView;
    TextView outputTextView;
    ImageButton sendButton;
    ImageButton micButton;
    EditText prompt_textbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        outputTitleTextView = findViewById(R.id.output_title_textbox);
        outputTextView = findViewById(R.id.output_textbox);
        sendButton = findViewById(R.id.tokenize_button);
        micButton = findViewById(R.id.mic_button);
        prompt_textbox = findViewById(R.id.prompt_textbox);

        // Handle window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO_PERMISSION);
        }

        // Set action listener for the prompt textbox to handle "Done" action
        prompt_textbox.setOnEditorActionListener((v, actionId, event) -> {
            if(actionId == EditorInfo.IME_ACTION_DONE ||
                    actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null) && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                sendButton.performClick();
                return true;
            }
            return false;
        });

        // Set click listener for the tokenize button
        sendButton.setOnClickListener(v -> {
            collapse_keyboard(v);
            Tokenizer tokenizer = new Tokenizer();
            String inputText = ((TextView) findViewById(R.id.prompt_textbox)).getText().toString();
            if(validate_input(inputText)) {
//                Object tokenizedOutput = tokenizer.string_to_JSON(inputText);
//                outputTextView.setText(tokenizedOutput.toString());
                outputTextView.setText(inputText);

                outputTitleTextView.setVisibility(View.VISIBLE);
                outputTextView.setVisibility(View.VISIBLE);

                ConstraintLayout root = findViewById(R.id.main);
                ConstraintSet set = new ConstraintSet();
                set.clone(root);

                set.clear(R.id.prompt_container, ConstraintSet.BOTTOM);
                set.connect(R.id.prompt_container, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
                set.connect(R.id.prompt_container, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
                set.setVerticalBias(R.id.prompt_container, 0.5f);

                Transition transition = new AutoTransition();
                transition.setDuration(600);
                TransitionManager.beginDelayedTransition(root);
                set.applyTo(root);
            }
        });

        // Set listener for the mic button
        micButton.setOnClickListener(v -> {
            Intent speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

            speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

            speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

            speechIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...");

            try{
                startActivityForResult(speechIntent, REQUEST_RECORD_AUDIO_PERMISSION);
            }catch (Exception e){
                Toast.makeText(MainActivity.this, " " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    // Handle the result from speech recognition activity
    @Override
    @Deprecated
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode,
                data);

        // Check if the result is from our speech input
        // request
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (resultCode == RESULT_OK && data != null) {
                // Get the list of results from speech
                // recognizer
                ArrayList<String> result
                        = data.getStringArrayListExtra(
                        RecognizerIntent.EXTRA_RESULTS);

                // Display the first recognized phrase in
                // the TextView
                if (result != null && !result.isEmpty()) {
//                    outputTextView.setText(Objects.requireNonNull(
//                            result.get(0)));

                    prompt_textbox.setText(Objects.requireNonNull(
                            result.get(0)));
                    sendButton.performClick();
                }
            }
        }
    }

    public boolean validate_input(String inputText) {
        if (inputText == null || inputText.trim().isEmpty()) {
            Toast.makeText(MainActivity.this, "Please enter a valid string!", Toast.LENGTH_SHORT).show();
            return false;
        } return true;
    }

    public void collapse_keyboard(View view){
        View currentFocus = this.getCurrentFocus();
        if(currentFocus != null){
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
    }
}