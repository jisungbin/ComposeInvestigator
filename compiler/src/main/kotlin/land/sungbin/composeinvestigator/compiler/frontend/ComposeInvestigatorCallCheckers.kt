/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.frontend

import land.sungbin.composeinvestigator.compiler.COMPOSABLE_NAME_FQN
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.expression.ExpressionCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirFunctionCallChecker
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.FirLiteralExpression
import org.jetbrains.kotlin.fir.expressions.argument
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar
import org.jetbrains.kotlin.fir.types.classId
import org.jetbrains.kotlin.fir.types.resolvedType
import org.jetbrains.kotlin.types.ConstantValueKind
import org.jetbrains.kotlin.utils.addToStdlib.safeAs

public class ComposeInvestigatorFirExtensionRegistrar : FirExtensionRegistrar() {
  override fun ExtensionRegistrarContext.configurePlugin() {
    +::ComposeInvestigatorCallCheckers
  }
}

private class ComposeInvestigatorCallCheckers(session: FirSession) : FirAdditionalCheckersExtension(session) {
  override val expressionCheckers = object : ExpressionCheckers() {
    override val functionCallCheckers = setOf(ComposableNameCallChecker)
  }
}

private object ComposableNameCallChecker : FirFunctionCallChecker(MppCheckerKind.Common) {
  override fun check(expression: FirFunctionCall, context: CheckerContext, reporter: DiagnosticReporter) {
    if (expression.resolvedType.classId?.asSingleFqName() != COMPOSABLE_NAME_FQN) return
    if (expression.argument.safeAs<FirLiteralExpression>()?.kind != ConstantValueKind.String) {
      reporter.reportOn(
        expression.source,
        ComposeInvestigatorErrors.COMPOSABLE_NAME_ONLY_HARDCODED,
        context,
      )
    }
  }
}
