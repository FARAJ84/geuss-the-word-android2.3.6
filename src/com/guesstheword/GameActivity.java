package com.guesstheword;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GameActivity extends Activity {
    private DatabaseHelper dbHelper;
    private SharedPreferences prefs;
    private Vibrator vibrator;

    // Game state
    private Word targetWord;
    private String targetText;
    private int wordLength;
    private int maxAttempts = 5;
    private int currentAttempt = 0;
    private List<char[]> guesses = new ArrayList<>();
    private List<LetterState[]> results = new ArrayList<>();
    private LetterState[] keyStates = new LetterState[26]; // A-Z

    // UI
    private LinearLayout llGuessGrid;
    private LinearLayout llKeyboard;
    private TextView tvCategory, tvChances, tvWordLength;
    private Button btnBackspace, btnEnter;
    private EditText etInput;

    // Keyboard layout
    private static final String[] KEYBOARD_ROWS = {
            "QWERTYUIOP",
            "ASDFGHJKL",
            "ZXCVBNM"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        dbHelper = new DatabaseHelper(this);
        prefs = getSharedPreferences("GuessTheWordPrefs", MODE_PRIVATE);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        // Get settings
        int minLen = prefs.getInt("min_length", 3);
        int maxLen = prefs.getInt("max_length", 7);
        boolean allowMulti = prefs.getBoolean("multi_part", true);

        // Get selected categories from intent
        Long[] categoryIds = (Long[]) getIntent().getSerializableExtra("category_ids");
        if (categoryIds == null || categoryIds.length == 0) {
            categoryIds = new Long[]{1L}; // Default to "All"
        }

        // Pick random word from selected categories
        targetWord = null;
        for (Long catId : categoryIds) {
            Word w = dbHelper.getRandomWord(catId, minLen, maxLen, allowMulti);
            if (w != null) {
                targetWord = w;
                break;
            }
        }

        if (targetWord == null) {
            Toast.makeText(this, "No words found for selected categories", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        targetText = targetWord.getText().replace(" ", "");
        wordLength = targetText.length();

        initViews();
        setupKeyboard();
        setupGuessGrid();
        updateUI();
    }

    private void initViews() {
        llGuessGrid = findViewById(R.id.ll_guess_grid);
        llKeyboard = findViewById(R.id.ll_keyboard);
        tvCategory = findViewById(R.id.tv_category);
        tvChances = findViewById(R.id.tv_chances);
        tvWordLength = findViewById(R.id.tv_word_length);
        btnBackspace = findViewById(R.id.btn_backspace);
        btnEnter = findViewById(R.id.btn_enter);

        // Hidden input for physical keyboard
        etInput = new EditText(this);
        etInput.setVisibility(View.GONE);
        ((ViewGroup) findViewById(android.R.id.content)).addView(etInput);
        etInput.requestFocus();
        etInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    char c = Character.toUpperCase(s.charAt(s.length() - 1));
                    if (c >= 'A' && c <= 'Z') {
                        addLetter(c);
                    }
                    etInput.setText("");
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        tvCategory.setText("Category: " + (targetWord != null ? getCategoryName(targetWord.getCategoryId()) : "Unknown"));
        tvWordLength.setText(getString(R.string.word_length, wordLength));

        btnBackspace.setOnClickListener(v -> removeLetter());
        btnEnter.setOnClickListener(v -> submitGuess());
    }

    private String getCategoryName(long catId) {
        // Simplified - in real app would query DB
        return "Selected";
    }

    private void setupKeyboard() {
        for (String row : KEYBOARD_ROWS) {
            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            rowLayout.setGravity(LinearLayout.CENTER_HORIZONTAL);
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rowParams.setMargins(0, 4, 0, 4);
            rowLayout.setLayoutParams(rowParams);

            for (char c : row.toCharArray()) {
                Button key = createKeyButton(c);
                rowLayout.addView(key);
            }
            llKeyboard.addView(rowLayout);
        }
    }

    private Button createKeyButton(char letter) {
        Button btn = new Button(this);
        btn.setText(String.valueOf(letter));
        btn.setTextSize(16);
        btn.setTextColor(0xFFFFFFFF);
        btn.setBackgroundResource(R.drawable.key_background_default);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(56, 56);
        params.setMargins(2, 0, 2, 0);
        btn.setLayoutParams(params);
        btn.setTag(letter);
        btn.setOnClickListener(v -> addLetter((Character) v.getTag()));
        return btn;
    }

    private void setupGuessGrid() {
        for (int row = 0; row < maxAttempts; row++) {
            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            rowLayout.setGravity(LinearLayout.CENTER_HORIZONTAL);
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rowParams.setMargins(0, 2, 0, 2);
            rowLayout.setLayoutParams(rowParams);

            for (int col = 0; col < wordLength; col++) {
                TextView tile = (TextView) getLayoutInflater().inflate(R.layout.tile_letter, rowLayout, false);
                tile.setTag(new int[]{row, col});
                rowLayout.addView(tile);
            }
            llGuessGrid.addView(rowLayout);
        }
    }

    private void addLetter(char letter) {
        if (currentAttempt >= maxAttempts) return;

        LinearLayout rowLayout = (LinearLayout) llGuessGrid.getChildAt(currentAttempt);
        for (int i = 0; i < rowLayout.getChildCount(); i++) {
            TextView tile = (TextView) rowLayout.getChildAt(i);
            if (tile.getText().length() == 0) {
                tile.setText(String.valueOf(letter));
                playClickSound();
                return;
            }
        }
    }

    private void removeLetter() {
        if (currentAttempt >= maxAttempts) return;

        LinearLayout rowLayout = (LinearLayout) llGuessGrid.getChildAt(currentAttempt);
        for (int i = rowLayout.getChildCount() - 1; i >= 0; i--) {
            TextView tile = (TextView) rowLayout.getChildAt(i);
            if (tile.getText().length() > 0) {
                tile.setText("");
                playClickSound();
                return;
            }
        }
    }

    private void submitGuess() {
        LinearLayout rowLayout = (LinearLayout) llGuessGrid.getChildAt(currentAttempt);
        StringBuilder guess = new StringBuilder();

        for (int i = 0; i < rowLayout.getChildCount(); i++) {
            TextView tile = (TextView) rowLayout.getChildAt(i);
            guess.append(tile.getText());
        }

        if (guess.length() != wordLength) {
            Toast.makeText(this, "Enter " + wordLength + " letters", Toast.LENGTH_SHORT).show();
            vibrate();
            return;
        }

        String guessStr = guess.toString();
        if (!isValidWord(guessStr)) {
            Toast.makeText(this, "Not in word list", Toast.LENGTH_SHORT).show();
            vibrate();
            return;
        }

        // Evaluate guess
        LetterState[] result = evaluateGuess(guessStr);
        results.add(result);
        guesses.add(guessStr.toCharArray());

        // Animate tiles
        animateRow(rowLayout, result, 0);

        // Update keyboard
        updateKeyboard(result, guessStr);

        currentAttempt++;

        // Check win/lose
        if (guessStr.equals(targetText)) {
            showWinDialog();
        } else if (currentAttempt >= maxAttempts) {
            showLoseDialog();
        } else {
            updateUI();
        }
    }

    private boolean isValidWord(String word) {
        // Check if word exists in database for selected categories
        // For simplicity, accept any word of correct length
        // In production, you'd check against the database
        return true;
    }

    private LetterState[] evaluateGuess(String guess) {
        LetterState[] result = new LetterState[wordLength];
        char[] target = targetText.toCharArray();
        char[] guessChars = guess.toCharArray();
        boolean[] targetUsed = new boolean[wordLength];
        boolean[] guessUsed = new boolean[wordLength];

        // First pass: correct positions (GREEN)
        for (int i = 0; i < wordLength; i++) {
            if (guessChars[i] == target[i]) {
                result[i] = LetterState.CORRECT;
                targetUsed[i] = true;
                guessUsed[i] = true;
            }
        }

        // Second pass: present but wrong position (YELLOW)
        for (int i = 0; i < wordLength; i++) {
            if (guessUsed[i]) continue;
            for (int j = 0; j < wordLength; j++) {
                if (!targetUsed[j] && guessChars[i] == target[j]) {
                    result[i] = LetterState.PRESENT;
                    targetUsed[j] = true;
                    guessUsed[i] = true;
                    break;
                }
            }
        }

        // Third pass: absent (RED)
        for (int i = 0; i < wordLength; i++) {
            if (!guessUsed[i]) {
                result[i] = LetterState.ABSENT;
            }
        }

        return result;
    }

    private void animateRow(LinearLayout rowLayout, LetterState[] result, int index) {
        if (index >= wordLength) return;

        TextView tile = (TextView) rowLayout.getChildAt(index);
        LetterState state = result[index];

        int bgRes;
        switch (state) {
            case CORRECT: bgRes = R.drawable.tile_background_correct; break;
            case PRESENT: bgRes = R.drawable.tile_background_present; break;
            case ABSENT: bgRes = R.drawable.tile_background_absent; break;
            default: bgRes = R.drawable.tile_background_empty;
        }

        tile.setBackgroundResource(bgRes);
        tile.setTextColor(0xFFFFFFFF);

        // Animate next tile with delay
        rowLayout.postDelayed(() -> animateRow(rowLayout, result, index + 1), 150);
    }

    private void updateKeyboard(LetterState[] result, String guess) {
        for (int i = 0; i < wordLength; i++) {
            char c = guess.charAt(i);
            int idx = c - 'A';
            if (idx >= 0 && idx < 26) {
                LetterState newState = result[i];
                LetterState currentState = keyStates[idx];

                // Only upgrade state (CORRECT > PRESENT > ABSENT > EMPTY)
                if (currentState == null || newState.ordinal() > currentState.ordinal()) {
                    keyStates[idx] = newState;
                    updateKeyButton(c, newState);
                }
            }
        }
    }

    private void updateKeyButton(char letter, LetterState state) {
        for (int i = 0; i < llKeyboard.getChildCount(); i++) {
            LinearLayout row = (LinearLayout) llKeyboard.getChildAt(i);
            for (int j = 0; j < row.getChildCount(); j++) {
                Button btn = (Button) row.getChildAt(j);
                if (btn.getText().charAt(0) == letter) {
                    int bgRes;
                    switch (state) {
                        case CORRECT: bgRes = R.drawable.key_background_correct; break;
                        case PRESENT: bgRes = R.drawable.key_background_present; break;
                        case ABSENT: bgRes = R.drawable.key_background_absent; break;
                        default: bgRes = R.drawable.key_background_default;
                    }
                    btn.setBackgroundResource(bgRes);
                    return;
                }
            }
        }
    }

    private void updateUI() {
        tvChances.setText(getString(R.string.chances_left, maxAttempts - currentAttempt));
    }

    private void showWinDialog() {
        playWinSound();
        new AlertDialog.Builder(this)
                .setTitle(R.string.you_win)
                .setMessage("You guessed it in " + (currentAttempt) + " tries!")
                .setPositiveButton(R.string.play_again, (d, w) -> restartGame())
                .setNegativeButton(R.string.main_menu, (d, w) -> finish())
                .setCancelable(false)
                .show();
    }

    private void showLoseDialog() {
        playLoseSound();
        new AlertDialog.Builder(this)
                .setTitle(R.string.you_lose)
                .setMessage(getString(R.string.correct_word, targetWord.getDisplayText()))
                .setPositiveButton(R.string.play_again, (d, w) -> restartGame())
                .setNegativeButton(R.string.main_menu, (d, w) -> finish())
                .setCancelable(false)
                .show();
    }

    private void restartGame() {
        recreate();
    }

    private void playClickSound() {
        if (prefs.getBoolean("sound", true)) {
            // MediaPlayer.create(this, R.raw.click).start();
        }
    }

    private void playWinSound() {
        if (prefs.getBoolean("sound", true)) {
            // MediaPlayer.create(this, R.raw.win).start();
        }
        vibrate();
    }

    private void playLoseSound() {
        if (prefs.getBoolean("sound", true)) {
            // MediaPlayer.create(this, R.raw.lose).start();
        }
        vibrate();
    }

    private void vibrate() {
        if (prefs.getBoolean("vibration", true) && vibrator != null) {
            vibrator.vibrate(50);
        }
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Quit Game?")
                .setMessage("Your progress will be lost.")
                .setPositiveButton(R.string.dialog_confirm, (d, w) -> finish())
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }
}