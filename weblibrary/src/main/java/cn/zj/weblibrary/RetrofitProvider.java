package cn.zj.weblibrary;

import android.content.res.Resources;
import android.support.v4.content.res.ResourcesCompat;
import android.text.TextUtils;

import com.androidnetworking.interceptors.HttpLoggingInterceptor;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 作者： zhangzhuojia
 * 日期： 2018/11/6
 * 版本： V1.0
 * 说明：
 */
public class RetrofitProvider {
    private static Retrofit retrofit;
    private static String baseUrl;

    public static void setBaseUrl(String baseUrl) {
        RetrofitProvider.baseUrl = baseUrl;
    }


    public static Retrofit getInstance() {
        if (TextUtils.isEmpty(baseUrl)) {
            throw new IllegalArgumentException(Resources.getSystem().getString(R.string.there_is_no_base_url));
        }
        if (retrofit == null) {
            synchronized (RetrofitProvider.class) {
                if (retrofit == null) {
                    OkHttpClient.Builder builder = new OkHttpClient.Builder();
                    builder.readTimeout(20, TimeUnit.SECONDS);
                    builder.writeTimeout(20, TimeUnit.SECONDS);


                    HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
                    loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                    builder.addInterceptor(loggingInterceptor);


                    retrofit = new Retrofit.Builder()
                            .baseUrl(baseUrl)
                            .client(builder.build())
                            .addConverterFactory(GsonConverterFactory.create())
                            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                            .build();
                }
            }
        }
        return retrofit;
    }
}
