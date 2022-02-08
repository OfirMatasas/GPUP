package http;

import okhttp3.*;

public class HttpClientUtil {

    private final static OkHttpClient HTTP_CLIENT =
            new OkHttpClient.Builder()
                    .followRedirects(false)
                    .build();

    public static void runAsync(String finalUrl, String method, RequestBody body, Callback callback) {
        Request request = new Request.Builder()
                .url(finalUrl).method(method, body)
                .build();

        HttpClientUtil.HTTP_CLIENT.newCall(request).enqueue(callback);
    }

    public static void runAsyncWithEmptyBody(String finalUrl, String method, Callback callback) {
        Request request = new Request.Builder()
                .url(finalUrl).method(method, RequestBody.create("Empty body", MediaType.parse("Empty body")))
                .build();

        HttpClientUtil.HTTP_CLIENT.newCall(request).enqueue(callback);
    }

    public static void runAsyncWithRequest(Request request, Callback callback) {
        HttpClientUtil.HTTP_CLIENT.newCall(request).enqueue(callback);
    }

    public static void shutdown() {
        System.out.println("Shutting down HTTP CLIENT");
        HTTP_CLIENT.dispatcher().executorService().shutdown();
        HTTP_CLIENT.connectionPool().evictAll();
    }
}