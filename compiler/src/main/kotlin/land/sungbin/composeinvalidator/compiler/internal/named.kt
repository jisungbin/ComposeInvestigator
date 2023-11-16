/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvalidator/blob/main/LICENSE
 */

package land.sungbin.composeinvalidator.compiler.internal

import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

internal val SKIP_TO_GROUP_END = Name.identifier("skipToGroupEnd")

internal val IS_TRACE_IN_PROGRESS = Name.identifier("isTraceInProgress")
internal val TRACE_EVENT_START = Name.identifier("traceEventStart")
internal val TRACE_EVENT_END = Name.identifier("traceEventEnd")

internal val COMPOSABLE_FQN = FqName("androidx.compose.runtime.Composable")
internal val COMPOSER_FQN = FqName("androidx.compose.runtime.Composer")
internal val COMPOSER_KT_FQN = FqName("androidx.compose.runtime.ComposerKt")