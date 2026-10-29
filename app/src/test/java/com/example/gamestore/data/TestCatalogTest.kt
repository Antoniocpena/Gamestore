package com.example.gamestore.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TestCatalogTest {
    @Test
    fun createProducts_builds500DeterministicAndCoherentProducts() {
        val firstCatalog = TestCatalog.createProducts()
        val secondCatalog = TestCatalog.createProducts()
        val profileIds = TestCatalog.profiles.map { it.id }.toSet()

        assertEquals(TestCatalog.CATALOG_SIZE, firstCatalog.size)
        assertEquals(firstCatalog, secondCatalog)
        assertEquals(TestCatalog.CATALOG_SIZE, firstCatalog.map { it.id }.toSet().size)
        assertEquals("game-001", firstCatalog.first().id)
        assertEquals("game-500", firstCatalog.last().id)
        assertTrue(firstCatalog.all { it.developerId in profileIds })
        assertTrue(firstCatalog.all { it.priceCents > 0 })
        assertTrue(firstCatalog.all { it.stock >= 0 })
        assertTrue(firstCatalog.all { it.coverColorIndex in TestCatalog.profiles.indices })
        assertEquals(
            TestCatalog.profiles.associate { it.id to 50 },
            firstCatalog.groupingBy { it.developerId }.eachCount(),
        )
    }
}
