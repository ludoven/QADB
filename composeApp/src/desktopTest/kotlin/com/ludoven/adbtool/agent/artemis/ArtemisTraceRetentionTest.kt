package com.ludoven.adbtool.agent.artemis

import java.nio.file.Files
import java.nio.file.attribute.FileTime
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ArtemisTraceRetentionTest {
    @Test
    fun `purge deletes only expired files below the supplied trace root`() {
        val root = Files.createTempDirectory("artemis-traces")
        val old = Files.writeString(root.resolve("old.json"), "old")
        val fresh = Files.writeString(root.resolve("fresh.json"), "fresh")
        val outside = Files.createTempFile("outside", ".json")
        val now = Instant.parse("2026-09-20T00:00:00Z")
        Files.setLastModifiedTime(old, FileTime.from(now.minusSeconds(25 * 3600)))
        Files.setLastModifiedTime(fresh, FileTime.from(now.minusSeconds(23 * 3600)))

        val deleted = ArtemisTraceRetention(
            ArtemisEvidencePolicy(rawTraceRetentionHours = 24), Clock.fixed(now, ZoneOffset.UTC)
        ).purgeExpired(root)

        assertEquals(listOf(old), deleted)
        assertFalse(Files.exists(old))
        assertTrue(Files.exists(fresh))
        assertTrue(Files.exists(outside))
    }
}
