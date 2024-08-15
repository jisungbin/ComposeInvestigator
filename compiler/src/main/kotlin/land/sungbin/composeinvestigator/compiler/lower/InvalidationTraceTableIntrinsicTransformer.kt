/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.lower

import androidx.compose.compiler.plugins.kotlin.hasComposableAnnotation
import androidx.compose.compiler.plugins.kotlin.irTrace
import androidx.compose.compiler.plugins.kotlin.lower.includeFileNameInExceptionTrace
import java.util.Stack
import land.sungbin.composeinvestigator.compiler.COMPOSABLE_INVALIDATION_TRACE_TABLE_FQN
import land.sungbin.composeinvestigator.compiler.COMPOSABLE_NAME_FQN
import land.sungbin.composeinvestigator.compiler.CURRENT_COMPOSABLE_INVALIDATION_TRACER_FQN
import land.sungbin.composeinvestigator.compiler.ComposableInvalidationTraceTable_CURRENT_COMPOSABLE_KEY_NAME
import land.sungbin.composeinvestigator.compiler.ComposableInvalidationTraceTable_CURRENT_COMPOSABLE_NAME
import land.sungbin.composeinvestigator.compiler.UNKNOWN_STRING
import land.sungbin.composeinvestigator.compiler.analysis.DurationWritableSlices
import land.sungbin.composeinvestigator.compiler.analysis.set
import land.sungbin.composeinvestigator.compiler.fromFqName
import land.sungbin.composeinvestigator.compiler.struct.IrComposableInformation
import land.sungbin.composeinvestigator.compiler.struct.IrInvalidationTraceTableHolder
import land.sungbin.composeinvestigator.compiler.struct.get
import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetObjectValueImpl
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.file
import org.jetbrains.kotlin.ir.util.getPropertyGetter
import org.jetbrains.kotlin.ir.util.getPropertySetter
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.SpecialNames
import org.jetbrains.kotlin.utils.addToStdlib.cast

// ComposeInvestigatorBaseLower will automatically lower this for each file.
public class InvalidationTraceTableIntrinsicTransformer(
  private val context: IrPluginContext,
  private val irComposableInformation: IrComposableInformation,
  private val tables: IrInvalidationTraceTableHolder,
) : IrElementTransformerVoid(), FileLoweringPass {
  private val composableStack = Stack<IrSimpleFunction>()

  private val tableSymbol = context.referenceClass(ClassId.topLevel(COMPOSABLE_INVALIDATION_TRACE_TABLE_FQN))!!
  private val composableNameSymbol = context.referenceClass(ClassId.topLevel(COMPOSABLE_NAME_FQN))!!.owner

  private val currentTableGetterSymbol =
    context.referenceProperties(CallableId.fromFqName(CURRENT_COMPOSABLE_INVALIDATION_TRACER_FQN)).single().owner.getter!!

  private val currentComposableNameGetterSymbol =
    tableSymbol.getPropertyGetter(ComposableInvalidationTraceTable_CURRENT_COMPOSABLE_NAME.asString())!!.owner

  private val currentComposableNameSetterSymbol =
    tableSymbol.getPropertySetter(ComposableInvalidationTraceTable_CURRENT_COMPOSABLE_NAME.asString())!!.owner

  private val currentComposableKeyNameGetterSymbol =
    tableSymbol.getPropertyGetter(ComposableInvalidationTraceTable_CURRENT_COMPOSABLE_KEY_NAME.asString())!!.owner

  override fun lower(irFile: IrFile) {
    includeFileNameInExceptionTrace(irFile) {
      irFile.transformChildrenVoid()
    }
  }

  override fun visitSimpleFunction(declaration: IrSimpleFunction): IrStatement {
    if (declaration.hasComposableAnnotation()) composableStack.push(declaration)

    declaration.body?.transformChildrenVoid()
    return super.visitSimpleFunction(declaration).also {
      if (declaration.hasComposableAnnotation())
        check(composableStack.pop() == declaration) { "composableStack is not balanced." }
    }
  }

  override fun visitCall(expression: IrCall): IrExpression {
    val table = tables[expression.symbol.owner.file]
    return when (expression.symbol.owner.kotlinFqName) {
      currentTableGetterSymbol.kotlinFqName -> table.propGetter(startOffset = expression.startOffset, endOffset = expression.endOffset)
      currentComposableNameGetterSymbol.kotlinFqName -> {
        IrConstructorCallImpl.fromSymbolOwner(
          startOffset = expression.startOffset,
          endOffset = expression.endOffset,
          type = composableNameSymbol.defaultType,
          constructorSymbol = composableNameSymbol.symbol.constructors.single(),
        ).apply {
          putValueArgument(
            0,
            composableStack.peek()
              ?.let { composable ->
                IrComposableInformation.getName(context.irTrace[DurationWritableSlices.DURABLE_FUNCTION_KEY, composable]!!.composable)
              }
              ?: context.irString(SpecialNames.UNKNOWN_STRING),
          )
        }
      }
      currentComposableNameSetterSymbol.kotlinFqName -> {
        composableStack.peek()?.let { composable ->
          val userProvideName = expression
            .getValueArgument(0).cast<IrConstructorCall>()
            .getValueArgument(0).cast<IrConst<String>>().value

          val originalKey = context.irTrace[DurationWritableSlices.DURABLE_FUNCTION_KEY, composable]!!
          val newComposable = irComposableInformation.copyFrom(originalKey.composable, name = context.irString(userProvideName))

          context.irTrace[DurationWritableSlices.DURABLE_FUNCTION_KEY, composable] = originalKey.copy(composable = newComposable)
        }

        IrGetObjectValueImpl(
          startOffset = expression.startOffset,
          endOffset = expression.endOffset,
          type = context.irBuiltIns.unitClass.defaultType,
          symbol = context.irBuiltIns.unitClass.owner.symbol,
        )
      }
      currentComposableKeyNameGetterSymbol.kotlinFqName -> {
        composableStack.peek()?.let { composable ->
          context.irString(
            context.irTrace[DurationWritableSlices.DURABLE_FUNCTION_KEY, composable]!!.keyName,
            startOffset = expression.startOffset,
            endOffset = expression.endOffset,
          )
        } ?: context.irString(SpecialNames.UNKNOWN_STRING, startOffset = expression.startOffset, endOffset = expression.endOffset)
      }
      else -> super.visitCall(expression)
    }
  }
}
