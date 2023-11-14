/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvalidator/blob/main/LICENSE
 */

package land.sungbin.composeinvalidator.compiler.internal.origin

import org.jetbrains.kotlin.ir.expressions.IrStatementOriginImpl

internal data object InvalidationTrackableOrigin : IrStatementOriginImpl("INVALIDATION_TRACKABLE_ORIGIN")
