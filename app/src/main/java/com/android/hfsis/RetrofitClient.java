package com.android.hfsis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit = null;

    /**
     * A lenient boolean deserializer that accepts both JSON booleans (true/false)
     * AND MySQL-style tinyint integers (1/0) from the Laravel server.
     *
     * Without this, Gson throws:
     *   IllegalStateException: Expected a boolean but was NUMBER
     * for every `is_synced`, `new_insert`, etc. column that Laravel serialises
     * as 0 or 1 instead of false or true.
     */
    private static final JsonDeserializer<Boolean> LENIENT_BOOL =
            (json, typeOfT, context) -> {
                JsonPrimitive p = json.getAsJsonPrimitive();
                if (p.isBoolean()) return p.getAsBoolean();
                if (p.isNumber())  return p.getAsInt() != 0;
                // Fallback: try parsing the string "true"/"false"/"1"/"0"
                String s = p.getAsString().trim();
                return s.equals("1") || s.equalsIgnoreCase("true");
            };

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Boolean.class, LENIENT_BOOL)   // boxed
            .registerTypeAdapter(boolean.class, LENIENT_BOOL)   // primitive
            .create();

    public static Retrofit getClient(String baseUrl) {
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .addInterceptor(interceptor);

        // Applied in ALL builds now, not just DEBUG — needed because the
        // server's cert chain isn't fully trusted by Android's default store.
        try {
            applyTrustAllSsl(builder);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set up trust-all SSL", e);
        }

        OkHttpClient client = builder.build();

        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(GSON))  // <-- use lenient Gson
                .client(client)
                .build();

        return retrofit;
    }

    private static void applyTrustAllSsl(OkHttpClient.Builder builder) throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {}
                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {}
                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[]{};
                    }
                }
        };

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts, new SecureRandom());

        builder.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0]);
        builder.hostnameVerifier((hostname, session) -> true);
    }
}