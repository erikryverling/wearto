package se.yverling.wearto.sync.network

import io.reactivex.Completable
import io.reactivex.Single
import org.json.JSONArray
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import se.yverling.wearto.sync.network.dtos.SyncResponse

interface SyncResource {
    @POST("sync/")
    @FormUrlEncoded
    fun getProjects(@Field("token") token: String,
                    @Field("sync_token") syncToken: String,
                    @Field("resource_types") resourceType: JSONArray): Single<SyncResponse>

    @POST("sync/")
    @FormUrlEncoded
    fun addItem(@Field("token") token: String,
                @Field("commands") commands: JSONArray): Completable
}
