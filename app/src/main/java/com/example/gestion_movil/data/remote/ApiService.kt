package com.example.gestion_movil.data.remote

import androidx.room.Query


interface ApiService{
    @GET("posts")
    suspend fun getPosts(@Query("_limit") limite: Int = 20): List<PostDto>

    @GET("users")
    suspend fun getUsers(): List<UserDto>
}