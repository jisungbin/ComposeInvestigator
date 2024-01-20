/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.test.table

import land.sungbin.composeinvestigator.compiler.test.source.table.callback.clearInvalidationListensViaEffectsLog
import land.sungbin.composeinvestigator.compiler.test.source.table.callback.clearInvalidationListensViaManualRegisterLog
import org.junit.rules.TestWatcher
import org.junit.runner.Description

class InvalidationCallbackTestRule : TestWatcher() {
  override fun finished(description: Description?) {
    clearInvalidationListensViaManualRegisterLog()
    clearInvalidationListensViaEffectsLog()
  }
}
