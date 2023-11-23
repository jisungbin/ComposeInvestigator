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

// START Compose Runtime
internal val COMPOSABLE_FQN = FqName("$AndroidxComposeRuntime.Composable")
internal val COMPOSER_FQN = FqName("$AndroidxComposeRuntime.Composer")
internal val COMPOSER_KT_FQN = FqName("$AndroidxComposeRuntime.ComposerKt")
// END Compose Runtime

// START ComposableInvalidationLogger.kt
internal val COMPOSABLE_INVALIDATION_LOGGER_FQN = FqName("$ComposeInvestigatorRuntime.ComposableInvalidationLogger")
internal val AFFECTED_COMPOSABLE_FQN = FqName("$ComposeInvestigatorRuntime.AffectedComposable")
internal val PARAMETER_INFO_FQN = FqName("$ComposeInvestigatorRuntime.ParameterInfo")

internal val INVALIDATION_REASON_FQN = FqName("$ComposeInvestigatorRuntime.InvalidationReason")
internal val INVALIDATION_REASON_INITIAL_FQN = INVALIDATION_REASON_FQN.child(Name.identifier("Initial"))
internal val INVALIDATION_REASON_PARAMETER_CHANGED_FQN = INVALIDATION_REASON_FQN.child(Name.identifier("ParameterChanged"))
internal val INVALIDATION_REASON_FORCE_FQN = INVALIDATION_REASON_FQN.child(Name.identifier("Force"))
internal val INVALIDATION_REASON_UNKNOWN_FQN = INVALIDATION_REASON_FQN.child(Name.identifier("Unknown"))

internal val COMPOSABLE_INVALIDATION_TYPE_FQN = FqName("$ComposeInvestigatorRuntime.ComposableInvalidationType")
internal val COMPOSABLE_INVALIDATION_TYPE_PROCESSED_FQN = COMPOSABLE_INVALIDATION_TYPE_FQN.child(Name.identifier("Processed"))
internal val COMPOSABLE_INVALIDATION_TYPE_SKIPPED_FQN = COMPOSABLE_INVALIDATION_TYPE_FQN.child(Name.identifier("Skipped"))
// END ComposableInvalidationLogger.kt

// START ComposableInvalidationTrackTable.kt
internal val CURRENT_COMPOSABLE_INVALIDATION_TRACKER_FQN = FqName("$ComposeInvestigatorRuntime.currentComposableInvalidationTracker")
internal val CURRENT_COMPOSABLE_INVALIDATION_TRACKER_FQN_GETTER_INTRINSIC = FqName("$ComposeInvestigatorRuntime.<get-currentComposableInvalidationTracker>")

internal val COMPOSABLE_INVALIDATION_LISTENER_FQN = FqName("$ComposeInvestigatorRuntime.ComposableInvalidationListener")
internal val COMPOSABLE_INVALIDATION_LISTENER_ON_INVALIDATE_FQN = COMPOSABLE_INVALIDATION_LISTENER_FQN.child(Name.identifier("onInvalidate"))

internal val COMPOSABLE_INVALIDATION_TRACK_TABLE_FQN = FqName("$ComposeInvestigatorRuntime.ComposableInvalidationTrackTable")
internal val COMPOSABLE_INVALIDATION_TRACK_TABLE_CURRENT_COMPOSABLE_NAME_FQN = COMPOSABLE_INVALIDATION_TRACK_TABLE_FQN.child(Name.identifier("currentComposableName"))
internal val COMPOSABLE_INVALIDATION_TRACK_TABLE_CURRENT_COMPOSABLE_NAME_FQN_GETTER_INTRINSIC = COMPOSABLE_INVALIDATION_TRACK_TABLE_FQN.child(Name.identifier("<get-currentComposableName>"))
internal val COMPOSABLE_INVALIDATION_TRACK_TABLE_CURRENT_COMPOSABLE_NAME_FQN_SETTER_INTRINSIC = COMPOSABLE_INVALIDATION_TRACK_TABLE_FQN.child(Name.identifier("<set-currentComposableName>"))
internal val COMPOSABLE_INVALIDATION_TRACK_TABLE_CURRENT_COMPOSABLE_KEY_NAME_FQN = COMPOSABLE_INVALIDATION_TRACK_TABLE_FQN.child(Name.identifier("currentComposableKeyName"))
internal val COMPOSABLE_INVALIDATION_TRACK_TABLE_CURRENT_COMPOSABLE_KEY_NAME_FQN_GETTER_INTRINSIC = COMPOSABLE_INVALIDATION_TRACK_TABLE_FQN.child(Name.identifier("<get-currentComposableKeyName>"))
internal val COMPOSABLE_INVALIDATION_TRACK_TABLE_CALL_LISTENERS_FQN = COMPOSABLE_INVALIDATION_TRACK_TABLE_FQN.child(Name.identifier("callListeners"))
internal val COMPOSABLE_INVALIDATION_TRACK_TABLE_COMPUTE_INVALIDATION_REASON_FQN = COMPOSABLE_INVALIDATION_TRACK_TABLE_FQN.child(Name.identifier("computeInvalidationReason"))

internal val DECLARATION_STABILITY_FQN = FqName("$ComposeInvestigatorRuntime.DeclarationStability")
internal val DECLARATION_STABILITY_CERTAIN_FQN = DECLARATION_STABILITY_FQN.child(Name.identifier("Certain"))
internal val DECLARATION_STABILITY_RUNTIME_FQN = DECLARATION_STABILITY_FQN.child(Name.identifier("Runtime"))
internal val DECLARATION_STABILITY_UNKNOWN_FQN = DECLARATION_STABILITY_FQN.child(Name.identifier("Unknown"))
internal val DECLARATION_STABILITY_PARAMETER_FQN = DECLARATION_STABILITY_FQN.child(Name.identifier("Parameter"))
internal val DECLARATION_STABILITY_COMBINED_FQN = DECLARATION_STABILITY_FQN.child(Name.identifier("Combined"))
// END ComposableInvalidationTrackTable.kt

// START ComposableInvalidationEffect.kt
internal val COMPOSABLE_INVALIDATION_EFFECT_FQN = FqName("$ComposeInvestigatorRuntime.ComposableInvalidationEffect")
// END ComposableInvalidationEffect.kt
