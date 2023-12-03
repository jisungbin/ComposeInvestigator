/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.internal

import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames

// ===== PACKAGE ===== //

private const val AndroidxComposeRuntime = "androidx.compose.runtime"
private const val ComposeInvestigatorRuntime = "land.sungbin.composeinvestigator.runtime"
private const val ComposeInvestigatorRuntimeAffect = "land.sungbin.composeinvestigator.runtime.affect"

// ===== NAME ===== //

internal val SKIP_TO_GROUP_END = Name.identifier("skipToGroupEnd")

internal val IS_TRACE_IN_PROGRESS = Name.identifier("isTraceInProgress")
internal val TRACE_EVENT_START = Name.identifier("traceEventStart")
internal val TRACE_EVENT_END = Name.identifier("traceEventEnd")

// ===== FULLY-QUALIFIED NAME ===== //

// START Kotlin Standard Library
// FIXME: There is a `functionN` factory in `IrBuiltIns`, but it currently produces unbound symbols.
//        We can switch to this and remove this function once KT-54230 is fixed.
internal val FUNCTION_2_FQN = FqName("kotlin.jvm.functions.Function2")
internal val FUNCTION_2_INVOKE_FQN = FUNCTION_2_FQN.child(Name.identifier("invoke"))

internal val MUTABLE_LIST_OF_FQN = FqName("kotlin.collections.mutableListOf")
internal val MUTABLE_LIST_ADD_FQN = FqName("kotlin.collections.MutableList.add")

internal val HASH_CODE_FQN = FqName("kotlin.hashCode")
// END Kotlin Standard Library

// START Compose Runtime
internal val COMPOSABLE_FQN = FqName("$AndroidxComposeRuntime.Composable")
internal val COMPOSER_FQN = FqName("$AndroidxComposeRuntime.Composer")
internal val COMPOSER_KT_FQN = FqName("$AndroidxComposeRuntime.ComposerKt")

internal val STATE_FQN = FqName("$AndroidxComposeRuntime.State")
internal val STATE_VALUE_FQN = STATE_FQN.child(Name.identifier("value"))
internal val STATE_VALUE_FQN_GETTER_INTRINSIC = STATE_FQN.child(Name.special("<get-value>"))
// END Compose Runtime

// START ComposeInvestigatorConfig.kt
internal val COMPOSE_INVESTIGATOR_CONFIG_FQN = FqName("$ComposeInvestigatorRuntime.ComposeInvestigatorConfig")
internal val COMPOSE_INVESTIGATOR_CONFIG_INVALIDATION_LOGGER_FQN = COMPOSE_INVESTIGATOR_CONFIG_FQN.child(Name.identifier("invalidationLogger"))
internal val COMPOSE_INVESTIGATOR_CONFIG_INVALIDATION_LOGGER_FQN_GETTER_INTRINSIC = COMPOSE_INVESTIGATOR_CONFIG_FQN.child(Name.special("<get-invalidationLogger>"))
// END ComposeInvestigatorConfig.kt

// START ComposableInvalidationLogger.kt
internal val SIMPLE_PARAMETER = FqName("$ComposeInvestigatorRuntime.SimpleParameter")
internal val CHANGED_FIELD_PAIR = FqName("$ComposeInvestigatorRuntime.ChangedFieldPair")

internal val INVALIDATION_REASON_FQN = FqName("$ComposeInvestigatorRuntime.InvalidationReason")
internal val INVALIDATION_REASON_INITIAL_FQN = INVALIDATION_REASON_FQN.child(Name.identifier("Initial"))
internal val INVALIDATION_REASON_FIELD_CHANGED_FQN = INVALIDATION_REASON_FQN.child(Name.identifier("FieldChanged"))
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

internal val COMPOSABLE_NAME_FQN = FqName("$ComposeInvestigatorRuntime.ComposableName")

internal val COMPOSABLE_INVALIDATION_TRACK_TABLE_FQN = FqName("$ComposeInvestigatorRuntime.ComposableInvalidationTrackTable")
internal val COMPOSABLE_INVALIDATION_TRACK_TABLE_CURRENT_COMPOSABLE_NAME_FQN = COMPOSABLE_INVALIDATION_TRACK_TABLE_FQN.child(Name.identifier("currentComposableName"))
internal val COMPOSABLE_INVALIDATION_TRACK_TABLE_CURRENT_COMPOSABLE_NAME_FQN_GETTER_INTRINSIC = COMPOSABLE_INVALIDATION_TRACK_TABLE_FQN.child(Name.special("<get-currentComposableName>"))
internal val COMPOSABLE_INVALIDATION_TRACK_TABLE_CURRENT_COMPOSABLE_NAME_FQN_SETTER_INTRINSIC = COMPOSABLE_INVALIDATION_TRACK_TABLE_FQN.child(Name.special("<set-currentComposableName>"))
internal val COMPOSABLE_INVALIDATION_TRACK_TABLE_CURRENT_COMPOSABLE_KEY_NAME_FQN = COMPOSABLE_INVALIDATION_TRACK_TABLE_FQN.child(Name.identifier("currentComposableKeyName"))
internal val COMPOSABLE_INVALIDATION_TRACK_TABLE_CURRENT_COMPOSABLE_KEY_NAME_FQN_GETTER_INTRINSIC = COMPOSABLE_INVALIDATION_TRACK_TABLE_FQN.child(Name.special("<get-currentComposableKeyName>"))
internal val COMPOSABLE_INVALIDATION_TRACK_TABLE_REGISTER_LISTENER_FQN = COMPOSABLE_INVALIDATION_TRACK_TABLE_FQN.child(Name.identifier("registerListener"))
internal val COMPOSABLE_INVALIDATION_TRACK_TABLE_UNREGISTER_LISTENER_FQN = COMPOSABLE_INVALIDATION_TRACK_TABLE_FQN.child(Name.identifier("unregisterListener"))
internal val COMPOSABLE_INVALIDATION_TRACK_TABLE_CALL_LISTENERS_FQN = COMPOSABLE_INVALIDATION_TRACK_TABLE_FQN.child(Name.identifier("callListeners"))
internal val COMPOSABLE_INVALIDATION_TRACK_TABLE_COMPUTE_INVALIDATION_REASON_FQN = COMPOSABLE_INVALIDATION_TRACK_TABLE_FQN.child(Name.identifier("computeInvalidationReason"))
// END ComposableInvalidationTrackTable.kt

// START DeclarationStability.kt
internal val DECLARATION_STABILITY_FQN = FqName("$ComposeInvestigatorRuntime.DeclarationStability")
internal val DECLARATION_STABILITY_CERTAIN_FQN = DECLARATION_STABILITY_FQN.child(Name.identifier("Certain"))
internal val DECLARATION_STABILITY_RUNTIME_FQN = DECLARATION_STABILITY_FQN.child(Name.identifier("Runtime"))
internal val DECLARATION_STABILITY_UNKNOWN_FQN = DECLARATION_STABILITY_FQN.child(Name.identifier("Unknown"))
internal val DECLARATION_STABILITY_PARAMETER_FQN = DECLARATION_STABILITY_FQN.child(Name.identifier("Parameter"))
internal val DECLARATION_STABILITY_COMBINED_FQN = DECLARATION_STABILITY_FQN.child(Name.identifier("Combined"))
// END DeclarationStability.kt

// START ComposableInvalidationEffect.kt
internal val COMPOSABLE_INVALIDATION_EFFECT_FQN = FqName("$ComposeInvestigatorRuntime.ComposableInvalidationEffect")
// END ComposableInvalidationEffect.kt

// START affect/AffectedField.kt
internal val AFFECTED_FIELD_FQN = FqName("$ComposeInvestigatorRuntimeAffect.AffectedField")
internal val AFFECTED_FIELD_VALUE_PARAMETER_FQN = AFFECTED_FIELD_FQN.child(Name.identifier("ValueParameter"))
internal val AFFECTED_FIELD_STATE_PROPERTY_FQN = AFFECTED_FIELD_FQN.child(Name.identifier("StateProperty"))
// END affect/AffectableField.kt

// START affect/AffectedComposable.kt
internal val AFFECTED_COMPOSABLE_FQN = FqName("$ComposeInvestigatorRuntimeAffect.AffectedComposable")
// END affect/AffectedComposable.kt

// START helper/IrHelper.kt
internal val OBTAIN_STATE_PROPERTY_AND_ADD_FQN = FqName("$ComposeInvestigatorRuntime.helper.obtainStatePropertyAndAdd")
// END helper/IrHelper.kt

public fun CallableId.Companion.fromFqName(fqName: FqName): CallableId =
  CallableId(packageName = fqName.parent(), callableName = fqName.shortName())

@Suppress("UnusedReceiverParameter")
public val SpecialNames.UNKNOWN_STRING: String get() = "<unknown>"
