package id.jefrydco.botcoll.utils;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class NetworkUtils {
    public static final String API_URL = "http://10.0.2.2:8080/botcoll/";

    public NetworkUtils() {
    }

    public static Object fetch(Class<?> service) {
        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();

        Retrofit.Builder retrofitBuilder = new Retrofit.Builder()
                .baseUrl(API_URL)
                .addConverterFactory(ScalarsConverterFactory.create());

        Retrofit retrofit = retrofitBuilder.client(okHttpClientBuilder.build()).build();

        return retrofit.create(service);
    }
}
