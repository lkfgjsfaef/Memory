package com.niit.memory.data.model;

import java.io.IOException;
import retrofit2.Response;

public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;

    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    public boolean isSuccess() { return code == 200; }

    public static <T> T parseResponse(Response<ApiResponse<T>> resp) throws Exception {
        if (!resp.isSuccessful()) throw new IOException("Network error");
        ApiResponse<T> body = resp.body();
        if (body == null || !body.isSuccess()) throw new Exception(body.getMessage() != null ? body.getMessage() : "服务器返回数据异常");
        return body.getData();
    }

    public static void validateResponse(Response<ApiResponse<Void>> resp) throws Exception {
        if (!resp.isSuccessful()) throw new IOException("Network error");
        ApiResponse<Void> body = resp.body();
        if (body == null || !body.isSuccess()) throw new Exception(body.getMessage() != null ? body.getMessage() : "服务器返回数据异常");
    }
}
