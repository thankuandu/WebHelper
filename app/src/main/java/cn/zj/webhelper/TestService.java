package cn.zj.webhelper;


import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;

/**
 * 作者： 张卓嘉  .
 * 日期： 2018/11/7
 * 版本： V1.0
 * 说明：
 */
public interface TestService {

    @GET("today")
    Observable<ResponseBody> getToday();

}
