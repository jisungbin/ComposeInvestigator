/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.origin

import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin

public data object ComposableCallstackTrackerSyntheticOrigin : IrDeclarationOrigin {
  override val name: String = "GENERATED_COMPOSABLE_CALLSTACK_TRACKER_MEMBER"
  override val isSynthetic: Boolean = true
}
