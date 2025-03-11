// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler.lower

import land.sungbin.composeinvestigator.compiler.COMPOSABLE_INVALIDATION_TRACE_TABLE_FQN
import land.sungbin.composeinvestigator.compiler.NO_INVESTIGATION_FQN
import land.sungbin.composeinvestigator.compiler.log
import land.sungbin.composeinvestigator.compiler.struct.IrComposeInvestigator
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.jvm.ir.getKtFile
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.codegen.CompilationException
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.name
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.util.findDeclaration
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.setDeclarationsParent
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid

/**
 * Generates code to instantiate `ComposableInvalidationTraceTable` as the top-level
 * variable of the current file, if the file is not annotated with `@file:NoInvestigation`.
 *
 * ### Original
 *
 * ```
 * @Composable fun DisplayPlusResult(a: Int, b: Int) {
 *   Text((a + b).toString())
 * }
 * ```
 *
 * ### Transformed
 *
 * ```
 * val ComposableInvalidationTraceTableImpl%DisplayPlusResultKt = ComposableInvalidationTraceTable()
 *
 * @Composable fun DisplayPlusResult(a: Int, b: Int) {
 *   Text((a + b).toString())
 * }
 * ```
 */
public class ComposeInvestigatorInstantiateTransformer(
  private val context: IrPluginContext,
  private val messageCollector: MessageCollector, // TODO context.createDiagnosticReporter() (Blocked: "This API is not supported for K2")
) : IrElementTransformerVoid() {
  private val tables = mutableMapOf<IrFile, IrComposeInvestigator>()

  override fun getByFile(file: IrFile): IrComposeInvestigator =
    tables[file] ?: throw CompilationException(
      "No table for ${file.name}",
      /* cause = */ null,
      /* element = */ file.getKtFile(),
    )

  override fun visitFile(declaration: IrFile): IrFile =
    includeFilePathInExceptionTrace(declaration) {
      if (
        declaration.hasAnnotation(NO_INVESTIGATION_FQN)
      // FIXME `fun c(l: @Composable () -> Unit)` ==> NO TABLE GENERATED
      // declaration.declarations
      //   .filter { element -> element.hasComposableAnnotation() }
      //   .all { element -> element.hasAnnotation(NO_INVESTIGATION_FQN) }
      )
        return declaration

      val existsTable = declaration.findDeclaration<IrProperty> { property ->
        property.backingField?.type?.classFqName == COMPOSABLE_INVALIDATION_TRACE_TABLE_FQN
      }
      val table = existsTable?.let(IrComposeInvestigator::from) ?: run {
        IrComposeInvestigator.create(context, declaration).also { table ->
          declaration.declarations.add(0, table.rawProp.also { prop -> prop.setDeclarationsParent(declaration) })
        }
      }

      tables[declaration] = table
      messageCollector.log("Instantiated InvalidationTraceTable for ${declaration.name}")

      super.visitFile(declaration)
    }
}
