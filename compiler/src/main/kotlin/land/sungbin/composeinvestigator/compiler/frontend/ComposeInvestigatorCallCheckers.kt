/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.frontend

import androidx.compose.compiler.plugins.kotlin.k2.ComposableFunction
import androidx.compose.compiler.plugins.kotlin.k2.hasComposableAnnotation
import androidx.compose.compiler.plugins.kotlin.lower.fastForEach
import land.sungbin.composeinvestigator.compiler.COMPOSABLE_NAME_FQN
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.DeclarationCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirFunctionChecker
import org.jetbrains.kotlin.fir.analysis.checkers.expression.ExpressionCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirFunctionCallChecker
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension
import org.jetbrains.kotlin.fir.declarations.FirAnonymousFunction
import org.jetbrains.kotlin.fir.declarations.FirFunction
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.FirLiteralExpression
import org.jetbrains.kotlin.fir.expressions.argument
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar
import org.jetbrains.kotlin.fir.types.classId
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.types.functionTypeKind
import org.jetbrains.kotlin.fir.types.resolvedType

public class ComposeInvestigatorFirExtensionRegistrar : FirExtensionRegistrar() {
  override fun ExtensionRegistrarContext.configurePlugin() {
    +::ComposeInvestigatorCallCheckers
  }
}

private class ComposeInvestigatorCallCheckers(session: FirSession) : FirAdditionalCheckersExtension(session) {
  override val declarationCheckers = object : DeclarationCheckers() {
    override val functionCheckers = setOf(ComposableNameUsageChecker)
  }

  override val expressionCheckers = object : ExpressionCheckers() {
    override val functionCallCheckers = setOf(ComposableNameExpressionChecker)
  }
}

private object ComposableNameUsageChecker : FirFunctionChecker(MppCheckerKind.Common) {
  override fun check(declaration: FirFunction, context: CheckerContext, reporter: DiagnosticReporter) {
    when {
      declaration is FirAnonymousFunction -> if (declaration.typeRef.coneType.functionTypeKind(context.session) === ComposableFunction) return
      else -> if (declaration.hasComposableAnnotation(context.session)) return
    }

    declaration.body?.statements?.fastForEach { statement ->
      if (statement !is FirFunctionCall) return
      if (statement.resolvedType.classId?.asSingleFqName() != COMPOSABLE_NAME_FQN) return

      reporter.reportOn(statement.source, ComposeInvestigatorErrors.ILLEGAL_COMPOSABLE_NAME, context)
    }
  }
}

private object ComposableNameExpressionChecker : FirFunctionCallChecker(MppCheckerKind.Common) {
  override fun check(expression: FirFunctionCall, context: CheckerContext, reporter: DiagnosticReporter) {
    if (expression.resolvedType.classId?.asSingleFqName() != COMPOSABLE_NAME_FQN) return
    if (expression.argument !is FirLiteralExpression) {
      reporter.reportOn(expression.source, ComposeInvestigatorErrors.UNSUPPORTED_COMPOSABLE_NAME, context)
    }
  }
}
