/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.origin

import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin

public data object ComposableInvalidationTrackerOrigin : IrStatementOrigin {
  override val debugName: String = "GENERATED_COMPOSABLE_INVALIDATION_TRACKER_MEMBER"
}
