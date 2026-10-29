package com.cloudreel.mobile.ui.streamer

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Test

class SavedSettingsTest {

    /** A save written before video formats, the extra metadata fields and the settings pages existed. */
    private val olderSaveJson = """
        {
          "resolution": "FULL_HD",
          "frameRate": 60,
          "latencySeconds": 5.0,
          "filePrefix": "NEWS",
          "metadata": { "isEnabled": true, "slugline": "debate", "crewName": "Crew A" },
          "activeLinks": ["CELLULAR"]
        }
    """.trimIndent()

    @Test
    fun olderSaveKeepsItsValuesAndFillsNewFieldsWithDefaults() {
        val restored = Gson().fromJson(olderSaveJson, SavedSettings::class.java)

        assertEquals(5f, restored.latencySeconds)
        assertEquals("NEWS", restored.filePrefix)
        assertEquals("debate", restored.metadata.slugline)
        assertEquals("Crew A", restored.metadata.crewName)
        assertEquals(listOf(LinkKind.CELLULAR), restored.activeLinks)
        // Fields the older save never had must come back as defaults, not null.
        assertEquals(VideoFormat.P1080_30, restored.videoFormat)
        assertEquals("", restored.metadata.reporter)
        assertEquals(StreamerSettings(), restored.settings)
    }

    @Test
    fun currentSaveRoundTrips() {
        val saved = SavedSettings(
            videoFormat = VideoFormat.I576_AUTO,
            metadata = StreamMetadata(keywords = "election, live"),
            settings = StreamerSettings(previewMirroring = PreviewMirroring.VERTICAL, ifbVolume = 3)
        )
        val gson = Gson()
        assertEquals(saved, gson.fromJson(gson.toJson(saved), SavedSettings::class.java))
    }
}
