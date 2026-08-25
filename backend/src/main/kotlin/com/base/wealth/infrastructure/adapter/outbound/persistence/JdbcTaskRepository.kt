package com.base.wealth.infrastructure.adapter.outbound.persistence

import com.base.wealth.domain.model.Task
import com.base.wealth.domain.model.UserId
import com.base.wealth.domain.port.outbound.TaskRepository
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.util.UUID

@Repository
class JdbcTaskRepository(
    private val jdbcClient: JdbcClient,
) : TaskRepository {
    override fun findAll(userId: UserId): List<Task> =
        jdbcClient
            .sql("SELECT * FROM tasks WHERE user_id = :userId ORDER BY created_at DESC")
            .param("userId", userId.value)
            .query(::mapRow)
            .list()

    override fun findById(
        userId: UserId,
        id: UUID,
    ): Task? =
        jdbcClient
            .sql("SELECT * FROM tasks WHERE id = :id AND user_id = :userId")
            .param("id", id)
            .param("userId", userId.value)
            .query(::mapRow)
            .optional()
            .orElse(null)

    override fun save(task: Task): Task {
        jdbcClient
            .sql(
                """
                INSERT INTO tasks (id, user_id, title, completed, created_at)
                VALUES (:id, :userId, :title, :completed, :createdAt)
                ON CONFLICT (id) DO UPDATE SET title = EXCLUDED.title, completed = EXCLUDED.completed
                """.trimIndent(),
            ).param("id", task.id)
            .param("userId", task.userId)
            .param("title", task.title)
            .param("completed", task.completed)
            .param("createdAt", task.createdAt.toSqlTimestamp())
            .update()
        return task
    }

    override fun deleteById(
        userId: UserId,
        id: UUID,
    ): Boolean =
        jdbcClient
            .sql("DELETE FROM tasks WHERE id = :id AND user_id = :userId")
            .param("id", id)
            .param("userId", userId.value)
            .update() > 0

    private fun mapRow(
        rs: ResultSet,
        @Suppress("UNUSED_PARAMETER") rowNum: Int,
    ): Task =
        Task(
            id = rs.getObject("id", UUID::class.java),
            userId = rs.getObject("user_id", UUID::class.java),
            title = rs.getString("title"),
            completed = rs.getBoolean("completed"),
            createdAt = rs.getTimestamp("created_at").toInstant(),
        )
}
