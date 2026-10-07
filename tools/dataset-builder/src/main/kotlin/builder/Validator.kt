package builder

private val UUID_REGEX = Regex("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")

/** Dataset sanity rules from the testing strategy: min items, unique ranks, valid UUIDs. */
object Validator {
    fun validate(
        capsules: List<BuiltCapsule>,
        minSongs: Int,
    ): List<String> {
        val problems = mutableListOf<String>()
        for (capsule in capsules) {
            val songs = capsule.items.count { it.category == "SONG" }
            if (songs < minSongs) problems += "${capsule.year}: only $songs songs (min $minSongs)"
            capsule.items.groupBy { it.category }.forEach { (category, items) ->
                val dupes =
                    items
                        .groupingBy { it.rank }
                        .eachCount()
                        .filterValues { it > 1 }
                        .keys
                if (dupes.isNotEmpty()) problems += "${capsule.year}/$category: duplicate ranks $dupes"
            }
            capsule.items
                .filter { it.mbid != null && !UUID_REGEX.matches(it.mbid) }
                .forEach { problems += "${capsule.year}: invalid MBID '${it.mbid}' for ${it.title}" }
        }
        return problems
    }
}
