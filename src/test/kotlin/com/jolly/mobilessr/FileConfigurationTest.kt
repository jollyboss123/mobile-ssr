package com.jolly.mobilessr

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.io.File
import java.nio.file.Path

/**
 * @author jolly
 */
@SpringBootTest
@ActiveProfiles("test")
class FileConfigurationTest {

    @Autowired
    private lateinit var fileConfiguration: FileConfiguration

    @BeforeEach
    fun resetFileVer() {
        fileConfiguration.fileVer.set(0)
    }

    @Test
    fun `checkVersion returns true when version is greater than current`(@TempDir tempDir: Path) {
        val file = createFileWithVersion(tempDir, 2)
        val result = fileConfiguration.checkVersion(file)

        assertTrue(result, "Expected checkVersion to return true, but it returned false")
    }

    @Test
    fun `checkVersion returns false when version is not greater than current`(@TempDir tempDir: Path) {
        val file2 = createFileWithVersion(tempDir, 2)
        val initialResult = fileConfiguration.checkVersion(file2)

        assertTrue(initialResult, "Expected checkVersion to return true, but it returned false")

        val file1 = createFileWithVersion(tempDir, 1)
        val result = fileConfiguration.checkVersion(file1)

        assertFalse(result, "Expected checkVersion to return false, but it returned true")
    }

    @Test
    fun `checkVersion returns false when version is equal current`(@TempDir tempDir: Path) {
        val file = createFileWithVersion(tempDir, 1)
        val initialResult = fileConfiguration.checkVersion(file)

        assertTrue(initialResult, "Expected checkVersion to return true, but it returned false")

        val result = fileConfiguration.checkVersion(file)

        assertFalse(result, "Expected checkVersion to return false, but it returned true")
    }

    private fun createFileWithVersion(tempDir: Path, version: Int): File {
        val file = tempDir.resolve("test__v$version.json").toFile()
        file.createNewFile()
        return file
    }
}
