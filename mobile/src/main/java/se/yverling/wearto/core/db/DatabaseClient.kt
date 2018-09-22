package se.yverling.wearto.core.db

import android.content.Context
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import org.jetbrains.anko.AnkoLogger
import se.yverling.wearto.core.entities.Item
import se.yverling.wearto.core.entities.ItemWithProject
import se.yverling.wearto.core.entities.Project
import se.yverling.wearto.core.projectColorToColorHex
import java.util.*
import javax.inject.Inject

class DatabaseClient @Inject constructor(
        private val context: Context,
        private val database: AppDatabase
) : AnkoLogger {


    // -- Items

    fun findItemByUuid(uuid: String): Single<Item> = database.itemDao().findByUuid(uuid)

    fun findByNameAndProjectId(name: String, projectId: Long): Maybe<Item>  {
        return database.itemDao().findByNameAndProjectId(name, projectId)
    }

    fun findItemWithProjectByUuid(uuid: String): Single<ItemWithProject> {
        return database.itemWithProjectDao().findItemWithProjectByUuid(uuid)
    }

    fun findAllItemsWithProject(): Single<List<ItemWithProject>> {
        return database.itemWithProjectDao().findAllItemsWithProject()
    }

    fun findAllItemsWithProjectContinuously(): Flowable<List<ItemWithProject>> {
        return database.itemWithProjectDao().findAllItemsWithProjectContinuously()
    }

    fun findAllOrphanItems(): Single<List<Item>> = database.itemDao().findAllOrphans()

    fun saveItem(name: String, projectName: String): Completable {
        return database.projectDao()
                .findByName(projectName)
                .flatMapCompletable {
                    Completable.fromCallable {
                        database.itemDao().save(Item(UUID.randomUUID().toString(), name, it.id))
                    }
                }
    }

    fun updateItem(uuid: String, name: String, projectName: String): Completable {
        return database.projectDao()
                .findByName(projectName)
                .flatMapCompletable {
                    Completable.fromCallable {
                        database.itemDao().update(Item(uuid, name, it.id))
                    }
                }
    }

    fun deleteItem(uuid: String): Completable {
        return database.itemDao()
                .findByUuid(uuid)
                .flatMapCompletable {
                    Completable.fromCallable {
                        database.itemDao().update(Item(it.uuid, it.name, it.projectId, true))
                    }
                }
    }

    fun deleteAllItems(): Completable {
        return Completable.fromCallable {
            database.itemDao().deleteAll()
        }
    }


    // -- Projects

    fun findAllProjects(): Single<List<Project>> = database.projectDao().findAll()

    fun findProjectByName(name: String): Single<Project> {
        return database.projectDao().findByName(name)
    }

    fun replaceAllProjects(projects: List<se.yverling.wearto.sync.network.dtos.Project>): Completable {
        return Completable.fromCallable {
            database.projectDao().deleteAll()
            database.projectDao().saveAll(projects.map {
                Project(it.id, it.name, projectColorToColorHex(context, it.color))
            })
        }
    }


    // -- Other

    fun deleteAll(): Completable {
        return Completable.fromCallable {
            database.itemDao().deleteAll()
            database.projectDao().deleteAll()
        }
    }

    fun close() {
        database.close()
    }
}