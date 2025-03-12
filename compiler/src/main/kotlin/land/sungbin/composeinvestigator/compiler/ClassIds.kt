// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler

import androidx.compose.compiler.plugins.kotlin.ComposeClassIds
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name.identifier

private val AndroidxComposeRuntimeRoot = FqName("androidx.compose.runtime")

@Suppress("UnusedReceiverParameter")
public val ComposeClassIds.StateObject
  get() = ClassId(AndroidxComposeRuntimeRoot.child(identifier("snapshots")), identifier("StateObject"))

public object InvestigatorClassIds {
  private val ROOT = FqName("land.sungbin.composeinvestigator.runtime")

  public val ComposableScope: ClassId = ClassId(ROOT, identifier("ComposableScope"))
  public val NoInvestigation: ClassId = ClassId(ROOT, identifier("NoInvestigation"))

  public val ComposeInvestigator: ClassId = ClassId(ROOT, identifier("ComposeInvestigator"))

  public val InvalidationLogger: ClassId = ClassId(ROOT, identifier("InvalidationLogger"))
  public val InvalidationResult: ClassId = ClassId(ROOT, identifier("InvalidationResult"))

  public val ComposableInformation: ClassId = ClassId(ROOT, identifier("ComposableInformation"))
  public val ValueArgument: ClassId = ClassId(ROOT, identifier("ValueArgument"))
  public val Stability: ClassId = ClassId(ROOT, identifier("Stability"))
}
