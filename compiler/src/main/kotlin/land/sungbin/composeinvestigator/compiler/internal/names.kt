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

// ===== FULLY-QUALIFIED NAME ===== //

// START Kotlin Standard Library
internal val MUTABLE_LIST_OF_FQN = FqName("kotlin.collections.mutableListOf")
internal val MUTABLE_LIST_ADD_FQN = FqName("kotlin.collections.MutableList.add")

internal val HASH_CODE_FQN = FqName("kotlin.hashCode")

internal val PAIR_FQN = FqName("kotlin.Pair")
// END Kotlin Standard Library

// START Compose Runtime
internal val COMPOSABLE_FQN = FqName("$AndroidxComposeRuntime.Composable")
internal val COMPOSER_FQN = FqName("$AndroidxComposeRuntime.Composer")
internal val COMPOSER_KT_FQN = FqName("$AndroidxComposeRuntime.ComposerKt")

internal val STATE_FQN = FqName("$AndroidxComposeRuntime.State")
internal val STATE_VALUE_FQN = STATE_FQN.child(Name.identifier("value"))
internal val STATE_VALUE_FQN_GETTER_INTRINSIC = STATE_FQN.child(Name.special("<get-value>"))
// END Compose Runtime

// START ComposeInvestigatorConfig
internal val COMPOSE_INVESTIGATOR_CONFIG_FQN = FqName("$ComposeInvestigatorRuntime.ComposeInvestigatorConfig")
internal val COMPOSE_INVESTIGATOR_CONFIG_INVALIDATION_LOGGER_FQN = COMPOSE_INVESTIGATOR_CONFIG_FQN.child(Name.identifier("invalidationLogger"))
internal val COMPOSE_INVESTIGATOR_CONFIG_INVALIDATION_LOGGER_FQN_GETTER_INTRINSIC = COMPOSE_INVESTIGATOR_CONFIG_FQN.child(Name.special("<get-invalidationLogger>"))
// END ComposeInvestigatorConfig

// START ComposableInvalidationLogger
internal val COMPOSABLE_INVALIDATION_LOGGER_FQN = FqName("$ComposeInvestigatorRuntime.ComposableInvalidationLogger")
internal val COMPOSABLE_INVALIDATION_LOGGER_INVOKE_FQN = COMPOSABLE_INVALIDATION_LOGGER_FQN.child(Name.identifier("invoke"))

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
// END ComposableInvalidationLogger

// START ComposableInvalidationTrackTable
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
// END ComposableInvalidationTrackTable

// START StateObjectTracker
// TODO
// END StateObjectTracker

// START DeclarationStability
internal val DECLARATION_STABILITY_FQN = FqName("$ComposeInvestigatorRuntime.DeclarationStability")
internal val DECLARATION_STABILITY_CERTAIN_FQN = DECLARATION_STABILITY_FQN.child(Name.identifier("Certain"))
internal val DECLARATION_STABILITY_RUNTIME_FQN = DECLARATION_STABILITY_FQN.child(Name.identifier("Runtime"))
internal val DECLARATION_STABILITY_UNKNOWN_FQN = DECLARATION_STABILITY_FQN.child(Name.identifier("Unknown"))
internal val DECLARATION_STABILITY_PARAMETER_FQN = DECLARATION_STABILITY_FQN.child(Name.identifier("Parameter"))
internal val DECLARATION_STABILITY_COMBINED_FQN = DECLARATION_STABILITY_FQN.child(Name.identifier("Combined"))
// END DeclarationStability

// START ComposableInvalidationEffect
internal val COMPOSABLE_INVALIDATION_EFFECT_FQN = FqName("$ComposeInvestigatorRuntime.ComposableInvalidationEffect")
// END ComposableInvalidationEffect

// START affect/AffectedField
internal val AFFECTED_FIELD_FQN = FqName("$ComposeInvestigatorRuntimeAffect.AffectedField")
internal val AFFECTED_FIELD_VALUE_PARAMETER_FQN = AFFECTED_FIELD_FQN.child(Name.identifier("ValueParameter"))
internal val AFFECTED_FIELD_STATE_PROPERTY_FQN = AFFECTED_FIELD_FQN.child(Name.identifier("StateProperty"))
// END affect/AffectableField

// START affect/AffectedComposable
internal val AFFECTED_COMPOSABLE_FQN = FqName("$ComposeInvestigatorRuntimeAffect.AffectedComposable")
// END affect/AffectedComposable

public fun CallableId.Companion.fromFqName(fqName: FqName): CallableId =
  CallableId(packageName = fqName.parent(), callableName = fqName.shortName())

@Suppress("UnusedReceiverParameter")
public val SpecialNames.UNKNOWN_STRING: String get() = "<unknown>"
