// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler.analysis

import org.jetbrains.kotlin.ir.expressions.IrConstructorCall

public data class ComposableKeyInfo(
  public val keyName: String,
  public val composable: IrConstructorCall,
)
