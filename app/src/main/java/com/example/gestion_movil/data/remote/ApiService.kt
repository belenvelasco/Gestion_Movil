package com.example.gestion_movil.data.remote

import retrofit2.http.Query
import retrofit2.http.GET

//
interface ApiService{
    @GET("posts")
    suspend fun getPosts(@Query("_limit") limite: Int = 20): List<PostDto>

    @GET("users")
    suspend fun getUsers(): List<UserDto>
}