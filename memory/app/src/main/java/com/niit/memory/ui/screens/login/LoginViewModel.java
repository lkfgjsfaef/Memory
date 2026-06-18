package com.niit.memory.ui.screens.login;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.niit.memory.data.model.LoginResponse;
import com.niit.memory.data.repository.AuthRepository;
import com.niit.memory.util.SessionManager;
import com.niit.memory.util.TaskExecutor;

public class LoginViewModel extends AndroidViewModel {

    private static final String TAG = "LoginViewModel";
    private final AuthRepository repository;
    private final SessionManager session;

    public final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    public final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    public final MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>(false);

    public LoginViewModel(@NonNull Application application) {
        super(application);
        repository = new AuthRepository(application);
        session = SessionManager.getInstance(application);
    }

    public boolean isLoggedIn() {
        return session.isLoggedIn();
    }

    public void login(String username, String password) {
        loading.setValue(true);
        Log.d(TAG, "Attempting login for user: " + username);
        TaskExecutor.execute(() -> {
            try {
                LoginResponse resp = repository.login(username, password);
                if (resp == null || resp.getToken() == null) {
                    loading.postValue(false);
                    errorMessage.postValue("登录失败：服务器返回数据异常");
                    return;
                }
                Log.d(TAG, "Login response: userId=" + (resp.getUserId() != null ? resp.getUserId() : "null") + ", username=" + resp.getUsername());
                session.saveLogin(resp.getToken(), resp.getUserId() != null
                    ? String.valueOf(resp.getUserId()) : null,
                    resp.getUsername(), resp.getNickname(), resp.getAvatarUrl());
                Log.d(TAG, "Session saved, login success");
                loading.postValue(false);
                loginSuccess.postValue(true);
            } catch (Exception e) {
                Log.e(TAG, "Login failed: " + e.getMessage(), e);
                loading.postValue(false);
                errorMessage.postValue(e.getMessage() != null ? e.getMessage() : "登录失败");
            }
        });
    }
}
