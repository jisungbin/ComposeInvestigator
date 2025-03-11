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

public val ANDROIDX_COMPOSE_RUNTIME_FQN: FqName = FqName("androidx.compose.runtime")
public val COMPOSE_INVESTIGATOR_RUNTIME_FQN: FqName = FqName("land.sungbin.composeinvestigator.runtime")

public object StdlibNames {
  public val mutableListOf: FqName = COLLECTIONS_PACKAGE_FQ_NAME.child(identifier("mutableListOf"))
  public val mutableListAdd: FqName = FqNames.mutableList.child(identifier("add"))

  public val hashCode: FqName = BUILT_INS_PACKAGE_FQ_NAME.child(HASHCODE_NAME)
}

public object ComposeNames {
  public val composer: FqName = ANDROIDX_COMPOSE_RUNTIME_FQN.child(identifier("Composer"))
  public val composerSkipToGroupEnd: FqName = composer.child(identifier("skipToGroupEnd"))
  public val composerCompoundKeyHash: FqName = composer.child(identifier("compoundKeyHash"))
  public val currentComposer: FqName = ANDROIDX_COMPOSE_RUNTIME_FQN.child(identifier("currentComposer"))

  public val state: FqName = ANDROIDX_COMPOSE_RUNTIME_FQN.child(identifier("State"))
  public val stateObject: FqName = ANDROIDX_COMPOSE_RUNTIME_FQN.child(identifier("snapshots.StateObject"))
}

public object InvestigatorNames {
  public val composableScope: FqName = COMPOSE_INVESTIGATOR_RUNTIME_FQN.child(identifier("ComposableScope"))
  public val noInvestigation: FqName = COMPOSE_INVESTIGATOR_RUNTIME_FQN.child(identifier("NoInvestigation"))

  public val composeInvestigator: FqName = COMPOSE_INVESTIGATOR_RUNTIME_FQN.child(identifier("ComposeInvestigator"))
  public val composeInvestigatorCurrentComposablenName: FqName = composeInvestigator.child(identifier("currentComposableName"))
  public val composeInvestigatorRegisterStateObject: FqName = composeInvestigator.child(identifier("registerStateObject"))
  public val composeInvestigatorComputeInvalidationReason: FqName = composeInvestigator.child(identifier("computeInvalidationReason"))
  public val composeInvestigatorLogger: FqName = composeInvestigator.child(identifier("Companion")).child(identifier("Logger"))
  public val currentComposeInvestigator: FqName = COMPOSE_INVESTIGATOR_RUNTIME_FQN.child(identifier("currentComposeInvestigator"))

  public val invalidationLogger: FqName = COMPOSE_INVESTIGATOR_RUNTIME_FQN.child(identifier("InvalidationLogger"))
  public val invalidationLoggerLog: FqName = invalidationLogger.child(identifier("log"))

  public val invalidationResult: FqName = COMPOSE_INVESTIGATOR_RUNTIME_FQN.child(identifier("InvalidationResult"))
  public val invalidationResultSkipped: FqName = invalidationResult.child(identifier("Skipped"))

  public val composableName: FqName = COMPOSE_INVESTIGATOR_RUNTIME_FQN.child(identifier("ComposableName"))
  public val composableInformation: FqName = COMPOSE_INVESTIGATOR_RUNTIME_FQN.child(identifier("ComposableInformation"))
  public val valueArgument: FqName = COMPOSE_INVESTIGATOR_RUNTIME_FQN.child(identifier("ValueArgument"))

  public val stability: FqName = COMPOSE_INVESTIGATOR_RUNTIME_FQN.child(identifier("Stability"))
  public val stabilityCertain: FqName = stability.child(identifier("Certain"))
  public val stabilityRuntime: FqName = stability.child(identifier("Runtime"))
  public val stabilityParameter: FqName = stability.child(identifier("Parameter"))
  public val stabilityUnknown: FqName = stability.child(identifier("Unknown"))
  public val stabilityCombined: FqName = stability.child(identifier("Combined"))
}

/**
 * Attempts to parse the given [fqName][FqName] and convert it to a [CallableId]. If the conversion
 * to [CallableId] fails, an [IllegalStateException] is thrown.
 */
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
      // a.b.c.D.E.f -> packageName, classNames, callableName
      CallableId(
        packageName = FqName(paths.subList(0, /* exclusive */ firstUppercaseIndex).joinToString(".", transform = Name::asString)),
        className = FqName(paths.subList(firstUppercaseIndex, /* exclusive */ lastUppercaseIndex + 1).joinToString(".", transform = Name::asString)),
        callableName = paths.last(),
      )
    }
  }

  // a.b.c.d -> packageName, callableName
  check(lastUppercaseIndex == -1) { "Failed to parses CallableId. (fqName=${fqName.asString()})" }
  return CallableId(
    packageName = FqName(paths.subList(0, /* exclusive */ paths.lastIndex).joinToString(".", transform = Name::asString)),
    callableName = paths.last(),
  )
}

@Suppress("UnusedReceiverParameter")
public val SpecialNames.UNKNOWN_STRING: String get() = "<unknown>"
