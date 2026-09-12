package gay.nyaa.purrcollections.domain

data class CollectionId(val namespace: String, val key: String) {
    init {
        require(namespace.isNotBlank()) { "Namespace cannot be blank" }
        require(key.isNotBlank()) { "Key cannot be blank" }
        require(namespace.matches(Regex("[a-z0-9_]+"))) { "Namespace must be lowercase" }
        require(key.matches(Regex("[A-Z0-9_]+"))) { "Key must be uppercase" }
    }

    override fun toString(): String = "$namespace:$key"

    companion object {
        fun parse(str: String): CollectionId {
            val parts = str.split(":")
            require(parts.size == 2) { "Invalid format: $str" }
            return CollectionId(parts[0], parts[1])
        }
    }
}
