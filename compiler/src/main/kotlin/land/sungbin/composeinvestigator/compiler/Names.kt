// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler

import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.Name.identifier
import org.jetbrains.kotlin.name.SpecialNames

public object ComposeNames {
  public val skipToGroupEnd: Name = identifier("skipToGroupEnd")
  public val compoundKeyHash: Name = identifier("compoundKeyHash")
}

public object InvestigatorNames {
  public val getComposableName: Name = identifier("getComposableName")
  public val registerStateObject: Name = identifier("registerStateObject")
  public val computeInvalidationReason: Name = identifier("computeInvalidationReason")

  public val Logger: Name = identifier("Logger")
  public val log: Name = identifier("log")
  public val Skipped: Name = identifier("Skipped")

  public val Certain: Name = identifier("Certain")
  public val Runtime: Name = identifier("Runtime")
  public val Parameter: Name = identifier("Parameter")
  public val Unknown: Name = identifier("Unknown")
  public val Combined: Name = identifier("Combined")
}

@Suppress("UnusedReceiverParameter")
public val SpecialNames.UNKNOWN_STRING: String get() = "<unknown>"
