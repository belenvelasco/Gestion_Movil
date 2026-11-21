package com.example.gestion_movil.remote

data class PostDto(
    val userId: Int,
    val id: Int,
    val title: String?,
    val body: String?
)

data class UserDto(
    val id: Int,
    val name: String?,
    val address: AddressDto?,
    val comany: ComanyDto?
)

data class AddressDto(val city: String)
data class ComanyDto(val name: String)