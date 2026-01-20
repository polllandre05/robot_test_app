package com.example.robot_test_app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.transition.AutoTransition;
import android.transition.Transition;
import android.util.Log;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 1;

    TextView outputTitleTextView;
    TextView outputTextView;
    EditText prompt_textbox;
    ImageButton sendButton;
    ImageButton micButton;
    ConstraintLayout root;
    ConstraintSet set;

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
        root = findViewById(R.id.main);
        set = new ConstraintSet();

        // Handle window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Request audio recording permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO_PERMISSION);
        }

        // Set action listener for the prompt textbox
        // Trigger send button click when "Enter" key is pressed
        registerEnterEvent();

        // Set click listener for the tokenize button
        sendButton.setOnClickListener(v -> {
            collapse_keyboard(v);
            String inputText = ((TextView) findViewById(R.id.prompt_textbox)).getText().toString();
            if(validate_input(inputText)) {
                try {
                    JSONObject jsonOutput = new JSONObject();
                    jsonOutput.put("prompt_message", inputText);

                    outputTextView.setText(jsonOutput.toString());
                } catch (JSONException e) {
                    Log.e(
                            "RobotTestApp",
                            "Failed to build JSON. inputText=\"" + inputText + "\"",
                            e
                    );
                }

                setTextboxVisibility(outputTitleTextView, View.VISIBLE);
                setTextboxVisibility(outputTextView, View.VISIBLE);

                transitionLayoutPosition();
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

    public void setTextboxVisibility(TextView textview, int visibility){
        textview.setVisibility(visibility);
    }
    // Set action listener for the prompt textbox to handle "Done" action
    public void registerEnterEvent(){
        prompt_textbox.setOnEditorActionListener((v, actionId, event) -> {
            if(actionId == EditorInfo.IME_ACTION_DONE ||
                    actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null) && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                sendButton.performClick();
                return true;
            }
            return false;
        });
    }

    // Animate the layout transition of the prompt container
    public void transitionLayoutPosition(){
        set.clone(root);
        set.setVerticalBias(R.id.prompt_container, 1.0f);

        Transition transition = new AutoTransition();
        transition.setDuration(600);
        TransitionManager.beginDelayedTransition(root);
        set.applyTo(root);
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
        return inputText != null && !inputText.trim().isEmpty();
    }

    public void collapse_keyboard(View view){
        View currentFocus = this.getCurrentFocus();
        if(currentFocus != null){
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
    }
}