def file = new File(workspace.getRemote(), "github.zip") 

RESTClient.setUrl('https://github.com/d3camp0s/redis-notifier-plugin/archive/master.zip')
		  .downloadTo("${workspace.getRemote()}/github.zip")

def cont = 0
while( cont<100 ){
	print cont++
    println "*"*cont
    Thread.sleep(100)
}