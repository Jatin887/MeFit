package com.example.mefit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallback;
    private EditText inEmail;
    private Button loginButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inEmail = findViewById(R.id.inEmail);
        loginButton = findViewById(R.id.loginButton);
        progressBar = findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (inEmail.getText().toString().trim().isEmpty()) {
                    Toast.makeText(MainActivity.this, "Enter Phone Number", Toast.LENGTH_SHORT).show();
                } else if (inEmail.getText().toString().trim().length() != 10) {
                    Toast.makeText(MainActivity.this, "Invalid Phone Number", Toast.LENGTH_SHORT).show();
                } else {
                    otpSend();
                }
            }
        });
    }

    private void otpSend() {
        progressBar.setVisibility(View.VISIBLE);
        loginButton.setVisibility(View.INVISIBLE);

        mCallback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {}

            @Override
            public void onVerificationFailed(FirebaseException e) {
                progressBar.setVisibility(View.GONE);
                loginButton.setVisibility(View.VISIBLE);
                Toast.makeText(MainActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                progressBar.setVisibility(View.GONE);
                loginButton.setVisibility(View.VISIBLE);
                Intent intent = new Intent(MainActivity.this, Verify.class);
                intent.putExtra("vfId", verificationId);
                startActivity(intent);
            }
        };

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(inEmail.getText().toString().trim())
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(mCallback)
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }
}
