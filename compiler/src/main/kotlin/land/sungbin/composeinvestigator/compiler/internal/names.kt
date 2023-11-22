/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.internal

import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

private const val AndroidxComposeRuntime = "androidx.compose.runtime"
private const val ComposeInvestigatorRuntime = "land.sungbin.composeinvestigator.runtime"

// ===== NAME ===== //

internal val SKIP_TO_GROUP_END = Name.identifier("skipToGroupEnd")

internal val IS_TRACE_IN_PROGRESS = Name.identifier("isTraceInProgress")
internal val TRACE_EVENT_START = Name.identifier("traceEventStart")
internal val TRACE_EVENT_END = Name.identifier("traceEventEnd")

// ===== Fully-Qualified Name ===== //

internal val COMPOSABLE_FQN = FqName("$AndroidxComposeRuntime.Composable")
internal val COMPOSER_FQN = FqName("$AndroidxComposeRuntime.Composer")
internal val COMPOSER_KT_FQN = FqName("$AndroidxComposeRuntime.ComposerKt")

internal val COMPOSE_INVESTIGATE_LOGGER_FQN = FqName("$ComposeInvestigatorRuntime.ComposeInvestigateLogger")
internal val AFFECTED_COMPOSABLE_FQN = FqName("$ComposeInvestigatorRuntime.AffectedComposable")

internal val COMPOSABLE_INVALIDATE_TYPE_FQN = FqName("$ComposeInvestigatorRuntime.ComposableInvalidateType")
internal val COMPOSABLE_INVALIDATE_TYPE_PROCESSED_FQN = COMPOSABLE_INVALIDATE_TYPE_FQN.child(Name.identifier("Processed"))
internal val COMPOSABLE_INVALIDATE_TYPE_SKIPPED_FQN = COMPOSABLE_INVALIDATE_TYPE_FQN.child(Name.identifier("Skipped"))

internal val PARAMETER_INFO_FQN = FqName("$ComposeInvestigatorRuntime.ParameterInfo")

internal val CURRENT_COMPOSABLE_INVALIDATION_TRACKER_FQN = FqName("$ComposeInvestigatorRuntime.currentComposableInvalidationTracker")

internal val COMPOSABLE_INVALIDATION_TRACK_TABLE_FQN = FqName("$ComposeInvestigatorRuntime.ComposableInvalidationTrackTable")
internal val COMPOSABLE_INVALIDATION_TRACK_TABLE_CURRENT_COMPOSABLE_NAME_FQN = COMPOSABLE_INVALIDATION_TRACK_TABLE_FQN.child(Name.identifier("currentComposableName"))
internal val COMPOSABLE_INVALIDATION_TRACK_TABLE_CURRENT_COMPOSABLE_KEY_NAME_FQN = COMPOSABLE_INVALIDATION_TRACK_TABLE_FQN.child(Name.identifier("currentComposableKeyName"))
internal val COMPOSABLE_INVALIDATION_TRACK_TABLE_CALL_LISTENERS_FQN = COMPOSABLE_INVALIDATION_TRACK_TABLE_FQN.child(Name.identifier("callListeners"))
internal val COMPOSABLE_INVALIDATION_TRACK_TABLE_COMPUTE_DIFF_PARAMS_IF_PRESENT_FQN = COMPOSABLE_INVALIDATION_TRACK_TABLE_FQN.child(Name.identifier("computeDiffParamsIfPresent"))

internal val DECLARATION_STABILITY_FQN = FqName("$ComposeInvestigatorRuntime.DeclarationStability")
internal val DECLARATION_STABILITY_CERTAIN_FQN = DECLARATION_STABILITY_FQN.child(Name.identifier("Certain"))
internal val DECLARATION_STABILITY_RUNTIME_FQN = DECLARATION_STABILITY_FQN.child(Name.identifier("Runtime"))
internal val DECLARATION_STABILITY_UNKNOWN_FQN = DECLARATION_STABILITY_FQN.child(Name.identifier("Unknown"))
internal val DECLARATION_STABILITY_PARAMETER_FQN = DECLARATION_STABILITY_FQN.child(Name.identifier("Parameter"))
internal val DECLARATION_STABILITY_COMBINED_FQN = DECLARATION_STABILITY_FQN.child(Name.identifier("Combined"))
