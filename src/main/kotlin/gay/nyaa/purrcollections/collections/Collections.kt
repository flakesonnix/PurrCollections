package gay.nyaa.purrcollections.collections

import gay.nyaa.purrcollections.domain.Collection

object Collections {
    fun all(): List<Collection> = MiningCollections.all()
}
