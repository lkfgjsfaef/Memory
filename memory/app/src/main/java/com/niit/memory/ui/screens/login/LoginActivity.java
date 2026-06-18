package com.niit.memory.ui.screens.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import coil.Coil;
import coil.request.ImageRequest;
import com.google.android.material.card.MaterialCardView;
import com.niit.memory.MainActivity;
import com.niit.memory.R;
import com.niit.memory.data.api.ApiClient;
import com.niit.memory.data.api.AuthService;
import com.niit.memory.data.model.ApiResponse;
import com.niit.memory.data.model.AvatarsInfo;
import com.niit.memory.databinding.ActivityLoginBinding;
import com.niit.memory.util.ColorConstants;
import com.niit.memory.util.SessionManager;
import com.niit.memory.util.TaskExecutor;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private LoginViewModel viewModel;
    private String selectedUser = "jiangjiang";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        if (viewModel.isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setupUserCards();
        loadCachedAvatars();
        observeViewModel();

        binding.loginButton.setOnClickListener(v -> {
            String password = binding.passwordInput.getText() != null
                ? binding.passwordInput.getText().toString() : "";
            if (password.isEmpty()) {
                binding.loginError.setText("请输入密码");
                binding.loginError.setVisibility(View.VISIBLE);
                return;
            }
            binding.loginError.setVisibility(View.GONE);
            viewModel.login(selectedUser, password);
        });
    }

    private void setupUserCards() {
        // Default: select jiangjiang
        updateCardSelection(true);

        binding.userCardHis.setOnClickListener(v -> {
            selectedUser = "jiangjiang";
            updateCardSelection(true);
        });
        binding.userCardHer.setOnClickListener(v -> {
            selectedUser = "feifei";
            updateCardSelection(false);
        });
    }

    private void updateCardSelection(boolean isHis) {
        int selectedStrokeColor = ColorConstants.CARD_SELECTED_STROKE;
        int selectedStrokeWidth = 3;
        int defaultStrokeColor = ColorConstants.CARD_DEFAULT_STROKE;
        int defaultStrokeWidth = 1;

        MaterialCardView hisCard = binding.userCardHis;
        MaterialCardView herCard = binding.userCardHer;

        if (isHis) {
            hisCard.setStrokeColor(selectedStrokeColor);
            hisCard.setStrokeWidth(selectedStrokeWidth);
            herCard.setStrokeColor(defaultStrokeColor);
            herCard.setStrokeWidth(defaultStrokeWidth);
        } else {
            herCard.setStrokeColor(selectedStrokeColor);
            herCard.setStrokeWidth(selectedStrokeWidth);
            hisCard.setStrokeColor(defaultStrokeColor);
            hisCard.setStrokeWidth(defaultStrokeWidth);
        }
    }

    private void loadCachedAvatars() {
        SessionManager sm = SessionManager.getInstance(this);
        TaskExecutor.execute(() -> {
            try {
                AuthService authService = ApiClient.getInstance(this).create(AuthService.class);
                retrofit2.Response<ApiResponse<AvatarsInfo>> resp = authService.getAvatars().execute();
                if (resp.isSuccessful() && resp.body() != null && resp.body().isSuccess()) {
                    AvatarsInfo info = resp.body().getData();
                    if (info != null) {
                        sm.saveHisAvatarUrl(info.getHisAvatarUrl());
                        sm.saveHerAvatarUrl(info.getHerAvatarUrl());
                        runOnUiThread(() -> {
                            loadAvatarImage(binding.userAvatarHis, info.getHisAvatarUrl());
                            loadAvatarImage(binding.userAvatarHer, info.getHerAvatarUrl());
                        });
                        return;
                    }
                }
            } catch (Exception e) {
                android.util.Log.w("LoginActivity", "Failed to load avatars from server, using cache", e);
            }
            // Fallback to local cache (works after first successful API call)
            runOnUiThread(() -> {
                loadAvatarImage(binding.userAvatarHis, sm.getHisAvatarUrl());
                loadAvatarImage(binding.userAvatarHer, sm.getHerAvatarUrl());
            });
        });
    }

    private void loadAvatarImage(android.widget.ImageView imageView, String url) {
        if (url != null && !url.isEmpty()) {
            ImageRequest req = new ImageRequest.Builder(this)
                .data(url)
                .target(imageView)
                .placeholder(R.drawable.ic_avatar_default)
                .error(R.drawable.ic_avatar_default)
                .build();
            Coil.imageLoader(this).enqueue(req);
        }
    }

    private void observeViewModel() {
        viewModel.loading.observe(this, loading -> {
            binding.loginProgress.setVisibility(loading != null && loading ? View.VISIBLE : View.GONE);
            binding.loginButton.setEnabled(loading == null || !loading);
        });

        viewModel.errorMessage.observe(this, msg -> {
            if (msg != null) {
                binding.loginError.setText(msg);
                binding.loginError.setVisibility(View.VISIBLE);
            }
        });

        viewModel.loginSuccess.observe(this, success -> {
            if (success != null && success) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        });
    }
}
