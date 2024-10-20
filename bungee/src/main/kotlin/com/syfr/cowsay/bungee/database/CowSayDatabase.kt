package com.syfr.cowsay.bungee.database

import net.md_5.bungee.config.Configuration
import java.sql.Connection
import java.sql.DriverManager
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class CowSayDatabase(private val config: Configuration) {

    private val username: String = this.config.getString("database.username")
    private val password: String = this.config.getString("database.password")
    private val url: String = "jdbc:mysql://" +
            this.config.getString("database.host") + ":" +
            this.config.getInt("database.port") + "/" +
            this.config.getString("database.database") +
            "?autoReconnect=true"

    private var connection: Connection? = null

    init {
        this.loadSQL("/deploy.sql")
    }

    fun fetchLastSayAndCount(uuid: UUID): Pair<String?, AtomicInteger> {
        this.getConnection().prepareStatement(
            "SELECT last_say, count FROM $TABLE_NAME " +
                    "WHERE uuid=? LIMIT 1;"
        ).use { statement ->
            statement.setString(1, uuid.toString())
            statement.executeQuery().use { result ->
                if (!result.next()) {
                    return null to AtomicInteger()
                }
                return result.getString("last_say") to AtomicInteger(result.getInt("count"))
            }
        }
    }

    fun updateLastSayAndCount(uuid: UUID, lastSay: String?, count: Int, ) {
        this.getConnection().prepareStatement(
            "INSERT INTO $TABLE_NAME (uuid, last_say, count) VALUES " +
                    "(?, ?, ?) ON DUPLICATE KEY UPDATE last_say=?, count=?;"
        ).use { statement ->
            statement.setString(1, uuid.toString())
            statement.setString(2, lastSay)
            statement.setInt(3, count)
            statement.setString(4, lastSay)
            statement.setInt(5, count)
            statement.executeUpdate()
        }
    }

    private fun getConnection() : Connection {
        if (this.connection == null || this.connection!!.isClosed) {
            Class.forName("com.mysql.cj.jdbc.Driver")
            this.connection = DriverManager.getConnection(this.url, this.username, this.password)
        }
        return this.connection!!
    }

    private fun loadSQL(fileName: String) {
        this.javaClass.getResourceAsStream(fileName)?.let {
            Scanner(it).useDelimiter(";").use { scanner ->
                while (scanner.hasNext()) {
                    val query = scanner.next().trim()
                    if (query.isNotEmpty()) {
                        this.getConnection().prepareStatement(query).executeUpdate()
                    }
                }
            }
        }
    }

    private companion object {
        const val TABLE_NAME = "cow_say"
    }
}
