
try {
  def script = """
  job('example') {
    steps {
      batchFile('echo Hello World!')
    }
  }
  """
  
  jobDsl.setScriptText(script).execute()
}
catch(ex){
	throw ex
}