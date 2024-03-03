package com.example.iuda

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url


interface APIservice {
    @GET
    suspend fun getInformation(@Url url:String):Response<Direction>
}