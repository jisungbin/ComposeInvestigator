// Copyright 2025 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler

import org.jetbrains.kotlin.test.backend.handlers.AbstractIrHandler
import org.jetbrains.kotlin.test.backend.ir.IrBackendInput
import org.jetbrains.kotlin.test.model.BackendKind
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.TestServices

class IrSourceDumpHandler(
  services: TestServices,
  artifactKind: BackendKind<IrBackendInput>,
) : AbstractIrHandler(services, artifactKind) {
  override fun processModule(module: TestModule, info: IrBackendInput) {
    TODO("Not yet implemented")
  }

  override fun processAfterAllModules(someAssertionWasFailed: Boolean) {
    TODO("Not yet implemented")
  }
}
