// INVESTIGATOR_FEATURES: IntrinsicCall

@Composable fun MyComposable() {
  val lambda = @Composable {
    ComposeInvestigator.getCurrentComposableName()
  }

  Test(
    valueArgument = {
      ComposeInvestigator.getCurrentComposableName()
    },
  )
}

@Composable private fun Test(
  valueArgument: @Composable () -> Unit,
) = Unit
