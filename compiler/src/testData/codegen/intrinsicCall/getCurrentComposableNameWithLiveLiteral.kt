// INVESTIGATOR_FEATURES: IntrinsicCall
// COMPOSE_FEATURES: LiveLiterals

@Composable fun MyComposable() {
  ComposeInvestigator.getCurrentComposableName()
  ComposeInvestigator.getCurrentComposableName(default = "MyAwesomeThing")
}
