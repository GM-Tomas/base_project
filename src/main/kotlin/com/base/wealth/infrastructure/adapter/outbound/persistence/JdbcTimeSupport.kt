package com.base.wealth.infrastructure.adapter.outbound.persistence

import java.sql.Timestamp
import java.time.Instant

// pgjdbc 42.7.3 can't infer a SQL type for a bare java.time.Instant bound as a JdbcClient param
// ("Can't infer the SQL type to use for an instance of java.time.Instant") — every Jdbc*Repository
// write/filter that binds an Instant needs this. Confirmed against a real Postgres, not simulated.
fun Instant.toSqlTimestamp(): Timestamp = Timestamp.from(this)
