/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.test.tracker

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import land.sungbin.composeinvestigator.compiler.test.IrBaseTest
import land.sungbin.composeinvestigator.compiler.test.kotlinCompilation
import land.sungbin.composeinvestigator.compiler.test.utils.sourceString

class InvalidationTrackTableCallTest : FunSpec(), IrBaseTest {
  init {
    test("currentComposableInvalidationTracker") {
      val compiled = kotlinCompilation(
        workingDir = tempdir(),
        enableComposeCompiler = false,
        enableVerboseLogging = false,
        sourceFiles = arrayOf(SourceFile.kotlin("test.kt", sourceString("tracker/InvalidationTrackTableCallTestSource.kt"))),
      )

      compiled.exitCode shouldBe KotlinCompilation.ExitCode.OK

      val own = compiled.classLoader.loadClass("TestKt")
      val assertion = own.declaredMethods.single { method -> method.name == "assert" }

      assertion.invoke(own, own)
    }
  }
}
