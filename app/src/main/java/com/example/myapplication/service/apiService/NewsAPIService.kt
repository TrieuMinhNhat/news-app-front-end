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


import com.example.myapplication.data.SavedArticle
import com.example.myapplication.data.SavedArticleResponse
import com.example.myapplication.data.SavedSocialPost
import com.example.myapplication.data.SavedSocialPostResponse
import com.example.myapplication.data.SavedStatusResponse

interface NewsAPIService {

    @GET("api/articles/")
    suspend fun getArticles(
        @Query("page") page: Int,
        @Query("topic") topic: String? = null,
        @Query("keywords") keywords: String? = null
    ): DjangoPage

    @GET("api/saved/articles/")
    suspend fun listSavedArticles(
        @Header("X-DEVICE-TOKEN") token: String
    ): SavedArticleResponse

    @POST("api/saved/articles/{article_id}/save/")
    suspend fun saveArticle(
        @Header("X-DEVICE-TOKEN") token: String,
        @Path("article_id") articleId: Int
    ): SavedArticle

    @DELETE("api/saved/articles/{article_id}/unsave/")
    suspend fun unsaveArticle(
        @Header("X-DEVICE-TOKEN") token: String,
        @Path("article_id") articleId: Int
    ): com.example.myapplication.data.DeleteResponse

    @GET("api/saved/articles/{article_id}/status/")
    suspend fun savedArticleStatus(
        @Header("X-DEVICE-TOKEN") token: String,
        @Path("article_id") articleId: Int
    ): SavedStatusResponse

    @GET("api/fb/saved/social-posts/")
    suspend fun listSavedSocialPosts(
        @Header("X-DEVICE-TOKEN") token: String
    ): SavedSocialPostResponse

    @POST("api/fb/saved/social-posts/{post_id}/save/")
    suspend fun saveSocialPost(
        @Header("X-DEVICE-TOKEN") token: String,
        @Path("post_id") postId: Long
    ): SavedSocialPost

    @DELETE("api/fb/saved/social-posts/{post_id}/unsave/")
    suspend fun unsaveSocialPost(
        @Header("X-DEVICE-TOKEN") token: String,
        @Path("post_id") postId: Long
    ): com.example.myapplication.data.DeleteResponse

    @GET("api/fb/saved/social-posts/{post_id}/status/")
    suspend fun savedSocialPostStatus(
        @Header("X-DEVICE-TOKEN") token: String,
        @Path("post_id") postId: Long
    ): SavedStatusResponse

    @GET("api/search/")
    suspend fun searchArticles(
        @Query("q") query: String,
        @Query("page") page: Int
    ): DjangoPage

    @GET("api/articles/{id}/")
    suspend fun getArticleDetail(@Path("id") id: Int?): Article

    @GET("api/articles/headlines/")
    suspend fun getHeadlines(
        @Header("X-DEVICE-TOKEN") deviceToken: String
    ): DjangoPage

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
    //===FB post ===
    @GET("api/fb/posts/")
    suspend fun getFacebookPosts(
        @Header("X-DEVICE-TOKEN") token: String,
        @Query("page") page: Int,
        @Query("keywords") keywords: String? = null,
        @Query("sort_keyword") sortKeyword: String? = null

    ): FacebookResponse
}//192.168.1.12