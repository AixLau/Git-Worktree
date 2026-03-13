package com.github.gitworktree.dialog

import com.intellij.openapi.ui.ComboBox
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AsyncComboBoxLoaderTest {

    @Test
    fun `load defers supplier execution to submitter`() {
        val comboBox = ComboBox<String>()
        var supplierCalls = 0
        var capturedSupplier: (() -> List<String>)? = null
        var capturedCallback: ((List<String>) -> Unit)? = null
        val loader = AsyncComboBoxLoader(comboBox) { supplier, callback ->
            capturedSupplier = supplier
            capturedCallback = callback
        }

        loader.load {
            supplierCalls += 1
            listOf("v2.0.0", "v1.0.0")
        }

        assertEquals(0, supplierCalls)
        assertFalse(comboBox.isEnabled)
        assertEquals(0, comboBox.itemCount)

        val supplier = assertNotNull(capturedSupplier)
        val callback = assertNotNull(capturedCallback)
        callback(supplier())

        assertEquals(1, supplierCalls)
        assertTrue(comboBox.isEnabled)
        assertEquals(2, comboBox.itemCount)
        assertEquals("v2.0.0", comboBox.getItemAt(0))
        assertEquals("v1.0.0", comboBox.getItemAt(1))
    }

    @Test
    fun `load keeps combo disabled when supplier returns no items`() {
        val comboBox = ComboBox<String>()
        var capturedCallback: ((List<String>) -> Unit)? = null
        val loader = AsyncComboBoxLoader(comboBox) { _, callback ->
            capturedCallback = callback
        }

        loader.load { emptyList() }

        val callback = assertNotNull(capturedCallback)
        callback(emptyList())

        assertFalse(comboBox.isEnabled)
        assertEquals(0, comboBox.itemCount)
    }
}
