// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler.test.assertion

import assertk.Assert
import assertk.assertions.isEqualTo
import assertk.fail
import land.sungbin.composeinvestigator.compiler.test.Investigated
import land.sungbin.composeinvestigator.runtime.InvalidationResult

private val LAMBDA_FILTER_REGEX = Regex("""(function|lambda)""", RegexOption.IGNORE_CASE)

fun Assert<List<Investigated>>.assertInvestigations(vararg investigates: Investigated) {
  given { actuals ->
    if (actuals.size != investigates.size)
      fail(
        "Expected ${investigates.size} investigations, but found ${actuals.size}",
        expected = investigates,
        actual = actuals,
      )

    repeat(actuals.size) { index ->
      val (actualInformation, actualResult) = actuals[index]
      val (investigateInformation, investigateResult) = investigates[index]

      assertThat(actualInformation.copy(compoundKey = null))
        .isEqualTo(investigateInformation.copy(compoundKey = null))

      if (
        actualResult is InvalidationResult.ArgumentChanged &&
        investigateResult is InvalidationResult.ArgumentChanged
      ) {
        val maskedActualResult = InvalidationResult.ArgumentChanged(
          changed = actualResult.changed.map { changedArgument ->
            if (!changedArgument.previous.type.contains(LAMBDA_FILTER_REGEX)) return@map changedArgument
            changedArgument.copy(
              previous = changedArgument.previous.copy(type = "lambda", valueString = "() -> Any", valueHashCode = 0),
              new = changedArgument.new.copy(type = "lambda", valueString = "() -> Any", valueHashCode = 0),
            )
          },
        )
        val maskedInvestigateResult = InvalidationResult.ArgumentChanged(
          changed = investigateResult.changed.map { changedArgument ->
            if (!changedArgument.previous.type.contains(LAMBDA_FILTER_REGEX)) return@map changedArgument
            changedArgument.copy(
              previous = changedArgument.previous.copy(type = "lambda", valueString = "() -> Any", valueHashCode = 0),
              new = changedArgument.new.copy(type = "lambda", valueString = "() -> Any", valueHashCode = 0),
            )
          },
        )

        assertThat(maskedActualResult).isEqualTo(maskedInvestigateResult)
      } else {
        assertThat(actualResult).isEqualTo(investigateResult)
      }
    }
  }
}
