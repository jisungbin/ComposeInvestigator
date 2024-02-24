/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler

import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames

// ===== PACKAGE ===== //

public const val AndroidxComposeRuntime: String = "androidx.compose.runtime"
public const val ComposeInvestigatorRuntime: String = "land.sungbin.composeinvestigator.runtime"
public const val ComposeInvestigatorRuntimeAffect: String = "land.sungbin.composeinvestigator.runtime.affect"

// ===== FULLY-QUALIFIED NAME ===== //

// START Kotlin/Java Standard Library
public val ITERABLE_TO_LIST_FQN: FqName = FqName("kotlin.collections.toList")
public val EMPTY_LIST_FQN: FqName = FqName("kotlin.collections.emptyList")

public val MUTABLE_LIST_OF_FQN: FqName = FqName("kotlin.collections.mutableListOf")
public val MUTABLE_LIST_ADD_FQN: FqName = FqName("kotlin.collections.MutableList.add")

public val HASH_CODE_FQN: FqName = FqName("kotlin.hashCode")

public val STACK_FQN: FqName = FqName("java.util.Stack")
public val Stack_PUSH: Name = Name.identifier("push")
public val Stack_POP: Name = Name.identifier("pop")
// END Kotlin/Java Standard Library

// START Compose Runtime
public val COMPOSER_FQN: FqName = FqName("$AndroidxComposeRuntime.Composer")

public val Composer_SKIPPING: Name = Name.identifier("skipping")
public val Composer_SKIP_TO_GROUP_END: Name = Name.identifier("skipToGroupEnd")

public val SCOPE_UPDATE_SCOPE_FQN: FqName = FqName("$AndroidxComposeRuntime.ScopeUpdateScope")
public val ScopeUpdateScope_UPDATE_SCOPE: Name = Name.identifier("updateScope")

public val STATE_FQN: FqName = FqName("$AndroidxComposeRuntime.State")
// END Compose Runtime

// START Compose Animation
public val ANIMATABLE_FQN: FqName = FqName("androidx.compose.animation.core.Animatable")
// END Compose Animation

// START ComposeInvestigatorConfig
public val COMPOSE_INVESTIGATOR_CONFIG_FQN: FqName = FqName("$ComposeInvestigatorRuntime.ComposeInvestigatorConfig")
public val ComposeInvestigatorConfig_INVALIDATION_LOGGER: Name = Name.identifier("invalidationLogger")
// END ComposeInvestigatorConfig

// START ComposableInvalidationLogger
public val COMPOSABLE_INVALIDATION_LOGGER_FQN: FqName = FqName("$ComposeInvestigatorRuntime.ComposableInvalidationLogger")
public val ComposableInvalidationLogger_INVOKE: Name = Name.identifier("invoke")

public val INVALIDATION_REASON_FQN: FqName = FqName("$ComposeInvestigatorRuntime.InvalidationReason")
public val InvalidationReason_Invalidate: Name = Name.identifier("Invalidate")

public val COMPOSABLE_INVALIDATION_TYPE_FQN: FqName = FqName("$ComposeInvestigatorRuntime.ComposableInvalidationType")
public val ComposableInvalidationType_PROCESSED: Name = Name.identifier("Processed")
public val ComposableInvalidationType_SKIPPED: Name = Name.identifier("Skipped")
// END ComposableInvalidationLogger

// START ComposableInvalidationTrackTable
public val CURRENT_COMPOSABLE_INVALIDATION_TRACKER_FQN: FqName = FqName("$ComposeInvestigatorRuntime.currentComposableInvalidationTracker")

public val COMPOSABLE_NAME_FQN: FqName = FqName("$ComposeInvestigatorRuntime.ComposableName")

public val COMPOSABLE_INVALIDATION_TRACK_TABLE_FQN: FqName = FqName("$ComposeInvestigatorRuntime.ComposableInvalidationTrackTable")
public val ComposableInvalidationTrackTable_CURRENT_COMPOSABLE_NAME: Name = Name.identifier("currentComposableName")
public val ComposableInvalidationTrackTable_CURRENT_COMPOSABLE_KEY_NAME: Name = Name.identifier("currentComposableKeyName")
public val ComposableInvalidationTrackTable_CALL_LISTENERS: Name = Name.identifier("callListeners")
public val ComposableInvalidationTrackTable_COMPUTE_INVALIDATION_REASON: Name = Name.identifier("computeInvalidationReason")
// END ComposableInvalidationTrackTable

// START StateObjectTracker
public val REGISTER_STATE_OBJECT_TRACKING_FQN: FqName = FqName("$ComposeInvestigatorRuntime.registerStateObjectTracking")
// END StateObjectTracker

// START DeclarationStability
public val DECLARATION_STABILITY_FQN: FqName = FqName("$ComposeInvestigatorRuntime.DeclarationStability")
public val DeclarationStability_CERTAIN: Name = Name.identifier("Certain")
public val DeclarationStability_RUNTIME: Name = Name.identifier("Runtime")
public val DeclarationStability_UNKNOWN: Name = Name.identifier("Unknown")
public val DeclarationStability_PARAMETER: Name = Name.identifier("Parameter")
public val DeclarationStability_COMBINED: Name = Name.identifier("Combined")
// END DeclarationStability

// START affect/AffectedField
public val AFFECTED_FIELD_FQN: FqName = FqName("$ComposeInvestigatorRuntimeAffect.AffectedField")
public val AffectedField_VALUE_PARAMETER: Name = Name.identifier("ValueParameter")
// END affect/AffectableField

// START affect/AffectedComposable
public val AFFECTED_COMPOSABLE_FQN: FqName = FqName("$ComposeInvestigatorRuntimeAffect.AffectedComposable")
// END affect/AffectedComposable

public fun CallableId.Companion.fromFqName(fqName: FqName): CallableId =
  CallableId(packageName = fqName.parent(), callableName = fqName.shortName())

@Suppress("UnusedReceiverParameter")
public val SpecialNames.UNKNOWN_STRING: String get() = "<unknown>"
