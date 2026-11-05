package com.example

import com.example.data.local.SubtaskItem
import com.example.data.local.encodeSubtasks
import com.example.data.local.parseSubtasks
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleRobolectricTest {

    @Test
    fun `subtasks encoding and parsing test`() {
        val subtasks = listOf(
            SubtaskItem("Design wireframes", true),
            SubtaskItem("Implement UI", false),
            SubtaskItem("Write unit tests", true)
        )
        val encoded = encodeSubtasks(subtasks)
        val parsed = parseSubtasks(encoded)

        assertEquals(3, parsed.size)
        assertTrue(parsed[0].isDone)
        assertEquals("Design wireframes", parsed[0].title)
        assertEquals(false, parsed[1].isDone)
        assertTrue(parsed[2].isDone)
    }

    @Test
    fun `empty subtasks string parses to empty list`() {
        val empty = parseSubtasks("")
        assertTrue(empty.isEmpty())
    }
}
