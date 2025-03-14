// Copyright 2025 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler

import org.jetbrains.kotlin.generators.generateTestGroupSuiteWithJUnit5

fun main() {
  generateTestGroupSuiteWithJUnit5 {
    testGroup(
      testDataRoot = "compiler/src/test/data",
      testsRoot = "compiler/src/test/java",
    ) {
      testClass<AbstractBoxTest> { model("box") }
      testClass<AbstractFirDumpTest> { model("dump/fir") }
//      testClass<AbstractDiagnosticTest> { model("diagnostic") }
    }
  }
}
