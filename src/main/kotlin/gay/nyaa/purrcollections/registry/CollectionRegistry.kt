package gay.nyaa.purrcollections.registry

import gay.nyaa.purrcollections.domain.Collection
import gay.nyaa.purrcollections.domain.CollectionId
import gay.nyaa.purrcollections.domain.CollectionType
import org.bukkit.Material
import org.bukkit.entity.EntityType
import java.util.concurrent.ConcurrentHashMap

class CollectionRegistry {
    private val collections = ConcurrentHashMap<CollectionId, Collection>()
    private val materialIndex = ConcurrentHashMap<Material, CollectionId>()
    private val entityIndex = ConcurrentHashMap<EntityType, CollectionId>()

    fun register(collection: Collection) {
        val existing = collections.putIfAbsent(collection.id, collection)
        require(existing == null) { "Collection ${collection.id} already registered" }

        collection.material?.let { materialIndex[it] = collection.id }
        collection.entityType?.let { entityIndex[it] = collection.id }
    }

    fun get(id: CollectionId): Collection? = collections[id]

    fun getByMaterial(material: Material): Collection? = materialIndex[material]?.let { collections[it] }

    fun getByEntityType(entityType: EntityType): Collection? = entityIndex[entityType]?.let { collections[it] }

    fun getByType(type: CollectionType): List<Collection> = collections.values.filter { it.type == type }

    fun all(): List<Collection> = collections.values.toList()

    fun contains(id: CollectionId): Boolean = collections.containsKey(id)

    fun size(): Int = collections.size
}
