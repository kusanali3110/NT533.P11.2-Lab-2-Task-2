package com.example.openstack_mobileinteraction;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.openstack_mobileinteraction.API.Identifier;

import java.net.MalformedURLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginScreen extends AppCompatActivity {

    private AlertDialog loadingDialog;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.login_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        executorService = Executors.newFixedThreadPool(3);

        Button loginButton = findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView usernameTextView = findViewById(R.id.username_textField);
                TextView passwordTextView = findViewById(R.id.password_textFied);
                TextView domainTextView = findViewById(R.id.domain_textField);
                if (TextUtils.isEmpty(usernameTextView.getText()) || TextUtils.isEmpty(passwordTextView.getText()) || TextUtils.isEmpty(domainTextView.getText()))
                    showRequireFieldsDialog();
                else {
                    String username = usernameTextView.getText().toString();
                    String password = passwordTextView.getText().toString();
                    String domain = domainTextView.getText().toString();
                    Login(username, password, domain);
                }
            }
        });
    }

    public void Login(String username, String password, String domain) {
        showLoadingDialog();

        CompletableFuture.supplyAsync(() -> {
           Identifier identifier = new Identifier();
           String token = null;

           try {
               token = identifier.getToken(username, password, domain);
           } catch (MalformedURLException | InterruptedException | ExecutionException e) {
               e.printStackTrace();
           }
           return token;
        }, executorService).thenAccept(token -> {
            runOnUiThread(() -> {
                hideLoadingDialog();
                if (token != null) {
                    navigateToMainInteraction(token);
                } else {
                    showFailureDialog();
                }
            });
        }).exceptionally(ex -> {
            runOnUiThread(() -> {
                hideLoadingDialog();
                showFailureDialog();
            });
            return null;
        });
    }

    private void showLoadingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logging in");
        builder.setMessage("Please wait while we log you in...");
        builder.setCancelable(false);

        loadingDialog = builder.create();
        loadingDialog.show();
    }
    private void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    private void navigateToMainInteraction(String token) {
        Intent intent = new Intent(LoginScreen.this, MainInteractionScreen.class);
        intent.putExtra("token", token);
        startActivity(intent);
        finish();
    }

    private void showFailureDialog() {
        new AlertDialog.Builder(LoginScreen.this)
                .setTitle("Error")
                .setMessage("Failed to login. Please try again.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showRequireFieldsDialog() {
        new AlertDialog.Builder(LoginScreen.this)
                .setTitle("Error")
                .setMessage("Please provide username, password and domain.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

}