package com.chatslau.notification;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAF6jlw4I:APA91bGPLsomy0i4Kkniw7Pe_py6RHxH15VXs98diZtJL4-dS4O5Ez_4FhP0GlxMkwvJMYaAOIZXn2zOm0l6XzsvzhOq7iSgaI2ccL6UhbRXJ_JYe9xc0IV-JuxEARDmNcvWx-iBz1VK"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
