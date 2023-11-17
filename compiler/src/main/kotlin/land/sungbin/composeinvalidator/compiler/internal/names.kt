/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvalidator/blob/main/LICENSE
 */

package land.sungbin.composeinvalidator.compiler.internal

import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

private const val AndroidxComposeRuntime = "androidx.compose.runtime"
private const val ComposeInvalidatorRuntime = "land.sungbin.composeinvalidator.runtime"

// ===== NAME ===== //

internal val SKIP_TO_GROUP_END = Name.identifier("skipToGroupEnd")

internal val IS_TRACE_IN_PROGRESS = Name.identifier("isTraceInProgress")
internal val TRACE_EVENT_START = Name.identifier("traceEventStart")
internal val TRACE_EVENT_END = Name.identifier("traceEventEnd")

// ===== Fully-Qualified Name ===== //

internal val COMPOSABLE_FQN = FqName("$AndroidxComposeRuntime.Composable")
internal val COMPOSER_FQN = FqName("$AndroidxComposeRuntime.Composer")
internal val COMPOSER_KT_FQN = FqName("$AndroidxComposeRuntime.ComposerKt")

internal val PARAMETER_INFO_FQN = FqName("$ComposeInvalidatorRuntime.ParameterInfo")
internal val DIFF_PARAMS_FQN = FqName("$ComposeInvalidatorRuntime.DiffParams")
internal val COMPOSABLE_INVALIDATION_TRACK_TABLE_FQN =
  FqName("$ComposeInvalidatorRuntime.ComposableInvalidationTrackTable")

internal val DECLARATION_STABILITY_FQN = FqName("$ComposeInvalidatorRuntime.DeclarationStability")
internal val DECLARATION_STABILITY_CERTAIN_FQN = DECLARATION_STABILITY_FQN.child(Name.identifier("Certain"))
internal val DECLARATION_STABILITY_RUNTIME_FQN = DECLARATION_STABILITY_FQN.child(Name.identifier("Runtime"))
internal val DECLARATION_STABILITY_UNKNOWN_FQN = DECLARATION_STABILITY_FQN.child(Name.identifier("Unknown"))
internal val DECLARATION_STABILITY_PARAMETER_FQN = DECLARATION_STABILITY_FQN.child(Name.identifier("Parameter"))
internal val DECLARATION_STABILITY_COMBINED_FQN = DECLARATION_STABILITY_FQN.child(Name.identifier("Combined"))
