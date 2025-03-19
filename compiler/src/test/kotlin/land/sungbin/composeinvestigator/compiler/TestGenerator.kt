// Copyright 2025 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler

import org.jetbrains.kotlin.generators.generateTestGroupSuiteWithJUnit5

fun main() {
  System.setProperty("java.awt.headless", "true")

  generateTestGroupSuiteWithJUnit5 {
    testGroup(
      testDataRoot = "compiler/src/testData",
      testsRoot = "compiler/src/test-generated",
    ) {
      testClass<AbstractComposeInvestigatorFirJvmIrTest> { model("codegen") }
    }
  }
}
