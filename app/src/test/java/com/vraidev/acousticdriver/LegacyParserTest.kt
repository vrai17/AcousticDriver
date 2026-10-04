package com.vraidev.acousticdriver

import com.vraidev.acousticdriver.core.parser.LegacySonyEricssonParser
import org.junit.Assert.*
import org.junit.Test

class LegacyParserTest {

    private val parser = LegacySonyEricssonParser()

    @Test
    fun testParseValidSonyEricssonDriver() {
        val sampleIni = """
            ; Sony Ericsson Acoustic Driver Config
            [Acoustic_Driver]
            Name = Mega Acoustic Walkman
            Device = Sony Ericsson W810i
            Author = Black_Gohan
            Speaker_Gain = 0x08
            Spk_EQ_Band1 = 60, 5.0
            Spk_EQ_Band2 = 250, 3.0
            Spk_EQ_Band3 = 1000, 0.0
            Spk_EQ_Band4 = 4000, 2.0
            Spk_EQ_Band5 = 14000, 4.0
            Bass_Boost = 1
            Bass_Level = 850
            Compressor_Threshold = -14.0
            Compressor_Ratio = 2.5
            Limiter_Ceiling = -0.5
        """.trimIndent()

        assertTrue(parser.canParse(sampleIni))
        val air = parser.parse(sampleIni)

        assertEquals("Mega Acoustic Walkman", air.metadata.name)
        assertEquals("Sony Ericsson W810i", air.metadata.sourceDevice)
        assertEquals("Black_Gohan", air.metadata.author)
        assertEquals(5, air.graphicEq.size)
        assertEquals(60, air.graphicEq[0].frequencyHz)
        assertEquals(5.0f, air.graphicEq[0].gainDb, 0.01f)
        assertNotNull(air.bassBoost)
        assertTrue(air.bassBoost!!.enabled)
        assertEquals(850, air.bassBoost!!.strength)
        assertNotNull(air.compressor)
        assertEquals(-14.0f, air.compressor!!.thresholdDb, 0.01f)
        assertEquals(2.5f, air.compressor!!.ratio, 0.01f)
    }

    @Test
    fun testParseFallbackOnMinimalContent() {
        val minimal = "Speaker_Gain = 4"
        assertTrue(parser.canParse(minimal))
        val air = parser.parse(minimal)
        assertNotNull(air)
        assertTrue(air.graphicEq.isNotEmpty())
    }
}
