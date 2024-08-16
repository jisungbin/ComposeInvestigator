/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler._extension

import java.io.File
import java.nio.file.Files
import kotlin.test.assertEquals
import land.sungbin.composeinvestigator.compiler._extension.GoldenTransformTestInfo.Companion.PATH_TO_GOLDENS
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver

typealias VerifyGolden = (source: String, expect: String) -> Unit

/**
 * Compare transformed IR source to a golden test file. Golden files contain both the
 * pre-transformed and post-transformed source for easier review.
 **/
class GoldenTestExtension : BeforeEachCallback, ParameterResolver {
  private lateinit var goldenFile: File

  override fun beforeEach(context: ExtensionContext) {
    val goldenPath = "$PATH_TO_GOLDENS/${context.requiredTestClass.name}/${context.requiredTestMethod.name}.txt"
    goldenFile = File(goldenPath)
  }

  override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean =
    parameterContext.parameter.type == VerifyGolden::class.java

  override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any =
    ::verifyGolden

  /**
   * Verify the current test against the matching golden file. If the golden does not exist,
   * create a new golden file.
   */
  fun verifyGolden(source: String, expect: String) {
    if (!goldenFile.exists()) saveGolden(GoldenTransformTestInfo(source, expect))

    val loadedGolden = try {
      GoldenTransformTestInfo.fromEncodedString(goldenFile.readText())
    } catch (exception: IllegalStateException) {
      error("Golden ${goldenFile.absolutePath} file could not be parsed.\n${exception.message}")
    }

    assertEquals(
      loadedGolden.transformed,
      expect,
      "Transformed source does not match golden file: \n${goldenFile.absolutePath}",
    )
  }

  private fun saveGolden(testInfo: GoldenTransformTestInfo) {
    val directory = goldenFile.parentFile!!
    if (!directory.exists()) Files.createDirectories(directory.toPath())
    goldenFile.writeText(testInfo.encodeToString())
  }
}

/**
 * @param source The pre-transformed source code.
 * @param transformed Post transformed IR tree source.
 */
private data class GoldenTransformTestInfo(
  val source: String,
  val transformed: String,
) {
  fun encodeToString() = buildString {
    append(SOURCE_HEADER)
    appendLine()
    appendLine()
    append(source)
    appendLine()
    appendLine()
    append(TRANSFORM_HEADER)
    appendLine()
    appendLine()
    append(transformed)
    appendLine()
  }

  companion object {
    val SOURCE_HEADER = """
            //
            // Source
            // ------------------------------------------
    """.trimIndent()
    val TRANSFORM_HEADER = """
            //
            // Transformed IR
            // ------------------------------------------
    """.trimIndent()

    const val PATH_TO_GOLDENS = "src/test/resources/golden"

    fun fromEncodedString(encoded: String): GoldenTransformTestInfo {
      val split = encoded.removePrefix(SOURCE_HEADER).split(TRANSFORM_HEADER)
      if (split.size != 2) error("Could not parse encoded golden string. Expected 2 sections but was ${split.size}.")
      return GoldenTransformTestInfo(source = split[0].trim(), transformed = split[1].trim())
    }
  }
}
