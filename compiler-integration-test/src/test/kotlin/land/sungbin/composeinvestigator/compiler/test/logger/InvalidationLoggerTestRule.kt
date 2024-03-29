/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.test.logger

import land.sungbin.composeinvestigator.compiler.test.source.logger.clearInvalidationLog
import land.sungbin.composeinvestigator.compiler.test.source.logger.clearStateChangeLog
import land.sungbin.composeinvestigator.compiler.test.source.logger.invalidationLogger
import land.sungbin.composeinvestigator.compiler.test.source.logger.invalidationProcessedFileTable
import land.sungbin.composeinvestigator.compiler.test.source.logger.invalidationSkippedFileTable
import land.sungbin.composeinvestigator.compiler.test.source.logger.stateChangeLogger
import land.sungbin.composeinvestigator.runtime.ComposeInvestigatorConfig
import org.junit.rules.TestWatcher
import org.junit.runner.Description

class InvalidationLoggerTestRule : TestWatcher() {
  override fun starting(description: Description?) {
    ComposeInvestigatorConfig.invalidationLogger = invalidationLogger
    ComposeInvestigatorConfig.stateChangedListener = stateChangeLogger
  }

  override fun finished(description: Description?) {
    clearInvalidationLog()
    clearStateChangeLog()
    invalidationProcessedFileTable.resetAffectFields()
    invalidationSkippedFileTable.resetAffectFields()
  }
}
