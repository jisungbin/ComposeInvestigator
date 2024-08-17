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
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver

typealias VerifyGolden = (source: String, expect: String, directory: String) -> GoldenVerificationResult

enum class GoldenVerificationResult {
  SAVED,
  PASS,
}

/**
 * Compare transformed IR source to a golden test file. Golden files contain both the
 * pre-transformed and post-transformed source for easier review.
 **/
class GoldenTestExtension : BeforeEachCallback, ParameterResolver {
  private lateinit var goldenName: String

  override fun beforeEach(context: ExtensionContext) {
    val fqn = context.requiredTestClass.canonicalName.removePrefix("land.sungbin.composeinvestigator.compiler.")
    goldenName = "${fqn.replace(".", "/")}/${context.requiredTestMethod.name}.txt"
  }

  override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean =
    parameterContext.parameter.type == VerifyGolden::class.java

  override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any =
    ::verifyGolden

  /**
   * Verify the current test against the matching golden file. If the golden does not exist,
   * create a new golden file.
   */
  fun verifyGolden(source: String, expect: String, directory: String): GoldenVerificationResult {
    val golden = File(directory, goldenName)
    if (!golden.exists()) {
      saveGolden(golden, GoldenTransformTestInfo(source, expect))
      return GoldenVerificationResult.SAVED
    }

    val loadedGolden = try {
      GoldenTransformTestInfo.fromEncodedString(golden.readText())
    } catch (exception: IllegalStateException) {
      error("Golden ${golden.absolutePath} file could not be parsed.\n${exception.message}")
    }

    assertEquals(
      loadedGolden.transformed,
      expect,
      "Transformed source does not match golden file: \n${golden.absolutePath}",
    )
    return GoldenVerificationResult.PASS
  }

  private fun saveGolden(destination: File, testInfo: GoldenTransformTestInfo) {
    destination.parentFile?.run { if (!exists()) Files.createDirectories(toPath()) }
    destination.writeText(testInfo.encodeToString())
  }

  companion object {
    const val DEFAULT_GOLDEN_DIRECTORY = "src/test/resources/golden"
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

    fun fromEncodedString(encoded: String): GoldenTransformTestInfo {
      val split = encoded.removePrefix(SOURCE_HEADER).split(TRANSFORM_HEADER)
      if (split.size != 2) error("Could not parse encoded golden string. Expected 2 sections but was ${split.size}.")
      return GoldenTransformTestInfo(source = split[0].trim(), transformed = split[1].trim())
    }
  }
}
