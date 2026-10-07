package builder

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.sql.DriverManager

/**
 * Writes a SQLite file that Room's `createFromAsset` accepts: schema statements and the
 * identity hash come straight from the exported Room schema JSON, so the two cannot drift.
 */
object RoomAssetWriter {
    fun write(schemaFile: File, out: File, capsules: List<BuiltCapsule>) {
        val db = Json.parseToJsonElement(schemaFile.readText()).jsonObject["database"]!!.jsonObject
        val version = db["version"]!!.jsonPrimitive.content.toInt()
        out.parentFile.mkdirs()
        out.delete()
        DriverManager.getConnection("jdbc:sqlite:${out.absolutePath}").use { conn ->
            conn.createStatement().use { st ->
                for (entity in db["entities"]!!.jsonArray.map { it.jsonObject }) {
                    val table = entity["tableName"]!!.jsonPrimitive.content
                    st.execute(entity["createSql"]!!.jsonPrimitive.content.replace("\${TABLE_NAME}", table))
                    entity["indices"]?.jsonArray?.forEach {
                        st.execute(it.jsonObject["createSql"]!!.jsonPrimitive.content.replace("\${TABLE_NAME}", table))
                    }
                }
                db["setupQueries"]!!.jsonArray.forEach { st.execute(it.jsonPrimitive.content) }
                st.execute("PRAGMA user_version = $version")
            }
            conn.autoCommit = false
            conn.prepareStatement("INSERT INTO year_capsule(year, headline, summary) VALUES (?, ?, NULL)").use { ps ->
                capsules.forEach {
                    ps.setInt(1, it.year)
                    ps.setString(2, it.headline)
                    ps.addBatch()
                }
                ps.executeBatch()
            }
            conn.prepareStatement(
                "INSERT INTO capsule_item(year, category, rank, title, subtitle, mbid) VALUES (?, ?, ?, ?, ?, ?)",
            ).use { ps ->
                capsules.flatMap { it.items }.forEach {
                    ps.setInt(1, it.year)
                    ps.setString(2, it.category)
                    ps.setInt(3, it.rank)
                    ps.setString(4, it.title)
                    ps.setString(5, it.subtitle)
                    ps.setString(6, it.mbid)
                    ps.addBatch()
                }
                ps.executeBatch()
            }
            conn.commit()
        }
    }
}
