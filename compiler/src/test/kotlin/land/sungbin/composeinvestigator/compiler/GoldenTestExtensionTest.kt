/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import land.sungbin.composeinvestigator.compiler._extension.GoldenTestExtension
import land.sungbin.composeinvestigator.compiler._extension.GoldenVerificationResult
import org.junit.jupiter.api.io.TempDir

class GoldenTestExtensionTest {
  @field:TempDir lateinit var tempDir: File

  private val golden1 = "fun golden1() = Unit"
  private val golden2 = "fun golden2() = Unit"

  @Test fun happyWhenGoldensSame() {
    val path = tempDir.resolve("happyWhenGoldensSame.txt").path
    assertEquals(
      GoldenVerificationResult.SAVED,
      GoldenTestExtension.verifyGolden(path, golden1, golden1),
      "Save golden file when not exists",
    )
    assertEquals(
      GoldenVerificationResult.PASS,
      GoldenTestExtension.verifyGolden(path, golden1, golden1),
      "Pass when goldens are same",
    )
  }

  @Test fun throwsWhenGoldenChanged() {
    val path = tempDir.resolve("throwsWhenGoldenChanged.txt").path

    assertEquals(
      GoldenVerificationResult.SAVED,
      GoldenTestExtension.verifyGolden(path, golden1, golden1),
      "Save golden file when not exists",
    )

    val result = assertFails("Throwing when golden file was changed") {
      GoldenTestExtension.verifyGolden(path, golden1, golden2)
    }

    assertEquals(
      """
Transformed source does not match golden file: 
$path ==> expected: <$golden1> but was: <$golden2>
      """.trim(),
      result.message,
    )
  }
}
