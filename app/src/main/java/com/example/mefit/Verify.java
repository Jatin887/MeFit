package com.example.mefit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class Verify extends AppCompatActivity {

    private String verificationId;
    private EditText etC1, etC2, etC3, etC4, etC5, etC6;
    private Button verifyButton;
    private TextView tvResendBtn;
    private ProgressBar progressBarVerify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);

        etC1 = findViewById(R.id.etC1);
        etC2 = findViewById(R.id.etC2);
        etC3 = findViewById(R.id.etC3);
        etC4 = findViewById(R.id.etC4);
        etC5 = findViewById(R.id.etC5);
        etC6 = findViewById(R.id.etC6);
        verifyButton = findViewById(R.id.verifyButton);
        tvResendBtn = findViewById(R.id.tvResendBtn);
        progressBarVerify = findViewById(R.id.progressBarVerify);

        verificationId = getIntent().getStringExtra("vfId");
        editTextInput();

        tvResendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(Verify.this,"OTP sent Successfully", Toast.LENGTH_SHORT).show();
            }
        });

        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBarVerify.setVisibility(View.VISIBLE);
                verifyButton.setVisibility(View.INVISIBLE);
                if (etC1.getText().toString().trim().isEmpty() ||
                        etC2.getText().toString().trim().isEmpty() ||
                        etC3.getText().toString().trim().isEmpty() ||
                        etC4.getText().toString().trim().isEmpty() ||
                        etC5.getText().toString().trim().isEmpty() ||
                        etC6.getText().toString().trim().isEmpty()) {

                    Toast.makeText(Verify.this, "OTP is not Valid", Toast.LENGTH_SHORT).show();
                } else if (verificationId != null) {
                    String code = etC1.getText().toString().trim() +
                            etC2.getText().toString().trim() +
                            etC3.getText().toString().trim() +
                            etC4.getText().toString().trim() +
                            etC5.getText().toString().trim() +
                            etC6.getText().toString().trim();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
                    FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                progressBarVerify.setVisibility(View.VISIBLE);
                                verifyButton.setVisibility(View.INVISIBLE);
                                Intent intent = new Intent(Verify.this, CalorieGain.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            } else {
                                progressBarVerify.setVisibility(View.GONE);
                                verifyButton.setVisibility(View.VISIBLE);
                                Toast.makeText(Verify.this, "OTP is not Valid", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    private void editTextInput() {
        // Same logic here... you might want to consider refactoring this for brevity
        // ... (rest of your editTextInput method)
    }
}
