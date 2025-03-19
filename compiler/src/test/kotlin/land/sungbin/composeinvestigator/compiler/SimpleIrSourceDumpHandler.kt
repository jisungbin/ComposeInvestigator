// Copyright 2025 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler

import androidx.compose.compiler.plugins.kotlin.lower.dumpSrc
import org.jetbrains.kotlin.test.backend.handlers.AbstractIrHandler
import org.jetbrains.kotlin.test.backend.handlers.IrPrettyKotlinDumpHandler
import org.jetbrains.kotlin.test.backend.ir.IrBackendInput
import org.jetbrains.kotlin.test.model.BackendKind
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.moduleStructure
import org.jetbrains.kotlin.test.utils.MultiModuleInfoDumper
import org.jetbrains.kotlin.test.utils.withExtension

class SimpleIrSourceDumpHandler(
  services: TestServices,
  artifactKind: BackendKind<IrBackendInput>,
) : AbstractIrHandler(services, artifactKind) {
  private val baseDumper = MultiModuleInfoDumper()

  override fun processModule(module: TestModule, info: IrBackendInput) {
    val builder = baseDumper.builderForModule(module)

    for (file in info.irModuleFragment.files) {
      builder.append(file.dumpSrc(useFir = true))
    }
  }

  override fun processAfterAllModules(someAssertionWasFailed: Boolean) {
    val moduleStructure = testServices.moduleStructure

    // Always have a single file:
    //  https://github.com/JetBrains/kotlin/blob/9d1cf3aaa69bcc5f5060e8eebe05cfa81722419b/compiler/tests-common-new/tests/org/jetbrains/kotlin/test/services/impl/ModuleStructureExtractorImpl.kt#L54
    val defaultExpectedFile = moduleStructure.originalTestDataFiles.single().withExtension(IrPrettyKotlinDumpHandler.DUMP_EXTENSION)
    assertions.assertEqualsToFile(defaultExpectedFile, baseDumper.generateResultingDump())
  }
}
