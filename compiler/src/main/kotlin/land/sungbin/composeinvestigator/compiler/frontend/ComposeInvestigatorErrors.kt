/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.frontend

import land.sungbin.composeinvestigator.compiler.ErrorMessages
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactory0
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.SourceElementPositioningStrategies
import org.jetbrains.kotlin.diagnostics.error0
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.diagnostics.rendering.RootDiagnosticRendererFactory
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtElement

// Make this public so that this can be suppressed by the outside world.
public object ComposeInvestigatorErrors {
  public val UNSUPPORTED_COMPOSABLE_NAME: KtDiagnosticFactory0
    by error0<KtCallExpression>(SourceElementPositioningStrategies.VALUE_ARGUMENTS)

  public val API_ACCESS_IN_NO_INVESTIGATION_FILE: KtDiagnosticFactory0
    by error0<KtElement>(SourceElementPositioningStrategies.WHOLE_ELEMENT)

  public val ILLEGAL_COMPOSABLE_SCOPE_CALL: KtDiagnosticFactory0
    by error0<KtElement>(SourceElementPositioningStrategies.WHOLE_ELEMENT)

  private class DiagnosticRendererFactory : BaseDiagnosticRendererFactory() {
    override val MAP = KtDiagnosticFactoryToRendererMap("ComposeInvestigator").apply {
      put(UNSUPPORTED_COMPOSABLE_NAME, ErrorMessages.COMPOSABLE_NAME_EXPRESSION_ONLY_HARDCODED)
      put(API_ACCESS_IN_NO_INVESTIGATION_FILE, ErrorMessages.TRACE_TABLE_NOT_GENERATED)
      put(ILLEGAL_COMPOSABLE_SCOPE_CALL, ErrorMessages.COMPOSABLE_SCOPED_API_MUST_CALL_WITHIN_COMPOSABLE)
    }
  }

  init {
    RootDiagnosticRendererFactory.registerFactory(DiagnosticRendererFactory())
  }
}
