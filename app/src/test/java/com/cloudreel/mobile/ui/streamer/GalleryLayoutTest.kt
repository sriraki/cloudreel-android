package com.cloudreel.mobile.ui.streamer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GalleryLayoutTest {

    private fun FrameCell.overlaps(other: FrameCell) =
        x + width > other.x + 0.0001f && other.x + other.width > x + 0.0001f &&
            y + height > other.y + 0.0001f && other.y + other.height > y + 0.0001f

    @Test
    fun everyoneGetsATileInsideTheFrameAndNoTilesOverlap() {
        for (isLandscape in listOf(false, true)) {
            for (count in 1..16) {
                val cells = galleryCells(count, isLandscape)
                assertEquals(count, cells.size)
                cells.forEachIndexed { index, cell ->
                    assertTrue("$count people: $cell leaves the frame", cell.x >= 0f && cell.y >= 0f && cell.x + cell.width <= 1.0001f && cell.y + cell.height <= 1.0001f)
                    cells.drop(index + 1).forEach { other -> assertFalse("$count people: $cell overlaps $other", cell.overlaps(other)) }
                }
            }
        }
    }

    @Test
    fun onePersonFillsTheFrameAndTwoStackInPortrait() {
        assertEquals(listOf(FrameCell(0f, 0f, 1f, 1f)), galleryCells(1, isLandscape = false))
        assertEquals(listOf(FrameCell(0f, 0f, 1f, 0.5f), FrameCell(0f, 0.5f, 1f, 0.5f)), galleryCells(2, isLandscape = false))
    }

    @Test
    fun aShortLastRowIsCentred() {
        // Three in portrait: two on top, the third centred below.
        assertEquals(0.25f, galleryCells(3, isLandscape = false)[2].x, 0.0001f)
    }
}
