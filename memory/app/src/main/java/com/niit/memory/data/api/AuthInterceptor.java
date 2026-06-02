package com.niit.memory.data.api;

import com.niit.memory.util.SessionManager;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private final SessionManager sessionManager;

    public AuthInterceptor(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        String token = sessionManager.getToken();
        if (token != null && !token.isEmpty()) {
            Request request = original.newBuilder()
                .header("Authorization", "Bearer " + token)
                .build();
            return chain.proceed(request);
        }
        return chain.proceed(original);
    }
}
