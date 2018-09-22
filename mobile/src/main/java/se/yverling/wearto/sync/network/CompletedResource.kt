package se.yverling.wearto.sync.network

import io.reactivex.Single
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import se.yverling.wearto.sync.network.dtos.ProjectDataResponse

interface CompletedResource {
    @POST("completed/get_all")
    @FormUrlEncoded
    fun getItems(
            @Field("token") token: String,
            @Field("project_id") projectId: Long,
            @Field("offset") offset: Int,
            @Field("limit") limit: Int
    ): Single<ProjectDataResponse>
}