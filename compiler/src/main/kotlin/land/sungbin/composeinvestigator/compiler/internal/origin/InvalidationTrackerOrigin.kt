/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.internal.origin

import org.jetbrains.kotlin.ir.expressions.IrStatementOriginImpl

public data object InvalidationTrackerOrigin : IrStatementOriginImpl("GENERATED_INVALIDATION_TRACKER_MEMBER")
