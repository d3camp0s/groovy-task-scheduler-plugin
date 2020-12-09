
try {
  binding.getVariables().each { k, v ->
      println k+":"+v
  }
  
  exec.runTask("dowloadfile")
  
  binding.getVariables().each { k, v ->
      println k+":"+v
  }
  
}
catch(ex) {
	throw ex
}