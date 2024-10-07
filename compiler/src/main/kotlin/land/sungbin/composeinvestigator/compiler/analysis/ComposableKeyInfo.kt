// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler.analysis

import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import androidx.compose.compiler.plugins.kotlin.lower.DurableKeyTransformer

/**
 * Holds information about the Composable function to be visited by the
 * ComposeInvestigator.
 *
 * Calculated from [DurableComposableKeyAnalyzer] visitor.
 *
 * @param keyName Unique path of the Composable function, computed from
 * [DurableKeyTransformer.buildKey].
 * @param composable Constructor call of a `ComposableInformation` for the
 * current Composable function. No `compoundKey` is provided as a
 * [value arguments][IrConstructorCall.valueArguments] for this call.
 */
public data class ComposableKeyInfo(
  public val keyName: String,
  public val composable: IrConstructorCall,
)
