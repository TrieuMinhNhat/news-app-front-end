package com.example.myapplication.service.apiService


import retrofit2.Response
import com.example.myapplication.data.DeviceRequest
import com.example.myapplication.data.FacebookResponse
import com.example.myapplication.data.NotificationResponse
import com.example.myapplication.data.UnreadCountResponse
import com.example.myapplication.models.Article
import com.example.myapplication.models.DjangoPage
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.DELETE



interface NewsAPIService {

    @GET("api/articles/")
    suspend fun getArticles(
        @Query("page") page: Int,
        @Query("topic") topic: String? = null,
        @Query("keywords") keywords: String? = null
    ): DjangoPage

    @GET("api/search/")
    suspend fun searchArticles(
        @Query("q") query: String,
        @Query("page") page: Int
    ): DjangoPage

    @GET("api/articles/{id}/")
    suspend fun getArticleDetail(@Path("id") id: Int?): Article

    // --- NEW NOTIFICATION ENDPOINTS ---

    @GET("api/notifications/")
    suspend fun getNotifications(
        @Header("X-DEVICE-TOKEN") deviceToken: String
    ): List<NotificationResponse>

    @DELETE("api/notifications/{id}/")
    suspend fun deleteNotification(
        @Path("id") id: Long,
        @Header("X-DEVICE-TOKEN") deviceToken: String
    )
    @PUT("api/notifications/{id}/read/")
    suspend fun markNotificationRead(
        @Path("id") id: Long,
        @Header("X-DEVICE-TOKEN") deviceToken: String
    )

    @PUT("api/notifications/mark_all_read/")
    suspend fun markAllRead(
        @Header("X-DEVICE-TOKEN") deviceToken: String
    )

    @GET("api/notifications/unread_count/")
    suspend fun getUnreadCount(
        @Header("X-DEVICE-TOKEN") deviceToken: String
    ): UnreadCountResponse

    @PUT("api/notifications/read_by_article/")
    suspend fun markReadByArticle(
        @Header("X-DEVICE-TOKEN") token: String,
        @Body body: Map<String, String>
    )
    // Thêm vào interface NewsAPIService
    @POST("api/register_device/")
    suspend fun registerDevice(@Body body: DeviceRequest): Response<Unit> // hoặc data model trả về
    companion object {
        // Use http://10.0.2.2:8000/ if testing on Emulator and standard Django runserver
        // Use your actual IP if testing on a real device on the same Wifi.
        const val BASE_URL =  "http://192.168.1.19:8000/"
        //const val BASE_URL =  "http://127.0.0.1:8000/"

    }
    //http://172.20.0.1:8000/

    //===FB post ===
    @GET("api/fb/posts/")
    suspend fun getFacebookPosts(
        @Query("page") page: Int

    ): FacebookResponse
}//192.168.1.12