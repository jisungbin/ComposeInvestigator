/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.test

import land.sungbin.composeinvestigator.compiler.test.utils.source
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class IrDumpingTest : IrBaseTest {
  @get:Rule
  val tempDir: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

  @Test
  fun debug() {
    kotlinCompilation(
      workingDir = tempDir.root,
      sourceFiles = arrayOf(source("SourceForIrDump.kt")),
    )
  }
}
