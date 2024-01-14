/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.origin

import org.jetbrains.kotlin.ir.expressions.IrStatementOriginImpl

public data object StateChangeTrackerOrigin : IrStatementOriginImpl("GENERATED_STATE_CHANGE_TRACKER_MEMBER")
