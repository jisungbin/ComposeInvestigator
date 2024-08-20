/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.frontend

import androidx.compose.compiler.plugins.kotlin.k2.ComposableFunction
import androidx.compose.compiler.plugins.kotlin.k2.hasComposableAnnotation
import land.sungbin.composeinvestigator.compiler.NO_INVESTIGATION_FQN
import land.sungbin.composeinvestigator.compiler.lower.unsafeLazy
import org.jetbrains.kotlin.descriptors.annotations.AnnotationUseSiteTarget
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.DeclarationCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirFileChecker
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension
import org.jetbrains.kotlin.fir.declarations.FirFile
import org.jetbrains.kotlin.fir.declarations.FirFunction
import org.jetbrains.kotlin.fir.declarations.validate
import org.jetbrains.kotlin.fir.expressions.builder.buildAnnotation
import org.jetbrains.kotlin.fir.expressions.impl.FirEmptyAnnotationArgumentMapping
import org.jetbrains.kotlin.fir.smartPlus
import org.jetbrains.kotlin.fir.types.builder.buildResolvedTypeRef
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.types.constructClassLikeType
import org.jetbrains.kotlin.fir.types.functionTypeKind
import org.jetbrains.kotlin.name.ClassId

public class InvalidationTraceTableInstantiationValidator(session: FirSession) : FirAdditionalCheckersExtension(session) {
  override val declarationCheckers: DeclarationCheckers = object : DeclarationCheckers() {
    override val fileCheckers: Set<FirFileChecker> = setOf(NoComposableFileChecker)
  }
}

private object NoComposableFileChecker : FirFileChecker(MppCheckerKind.Common) {
  private val noInvestigationType by unsafeLazy {
    buildResolvedTypeRef {
      type = ClassId.topLevel(NO_INVESTIGATION_FQN).constructClassLikeType()
    }
  }

  override fun check(declaration: FirFile, context: CheckerContext, reporter: DiagnosticReporter) {
    if (
      declaration.declarations.none { element ->
        element.hasComposableAnnotation(context.session) ||
          (
            element is FirFunction &&
              element.valueParameters.any { parameter ->
                parameter.returnTypeRef.coneType.functionTypeKind(context.session) === ComposableFunction
              }
            )
      }
    ) {
      val noInvestigationAnnotation = buildAnnotation {
        useSiteTarget = AnnotationUseSiteTarget.FILE
        annotationTypeRef = noInvestigationType
        argumentMapping = FirEmptyAnnotationArgumentMapping
      }

      declaration.replaceAnnotations(listOf(noInvestigationAnnotation).smartPlus(declaration.annotations))

      // Validate that the new annotations do not break the file.
      declaration.validate()
    }
  }
}
