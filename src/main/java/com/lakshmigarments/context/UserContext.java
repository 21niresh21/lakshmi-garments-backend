package com.lakshmigarments.context;

public class UserContext {

    private static final ThreadLocal<UserInfo> USER = new ThreadLocal<>();

    public static void set(UserInfo userInfo) {
        USER.set(userInfo);
    }

    public static UserInfo get() {
        return USER.get();
    }

    public static void clear() {
        USER.remove();
    }
}
