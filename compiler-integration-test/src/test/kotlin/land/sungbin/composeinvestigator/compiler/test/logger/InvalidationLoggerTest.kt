/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.test.logger

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isDataClassEqualTo
import land.sungbin.composeinvestigator.compiler.test.source.logger.InvalidationProcessedRoot_StateDelegateReference
import land.sungbin.composeinvestigator.compiler.test.source.logger.InvalidationProcessedRoot_StateDirectReference
import land.sungbin.composeinvestigator.compiler.test.source.logger.InvalidationSkippedRoot
import land.sungbin.composeinvestigator.compiler.test.source.logger.InvalidationSkippedRoot_CustomName
import land.sungbin.composeinvestigator.compiler.test.source.logger.StateNameValue
import land.sungbin.composeinvestigator.compiler.test.source.logger.findInvalidationLog
import land.sungbin.composeinvestigator.compiler.test.source.logger.findStateChangeLog
import land.sungbin.composeinvestigator.compiler.test.source.logger.invalidationLog
import land.sungbin.composeinvestigator.runtime.ChangedFieldPair
import land.sungbin.composeinvestigator.runtime.ComposableInvalidationType
import land.sungbin.composeinvestigator.runtime.DeclarationStability
import land.sungbin.composeinvestigator.runtime.InvalidationReason
import land.sungbin.composeinvestigator.runtime.affect.AffectedComposable
import land.sungbin.composeinvestigator.runtime.affect.AffectedField
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InvalidationLoggerTest {
  @get:Rule
  val compose = createAndroidComposeRule<ComponentActivity>()

  @get:Rule
  val loggerTestRule = InvalidationLoggerTestRule()

  @Test fun affected_composable() {
    compose.setContent { InvalidationSkippedRoot() }

    compose.runOnIdle {
      val affectedRoot = invalidationLog.keys.first { affected -> affected.name == "InvalidationSkippedRoot" }
      val affectedChild = invalidationLog.keys.first { affected -> affected.name == "InvalidationSkippedChild" }

      assertThat(affectedRoot).isDataClassEqualTo(
        AffectedComposable(
          name = "InvalidationSkippedRoot",
          pkg = "land.sungbin.composeinvestigator.compiler.test.source.logger",
          filePath = affectedRoot.filePath, // This value is machine dependent, so we don't test it.
          startLine = 20,
          startColumn = 0,
        ),
      )
      assertThat(affectedChild).isDataClassEqualTo(
        AffectedComposable(
          name = "InvalidationSkippedChild",
          pkg = "land.sungbin.composeinvestigator.compiler.test.source.logger",
          filePath = affectedChild.filePath, // This value is machine dependent, so we don't test it.
          startLine = 28,
          startColumn = 8, // The InvalidationSkippedChild function has a 'private' modifier.
        ),
      )
    }
  }

  @Test fun invalidation_skipped() {
    compose.setContent { InvalidationSkippedRoot() }
    compose.onNode(hasClickAction()).performClick()

    compose.runOnIdle {
      val rootLogs = findInvalidationLog("InvalidationSkippedRoot")
      val childLogs = findInvalidationLog("InvalidationSkippedChild")

      assertThat(rootLogs).containsExactly(
        listOf(
          ComposableInvalidationType.Processed(InvalidationReason.Initial),
          ComposableInvalidationType.Processed(InvalidationReason.Invalidate),
          ComposableInvalidationType.Processed(InvalidationReason.Unknown(params = emptyList())),
        ),
      )
      assertThat(childLogs).containsExactly(
        listOf(
          ComposableInvalidationType.Processed(InvalidationReason.Initial),
          ComposableInvalidationType.Skipped,
        ),
      )
    }
  }

  @Test fun invalidation_skipped_custom_name() {
    compose.setContent { InvalidationSkippedRoot_CustomName() }
    compose.onNode(hasClickAction()).performClick()

    compose.runOnIdle {
      val rootLogs = findInvalidationLog("InvalidationSkippedRoot_custom_name")
      val childLogs = findInvalidationLog("InvalidationSkippedChild_custom_name")

      assertThat(rootLogs).containsExactly(
        listOf(
          ComposableInvalidationType.Processed(InvalidationReason.Initial),
          ComposableInvalidationType.Processed(InvalidationReason.Invalidate),
          ComposableInvalidationType.Processed(InvalidationReason.Unknown(params = emptyList())),
        ),
      )
      assertThat(childLogs).containsExactly(
        listOf(
          ComposableInvalidationType.Processed(InvalidationReason.Initial),
          ComposableInvalidationType.Skipped,
        ),
      )
    }
  }

  @Test fun invalidation_processed_state_delegate() {
    compose.setContent { InvalidationProcessedRoot_StateDelegateReference() }
    repeat(2) { compose.onNode(hasClickAction()).performClick() }

    compose.runOnIdle {
      val rootLogs = findInvalidationLog("InvalidationProcessedRoot_StateDelegateReference")
      val childLogs = findInvalidationLog("InvalidationProcessedChild_StateDelegateReference")
      val stateLogs = findStateChangeLog("InvalidationProcessedRoot_StateDelegateReference")

      assertThat(rootLogs).containsExactly(
        listOf(
          ComposableInvalidationType.Processed(InvalidationReason.Initial),
          ComposableInvalidationType.Processed(InvalidationReason.Invalidate),
          ComposableInvalidationType.Processed(InvalidationReason.Unknown(params = emptyList())),
          ComposableInvalidationType.Processed(InvalidationReason.Invalidate),
          ComposableInvalidationType.Processed(InvalidationReason.Unknown(params = emptyList())),
        ),
      )

      assertThat(childLogs).containsExactly(
        listOf(
          ComposableInvalidationType.Processed(InvalidationReason.Initial),
          ComposableInvalidationType.Processed(
            InvalidationReason.FieldChanged(
              changed = listOf(
                ChangedFieldPair(
                  old = AffectedField.ValueParameter(
                    name = "delegateCount",
                    typeFqName = "kotlin.Int",
                    valueString = "0",
                    valueHashCode = 0,
                    stability = DeclarationStability.Stable,
                  ),
                  new = AffectedField.ValueParameter(
                    name = "delegateCount",
                    typeFqName = "kotlin.Int",
                    valueString = "1",
                    valueHashCode = 1,
                    stability = DeclarationStability.Stable,
                  ),
                ),
              ),
            ),
          ),
          ComposableInvalidationType.Processed(
            InvalidationReason.FieldChanged(
              changed = listOf(
                ChangedFieldPair(
                  old = AffectedField.ValueParameter(
                    name = "delegateCount",
                    typeFqName = "kotlin.Int",
                    valueString = "1",
                    valueHashCode = 1,
                    stability = DeclarationStability.Stable,
                  ),
                  new = AffectedField.ValueParameter(
                    name = "delegateCount",
                    typeFqName = "kotlin.Int",
                    valueString = "2",
                    valueHashCode = 2,
                    stability = DeclarationStability.Stable,
                  ),
                ),
              ),
            ),
          ),
        ),
      )

      assertThat(stateLogs).containsExactly(
        listOf(
          StateNameValue(name = "delegateState", previousValue = 0, newValue = 1),
          StateNameValue(name = "delegateState", previousValue = 1, newValue = 2),
        ),
      )
    }
  }

  @Test fun invalidation_processed_state_direct() {
    compose.setContent { InvalidationProcessedRoot_StateDirectReference() }
    repeat(2) { compose.onNode(hasClickAction()).performClick() }

    compose.runOnIdle {
      val rootLogs = findInvalidationLog("InvalidationProcessedRoot_StateDirectReference")
      val childLogs = findInvalidationLog("InvalidationProcessedChild_StateDirectReference")
      val stateLogs = findStateChangeLog("InvalidationProcessedRoot_StateDirectReference")

      assertThat(rootLogs).containsExactly(
        listOf(
          ComposableInvalidationType.Processed(InvalidationReason.Initial),
          ComposableInvalidationType.Processed(InvalidationReason.Invalidate),
          ComposableInvalidationType.Processed(InvalidationReason.Unknown(params = emptyList())),
          ComposableInvalidationType.Processed(InvalidationReason.Invalidate),
          ComposableInvalidationType.Processed(InvalidationReason.Unknown(params = emptyList())),
        ),
      )

      assertThat(childLogs).containsExactly(
        listOf(
          ComposableInvalidationType.Processed(InvalidationReason.Initial),
          ComposableInvalidationType.Processed(
            InvalidationReason.FieldChanged(
              changed = listOf(
                ChangedFieldPair(
                  old = AffectedField.ValueParameter(
                    name = "directCount",
                    typeFqName = "kotlin.Int",
                    valueString = "0",
                    valueHashCode = 0,
                    stability = DeclarationStability.Stable,
                  ),
                  new = AffectedField.ValueParameter(
                    name = "directCount",
                    typeFqName = "kotlin.Int",
                    valueString = "1",
                    valueHashCode = 1,
                    stability = DeclarationStability.Stable,
                  ),
                ),
              ),
            ),
          ),
          ComposableInvalidationType.Processed(
            InvalidationReason.FieldChanged(
              changed = listOf(
                ChangedFieldPair(
                  old = AffectedField.ValueParameter(
                    name = "directCount",
                    typeFqName = "kotlin.Int",
                    valueString = "1",
                    valueHashCode = 1,
                    stability = DeclarationStability.Stable,
                  ),
                  new = AffectedField.ValueParameter(
                    name = "directCount",
                    typeFqName = "kotlin.Int",
                    valueString = "2",
                    valueHashCode = 2,
                    stability = DeclarationStability.Stable,
                  ),
                ),
              ),
            ),
          ),
        ),
      )

      assertThat(stateLogs).containsExactly(
        listOf(
          StateNameValue(name = "directState", previousValue = 0, newValue = 1),
          StateNameValue(name = "directState", previousValue = 1, newValue = 2),
        ),
      )
    }
  }
}
