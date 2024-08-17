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
import land.sungbin.composeinvestigator.compiler._extension.AbstractGoldenTest
import land.sungbin.composeinvestigator.compiler._extension.GoldenVerificationResult
import org.junit.jupiter.api.io.TempDir

class GoldenTestExtensionTest : AbstractGoldenTest() {
  @field:TempDir lateinit var tempDir: File

  private val golden1 = "fun golden1() = Unit"
  private val golden2 = "fun golden2() = Unit"

  @Test fun happyWhenGoldensSame() {
    assertEquals(GoldenVerificationResult.SAVED, verifyGolden(golden1, golden1, tempDir.path), "Save golden file when not exists")
    assertEquals(GoldenVerificationResult.PASS, verifyGolden(golden1, golden1, tempDir.path), "Pass when goldens are same")
  }

  @Test fun throwsWhenGoldenChanged() {
    assertEquals(GoldenVerificationResult.SAVED, verifyGolden(golden1, golden1, tempDir.path), "Save golden file when not exists")

    val result = assertFails("Throwing when golden file was changed") { verifyGolden(golden1, golden2, tempDir.path) }
    assertEquals(
      """
Transformed source does not match golden file: 
${tempDir.path}/GoldenTestExtensionTest/throwsWhenGoldenChanged.txt ==> expected: <$golden1> but was: <$golden2>
      """.trim(),
      result.message,
    )
  }
}
