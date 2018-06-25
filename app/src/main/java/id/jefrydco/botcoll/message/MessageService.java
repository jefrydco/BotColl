package id.jefrydco.botcoll.message;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface MessageService {

    @FormUrlEncoded
    @POST("api/apiUser/apiUser.php?apicall=get_history")
    Call<String> getHistory(@Field("keyword") String keyword);
}
