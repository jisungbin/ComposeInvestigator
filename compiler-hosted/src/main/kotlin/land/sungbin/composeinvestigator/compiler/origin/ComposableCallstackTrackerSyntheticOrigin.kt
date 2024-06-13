/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.origin

import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOriginImpl

public val ComposableCallstackTrackerSyntheticOrigin: IrDeclarationOrigin =
  IrDeclarationOriginImpl("GENERATED_COMPOSABLE_CALLSTACK_TRACKER_MEMBER", isSynthetic = true)
