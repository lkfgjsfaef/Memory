package com.niit.memory.config;

public class UserContext {
    private static final ThreadLocal<Long> currentUserId = new ThreadLocal<>();
    private static final ThreadLocal<String> currentUsername = new ThreadLocal<>();

    public static void set(Long userId, String username) {
        currentUserId.set(userId);
        currentUsername.set(username);
    }

    public static Long getUserId() {
        return currentUserId.get();
    }

    public static String getUsername() {
        return currentUsername.get();
    }

    public static void clear() {
        currentUserId.remove();
        currentUsername.remove();
    }
}
