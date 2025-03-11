// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler

import androidx.compose.compiler.plugins.kotlin.ComposeClassIds
import org.jetbrains.kotlin.builtins.StandardNames.BUILT_INS_PACKAGE_FQ_NAME
import org.jetbrains.kotlin.builtins.StandardNames.COLLECTIONS_PACKAGE_FQ_NAME
import org.jetbrains.kotlin.builtins.StandardNames.FqNames
import org.jetbrains.kotlin.builtins.StandardNames.HASHCODE_NAME
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.Name.identifier

public object StdlibFqNames {
  public val mutableListOf: FqName = COLLECTIONS_PACKAGE_FQ_NAME.child(identifier("mutableListOf"))
  public val mutableListAdd: FqName = FqNames.mutableList.child(identifier("add"))

  public val hashCode: FqName = BUILT_INS_PACKAGE_FQ_NAME.child(HASHCODE_NAME)
}

public object ComposeFqNames {
  private val ROOT = FqName("androidx.compose.runtime")

  public val StateObject: FqName = ROOT.child(identifier("snapshots")).child(identifier("StateObject"))
}

public object InvestigatorFqNames {
  private val ROOT = FqName("land.sungbin.composeinvestigator.runtime")

  public val ComposableScope: FqName = ROOT.child(identifier("ComposableScope"))
  public val NoInvestigation: FqName = ROOT.child(identifier("NoInvestigation"))

  public val ComposeInvestigator: FqName = ROOT.child(identifier("ComposeInvestigator"))
  public val currentComposeInvestigator: FqName = ROOT.child(identifier("currentComposeInvestigator"))

  public val InvalidationLogger: FqName = ROOT.child(identifier("InvalidationLogger"))
  public val InvalidationResult: FqName = ROOT.child(identifier("InvalidationResult"))

  public val ComposableInformation: FqName = ROOT.child(identifier("ComposableInformation"))
  public val ValueArgument: FqName = ROOT.child(identifier("ValueArgument"))

  public val Stability: FqName = ROOT.child(identifier("Stability"))
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
