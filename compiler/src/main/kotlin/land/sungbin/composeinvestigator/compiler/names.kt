// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler

import org.jetbrains.kotlin.builtins.StandardNames.BUILT_INS_PACKAGE_FQ_NAME
import org.jetbrains.kotlin.builtins.StandardNames.COLLECTIONS_PACKAGE_FQ_NAME
import org.jetbrains.kotlin.builtins.StandardNames.FqNames
import org.jetbrains.kotlin.builtins.StandardNames.HASHCODE_NAME
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.Name.identifier
import org.jetbrains.kotlin.name.SpecialNames

// ===== PACKAGE ===== //

public const val AndroidxComposeRuntime: String = "androidx.compose.runtime"
public const val ComposeInvestigatorRuntime: String = "land.sungbin.composeinvestigator.runtime"

// ===== FULLY-QUALIFIED NAME ===== //

// START Kotlin/Java Standard Library
public val MUTABLE_LIST_OF_FQN: FqName = COLLECTIONS_PACKAGE_FQ_NAME.child(identifier("mutableListOf"))
public val MUTABLE_LIST_ADD_FQN: FqName = FqNames.mutableList.child(identifier("add"))

public val HASH_CODE_FQN: FqName = BUILT_INS_PACKAGE_FQ_NAME.child(HASHCODE_NAME)
// END Kotlin/Java Standard Library

// START Compose Runtime
public val CURRENT_COMPOSER_FQN: FqName = FqName("$AndroidxComposeRuntime.currentComposer")

public val COMPOSER_FQN: FqName = FqName("$AndroidxComposeRuntime.Composer")
public val Composer_SKIP_TO_GROUP_END: Name = identifier("skipToGroupEnd")
public val Composer_COMPOUND_KEY_HASH: Name = identifier("compoundKeyHash")

public val STATE_FQN: FqName = FqName("$AndroidxComposeRuntime.State")
public val STATE_OBJECT_FQN: FqName = FqName("$AndroidxComposeRuntime.snapshots.StateObject")
// END Compose Runtime

// START ComposableScope
public val COMPOSABLE_SCOPE_FQN: FqName = FqName("$ComposeInvestigatorRuntime.ComposableScope")
// END ComposableScope

// START NoInvestigation
public val NO_INVESTIGATION_FQN: FqName = FqName("$ComposeInvestigatorRuntime.NoInvestigation")
// END NoInvestigation

// START ComposeInvestigatorConfig
public val COMPOSE_INVESTIGATOR_CONFIG_FQN: FqName = FqName("$ComposeInvestigatorRuntime.ComposeInvestigatorConfig")
public val ComposeInvestigatorConfig_LOGGER: Name = identifier("logger")
// END ComposeInvestigatorConfig

// START ComposableInvalidationLogger
public val COMPOSABLE_INVALIDATION_LOGGER_FQN: FqName = FqName("$ComposeInvestigatorRuntime.ComposableInvalidationLogger")
public val ComposableInvalidationLogger_LOG: Name = identifier("log")

public val INVALIDATION_RESULT_FQN: FqName = FqName("$ComposeInvestigatorRuntime.InvalidationResult")
public val InvalidationResult_SKIPPED: Name = identifier("Skipped")
// END ComposableInvalidationLogger

// START ComposableInvalidationTraceTable
public val CURRENT_COMPOSABLE_INVALIDATION_TRACER_FQN: FqName = FqName("$ComposeInvestigatorRuntime.currentComposableInvalidationTracer")

public val COMPOSABLE_NAME_FQN: FqName = FqName("$ComposeInvestigatorRuntime.ComposableName")

public val COMPOSABLE_INVALIDATION_TRACE_TABLE_FQN: FqName = FqName("$ComposeInvestigatorRuntime.ComposableInvalidationTraceTable")
public val ComposableInvalidationTraceTable_CURRENT_COMPOSABLE_NAME: Name = identifier("currentComposableName")
public val ComposableInvalidationTraceTable_CURRENT_COMPOSABLE_KEY_NAME: Name = identifier("currentComposableKeyName")
public val ComposableInvalidationTraceTable_COMPUTE_INVALIDATION_REASON: Name = identifier("computeInvalidationReason")
public val ComposableInvalidationTraceTable_REGISTER_STATE_OBJECT: Name = identifier("registerStateObject")
// END ComposableInvalidationTraceTable

// START Stability
public val STABILITY_FQN: FqName = FqName("$ComposeInvestigatorRuntime.Stability")
public val Stability_CERTAIN: Name = identifier("Certain")
public val Stability_RUNTIME: Name = identifier("Runtime")
public val Stability_UNKNOWN: Name = identifier("Unknown")
public val Stability_PARAMETER: Name = identifier("Parameter")
public val Stability_COMBINED: Name = identifier("Combined")
// END Stability

// START ComposableInformation
public val COMPOSABLE_INFORMATION_FQN: FqName = FqName("$ComposeInvestigatorRuntime.ComposableInformation")
public val ComposableInformation_WITH_COMPOUND_KEY: Name = identifier("withCompoundKey")
// END ComposableInformation

// START ValueArguments
public val VALUE_ARGUMENT_FQN: FqName = FqName("$ComposeInvestigatorRuntime.ValueArgument")
// END ValueArguments

// TODO testing
public fun CallableId.Companion.fromFqName(fqName: FqName): CallableId {
  val paths = fqName.pathSegments()
  val lastUppercaseIndex = paths.indexOfLast { path -> path.asString().first().isUpperCase() }

  if (lastUppercaseIndex == paths.lastIndex) {
    // a.b.c.D -> packageName, callableName
    return CallableId(
      packageName = FqName(paths.subList(0, /* exclusive */ lastUppercaseIndex).joinToString(".", transform = Name::asString)),
      callableName = paths.last(),
    )
  }

  if (lastUppercaseIndex != -1) {
    val firstUppercaseIndex = paths.indexOfFirst { path -> path.asString().first().isUpperCase() }

    return if (firstUppercaseIndex == lastUppercaseIndex) {
      // a.b.c.D.e -> packageName, className, callableName
      CallableId(
        packageName = FqName(paths.subList(0, /* exclusive */ lastUppercaseIndex).joinToString(".", transform = Name::asString)),
        className = FqName(paths[lastUppercaseIndex].asString()),
        callableName = paths.last(),
      )
    } else {
      // a.b.c.D.E.f -> packageName, classNames callableName
      CallableId(
        packageName = FqName(paths.subList(0, /* exclusive */ firstUppercaseIndex).joinToString(".", transform = Name::asString)),
        className = FqName(paths.subList(firstUppercaseIndex, /* exclusive */ lastUppercaseIndex + 1).joinToString(".", transform = Name::asString)),
        callableName = paths.last(),
      )
    }
  }

  // a.b.c.d -> packageName, callableName
  check(lastUppercaseIndex == -1) { "The CallableId parsing logic for the given FqName is invalid. (fqName=${fqName.asString()})" }
  return CallableId(
    packageName = FqName(paths.subList(0, /* exclusive */ paths.lastIndex).joinToString(".", transform = Name::asString)),
    callableName = paths.last(),
  )
}

@Suppress("UnusedReceiverParameter")
public val SpecialNames.UNKNOWN_STRING: String get() = "<unknown>"
