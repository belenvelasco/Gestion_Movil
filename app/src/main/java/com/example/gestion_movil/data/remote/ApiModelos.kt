package com.example.gestion_movil.data.remote

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
    val company: CompanyDto?
)

data class AddressDto(val city: String)
data class CompanyDto(val name: String)