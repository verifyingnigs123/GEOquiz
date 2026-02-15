package com.example.geoquiz;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class QuizActivity extends AppCompatActivity {
    private static final String TAG = "QuizActivity";
    private static final String KEY_INDEX = "index";
    private static final String KEY_ANSWERED = "answered";
    private static final String KEY_CHEATS_USED = "cheats_used";
    private static final String KEY_SCORE = "score";
    private static final int REQUEST_CODE_CHEAT = 0;
    private static final int MAX_CHEATS = 3;

    private Button mTrueButton;
    private Button mFalseButton;
    private Button mNextButton;
    private Button mPreviousButton;
    private Button mCheatButton;
    private TextView mQuestionTextView;
    private TextView mApiLevelTextView;
    private TextView mCheatCountTextView;
    
    private Question[] mQuestionBank = new Question[]{
            new Question(R.string.question_australia, true),
            new Question(R.string.question_oceans, true),
            new Question(R.string.question_mideast, false),
            new Question(R.string.question_africa, false),
            new Question(R.string.question_americas, true),
            new Question(R.string.question_asia, true),
    };
    
    private int mCurrentIndex = 0;
    private boolean mIsCheater;
    private boolean[] mAnswered;
    private int mCheatsUsed = 0;
    private int mScore = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate(Bundle) called");
        setContentView(R.layout.activity_quiz);

        // Initialize mAnswered array first
        if (mAnswered == null) {
            mAnswered = new boolean[mQuestionBank.length];
        }

        // Restore state if available
        if (savedInstanceState != null) {
            mCurrentIndex = savedInstanceState.getInt(KEY_INDEX, 0);
            mIsCheater = savedInstanceState.getBoolean("cheater", false);
            boolean[] saved = savedInstanceState.getBooleanArray(KEY_ANSWERED);
            if (saved != null) {
                mAnswered = saved;
            }
            mCheatsUsed = savedInstanceState.getInt(KEY_CHEATS_USED, 0);
            mScore = savedInstanceState.getInt(KEY_SCORE, 0);
        }

        // Get view references
        try {
            mQuestionTextView = (TextView) findViewById(R.id.question_text_view);
            mApiLevelTextView = (TextView) findViewById(R.id.api_level_text_view);
            mCheatCountTextView = (TextView) findViewById(R.id.cheat_count_text_view);
            mTrueButton = (Button) findViewById(R.id.true_button);
            mFalseButton = (Button) findViewById(R.id.false_button);
            mNextButton = (Button) findViewById(R.id.next_button);
            mPreviousButton = (Button) findViewById(R.id.previous_button);
            mCheatButton = (Button) findViewById(R.id.cheat_button);
        } catch (Exception e) {
            Log.e(TAG, "Error getting views: " + e.getMessage());
            return;
        }

        displayApiLevel();
        updateQuestion();

        // Set up button listeners
        mTrueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(true);
            }
        });

        mFalseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(false);
            }
        });

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentIndex = (mCurrentIndex + 1) % mQuestionBank.length;
                mIsCheater = false;
                updateQuestion();
            }
        });

        mPreviousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentIndex = (mCurrentIndex - 1 + mQuestionBank.length) % mQuestionBank.length;
                mIsCheater = false;
                updateQuestion();
            }
        });

        updateCheatButton();
        mCheatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCheatsUsed < MAX_CHEATS) {
                    boolean answerIsTrue = mQuestionBank[mCurrentIndex].isAnswerTrue();
                    Intent intent = CheatActivity.newIntent(QuizActivity.this, answerIsTrue);
                    startActivityForResult(intent, REQUEST_CODE_CHEAT);
                } else {
                    Toast.makeText(QuizActivity.this, "No cheats remaining!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void displayApiLevel() {
        int apiLevel = Build.VERSION.SDK_INT;
        String apiLevelText = getString(R.string.api_level) + apiLevel;
        mApiLevelTextView.setText(apiLevelText);
    }

    private void updateCheatButton() {
        int cheatsLeft = MAX_CHEATS - mCheatsUsed;
        if (cheatsLeft > 0) {
            mCheatButton.setEnabled(true);
            String cheatText = getString(R.string.cheat_button) + " (" + cheatsLeft + ")";
            mCheatButton.setText(cheatText);
        } else {
            mCheatButton.setEnabled(false);
            mCheatButton.setText(getString(R.string.cheat_button) + " (0)");
        }
        String cheatCountText = String.format(getString(R.string.cheats_remaining), cheatsLeft);
        mCheatCountTextView.setText(cheatCountText);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() called");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() called");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() called");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop() called");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() called");
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        Log.i(TAG, "onSaveInstanceState");
        savedInstanceState.putInt(KEY_INDEX, mCurrentIndex);
        savedInstanceState.putBoolean("cheater", mIsCheater);
        savedInstanceState.putBooleanArray(KEY_ANSWERED, mAnswered);
        savedInstanceState.putInt(KEY_CHEATS_USED, mCheatsUsed);
        savedInstanceState.putInt(KEY_SCORE, mScore);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_CODE_CHEAT) {
            if (data == null) {
                return;
            }
            mIsCheater = CheatActivity.wasAnswerShown(data);
            if (mIsCheater) {
                mCheatsUsed++;
                updateCheatButton();
            }
        }
    }

    private void updateQuestion() {
        int question = mQuestionBank[mCurrentIndex].getTextResId();
        mQuestionTextView.setText(question);
        updateButtonStates();
    }

    private void updateButtonStates() {
        if (mAnswered == null || mCurrentIndex >= mAnswered.length) {
            mTrueButton.setEnabled(true);
            mFalseButton.setEnabled(true);
            return;
        }
        boolean isAnswered = mAnswered[mCurrentIndex];
        mTrueButton.setEnabled(!isAnswered);
        mFalseButton.setEnabled(!isAnswered);
    }

    private void checkAnswer(boolean userPressedTrue) {
        if (mAnswered == null) {
            mAnswered = new boolean[mQuestionBank.length];
        }
        
        boolean answerIsTrue = mQuestionBank[mCurrentIndex].isAnswerTrue();
        int messageResId = 0;

        mAnswered[mCurrentIndex] = true;
        updateButtonStates();

        if (mIsCheater) {
            messageResId = R.string.judgment_toast;
        } else {
            if (userPressedTrue == answerIsTrue) {
                messageResId = R.string.correct_toast;
                mScore++;
            } else {
                messageResId = R.string.incorrect_toast;
            }
        }
        Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show();

        checkQuizComplete();
    }

    private void checkQuizComplete() {
        boolean allAnswered = true;
        for (boolean answered : mAnswered) {
            if (!answered) {
                allAnswered = false;
                break;
            }
        }

        if (allAnswered) {
            int percentage = (mScore * 100) / mQuestionBank.length;
            String scoreMessage = String.format(getString(R.string.score_format), percentage);
            Toast.makeText(this, scoreMessage, Toast.LENGTH_LONG).show();
        }
    }
}
