package kr.jeet.edu.student.server;

import kr.jeet.edu.student.utils.LogMgr;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static RetrofitClient instance = null;
    private static RetrofitApi retrofitApi;

    private RetrofitClient() {

        // 로그를 보기 위한 interceptor
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();

        Retrofit retrofit = null;
        if(LogMgr.DEBUG) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(RetrofitApi.SERVER_BASE_URL + RetrofitApi.PREFIX)   // 꼭 / 로 끝나야함
                    //.baseUrl(RetrofitApi.SERVER_BASE_URL)   // 꼭 / 로 끝나야함
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client) // 로그 기능 추가
                    .build();
        } else {
            retrofit = new Retrofit.Builder()
                    .baseUrl(RetrofitApi.SERVER_BASE_URL + RetrofitApi.PREFIX)   // 꼭 / 로 끝나야함
                    //.baseUrl(RetrofitApi.SERVER_BASE_URL)   // 꼭 / 로 끝나야함
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        retrofitApi = retrofit.create(RetrofitApi.class);
    }

    public static RetrofitClient getInstance() {
        if(instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }

    public static RetrofitApi getApiInterface() {
        return retrofitApi;
    }

}
