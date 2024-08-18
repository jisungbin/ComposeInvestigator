/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.lower

import androidx.compose.compiler.plugins.kotlin.irTrace
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
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetObjectValueImpl
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.getPropertyGetter
import org.jetbrains.kotlin.ir.util.getPropertySetter
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.SpecialNames
import org.jetbrains.kotlin.utils.addToStdlib.cast

public class InvalidationTraceTableIntrinsicTransformer(
  private val context: IrPluginContext,
  private val irComposableInformation: IrComposableInformation,
  private val tables: IrInvalidationTraceTableHolder,
) : IrElementTransformerVoidWithContext() {
  private val tableSymbol = context.referenceClass(ClassId.topLevel(COMPOSABLE_INVALIDATION_TRACE_TABLE_FQN))!!
  private val composableNameSymbol = context.referenceClass(ClassId.topLevel(COMPOSABLE_NAME_FQN))!!.owner

  private val currentTableGetterSymbol =
    context.referenceProperties(CallableId.fromFqName(CURRENT_COMPOSABLE_INVALIDATION_TRACER_FQN)).first().owner.getter!!

  private val currentComposableNameGetterSymbol =
    tableSymbol.getPropertyGetter(ComposableInvalidationTraceTable_CURRENT_COMPOSABLE_NAME.asString())!!.owner

  private val currentComposableNameSetterSymbol =
    tableSymbol.getPropertySetter(ComposableInvalidationTraceTable_CURRENT_COMPOSABLE_NAME.asString())!!.owner

  private val currentComposableKeyNameGetterSymbol =
    tableSymbol.getPropertyGetter(ComposableInvalidationTraceTable_CURRENT_COMPOSABLE_KEY_NAME.asString())!!.owner

  override fun visitCall(expression: IrCall): IrExpression {
    val table = tables[currentFile]
    return when (expression.symbol.owner.kotlinFqName) {
      currentTableGetterSymbol.kotlinFqName -> {
        table.propGetter(
          startOffset = expression.startOffset,
          endOffset = expression.endOffset,
        )
      }
      currentComposableNameGetterSymbol.kotlinFqName -> {
        IrConstructorCallImpl.fromSymbolOwner(
          startOffset = expression.startOffset,
          endOffset = expression.endOffset,
          type = composableNameSymbol.defaultType,
          constructorSymbol = composableNameSymbol.symbol.constructors.first(),
        ).apply {
          putValueArgument(
            0,
            allScopes.lastComposable()
              ?.let { composable ->
                IrComposableInformation.getName(
                  context
                    .irTrace[DurationWritableSlices.DURABLE_FUNCTION_KEY, composable]!!
                    .composable,
                )
              }
              ?: context.irString(SpecialNames.UNKNOWN_STRING),
          )
        }
      }
      currentComposableNameSetterSymbol.kotlinFqName -> {
        allScopes.lastComposable()?.let { composable ->
          val newName = expression
            .getValueArgument(0).cast<IrConstructorCall>()
            .getValueArgument(0).cast<IrConst<String>>().value

          val originalKey = context.irTrace[DurationWritableSlices.DURABLE_FUNCTION_KEY, composable]!!
          val newComposable = irComposableInformation.copyFrom(originalKey.composable, name = context.irString(newName))

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
        allScopes.lastComposable()?.let { composable ->
          context.irString(
            context.irTrace[DurationWritableSlices.DURABLE_FUNCTION_KEY, composable]!!.keyName,
            startOffset = expression.startOffset,
            endOffset = expression.endOffset,
          )
        } ?: run {
          context.irString(
            SpecialNames.UNKNOWN_STRING,
            startOffset = expression.startOffset,
            endOffset = expression.endOffset,
          )
        }
      }
      else -> super.visitCall(expression)
    }
  }
}
