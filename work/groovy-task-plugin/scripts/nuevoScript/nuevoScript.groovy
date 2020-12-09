
println startNode.getEnclosingBlocks().each { nod ->
  println nod.getSearchName()
  println nod.getTypeDisplayName()
  println nod.getTypeFunctionName()
  println nod.isBody()
}