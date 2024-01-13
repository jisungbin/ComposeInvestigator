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

// ===== FULLY-QUALIFIED NAME ===== //

// START Kotlin Standard Library
internal val MUTABLE_LIST_OF_FQN = FqName("kotlin.collections.mutableListOf")
internal val MUTABLE_LIST_ADD_FQN = FqName("kotlin.collections.MutableList.add")

internal val HASH_CODE_FQN = FqName("kotlin.hashCode")
// END Kotlin Standard Library

// START Compose Runtime
internal val COMPOSABLE_FQN = FqName("$AndroidxComposeRuntime.Composable")
internal val COMPOSER_FQN = FqName("$AndroidxComposeRuntime.Composer")

internal val Composer_SKIPPING = Name.identifier("skipping")
internal val Composer_SKIP_TO_GROUP_END = Name.identifier("skipToGroupEnd")

internal val SCOPE_UPDATE_SCOPE_FQN = FqName("$AndroidxComposeRuntime.ScopeUpdateScope")
internal val ScopeUpdateScope_UPDATE_SCOPE = Name.identifier("updateScope")

internal val STATE_FQN = FqName("$AndroidxComposeRuntime.State")
// END Compose Runtime

// START ComposeInvestigatorConfig
internal val COMPOSE_INVESTIGATOR_CONFIG_FQN = FqName("$ComposeInvestigatorRuntime.ComposeInvestigatorConfig")
internal val ComposeInvestigatorConfig_INVALIDATION_LOGGER = Name.identifier("invalidationLogger")
// END ComposeInvestigatorConfig

// START ComposableInvalidationLogger
internal val COMPOSABLE_INVALIDATION_LOGGER_FQN = FqName("$ComposeInvestigatorRuntime.ComposableInvalidationLogger")
internal val ComposableInvalidationLogger_INVOKE = Name.identifier("invoke")

internal val INVALIDATION_REASON_FQN = FqName("$ComposeInvestigatorRuntime.InvalidationReason")
internal val InvalidationReason_Invalidate = Name.identifier("Invalidate")

internal val COMPOSABLE_INVALIDATION_TYPE_FQN = FqName("$ComposeInvestigatorRuntime.ComposableInvalidationType")
internal val ComposableInvalidationType_PROCESSED = Name.identifier("Processed")
internal val ComposableInvalidationType_SKIPPED = Name.identifier("Skipped")
// END ComposableInvalidationLogger

// START ComposableInvalidationTrackTable
internal val CURRENT_COMPOSABLE_INVALIDATION_TRACKER_FQN = FqName("$ComposeInvestigatorRuntime.currentComposableInvalidationTracker")

internal val COMPOSABLE_NAME_FQN = FqName("$ComposeInvestigatorRuntime.ComposableName")

internal val COMPOSABLE_INVALIDATION_TRACK_TABLE_FQN = FqName("$ComposeInvestigatorRuntime.ComposableInvalidationTrackTable")
internal val ComposableInvalidationTrackTable_CURRENT_COMPOSABLE_NAME = Name.identifier("currentComposableName")
internal val ComposableInvalidationTrackTable_CURRENT_COMPOSABLE_KEY_NAME = Name.identifier("currentComposableKeyName")
internal val ComposableInvalidationTrackTable_CALL_LISTENERS = Name.identifier("callListeners")
internal val ComposableInvalidationTrackTable_COMPUTE_INVALIDATION_REASON = Name.identifier("computeInvalidationReason")
// END ComposableInvalidationTrackTable

// START StateObjectTracker
internal val REGISTER_STATE_OBJECT_TRACKING_FQN = FqName("$ComposeInvestigatorRuntime.registerStateObjectTracking")
// END StateObjectTracker

// START DeclarationStability
internal val DECLARATION_STABILITY_FQN = FqName("$ComposeInvestigatorRuntime.DeclarationStability")
internal val DeclarationStability_CERTAIN = Name.identifier("Certain")
internal val DeclarationStability_RUNTIME = Name.identifier("Runtime")
internal val DeclarationStability_UNKNOWN = Name.identifier("Unknown")
internal val DeclarationStability_PARAMETER = Name.identifier("Parameter")
internal val DeclarationStability_COMBINED = Name.identifier("Combined")
// END DeclarationStability

// START affect/AffectedField
internal val AFFECTED_FIELD_FQN = FqName("$ComposeInvestigatorRuntimeAffect.AffectedField")
internal val AffectedField_VALUE_PARAMETER = Name.identifier("ValueParameter")
// END affect/AffectableField

// START affect/AffectedComposable
internal val AFFECTED_COMPOSABLE_FQN = FqName("$ComposeInvestigatorRuntimeAffect.AffectedComposable")
// END affect/AffectedComposable

public fun CallableId.Companion.fromFqName(fqName: FqName): CallableId =
  CallableId(packageName = fqName.parent(), callableName = fqName.shortName())

@Suppress("UnusedReceiverParameter")
public val SpecialNames.UNKNOWN_STRING: String get() = "<unknown>"
