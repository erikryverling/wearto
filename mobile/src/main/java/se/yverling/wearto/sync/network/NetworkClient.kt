package se.yverling.wearto.sync.network

import com.squareup.moshi.Moshi
import io.reactivex.Completable
import io.reactivex.Single
import org.json.JSONArray
import se.yverling.wearto.auth.TokenManager
import se.yverling.wearto.core.entities.Item
import se.yverling.wearto.sync.network.dtos.AddItemCommand
import se.yverling.wearto.sync.network.dtos.Args
import se.yverling.wearto.sync.network.dtos.SyncResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkClient @Inject constructor(
        private val syncResource: SyncResource,
        private val tokenManager: TokenManager
) {
    private val syncToken = "*"
    private val resourceTypes = JSONArray().put("projects")

    fun getProjects(): Single<SyncResponse> {
        return tokenManager.getAccessToken().flatMapSingle {
            syncResource.getProjects(it, syncToken, resourceTypes)
        }
    }

    fun getProjects(accessToken: String): Single<SyncResponse> {
        return syncResource.getProjects(accessToken, syncToken, resourceTypes)
    }

    fun addItem(item: Item): Completable {
        return tokenManager.getAccessToken()
                .flatMapCompletable {
                    val builder = Moshi.Builder().build()
                    val adapter = builder.adapter<AddItemCommand>(AddItemCommand::class.java)
                    val json = adapter.toJson(AddItemCommand(Args(item.projectId.toString(), item.name)))

                    val commands = JSONArray().put(json)
                    syncResource.addItem(it, commands)
                }
    }
}
