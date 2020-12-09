import com.michelin.cio.hudson.plugins.rolestrategy.RoleBasedAuthorizationStrategy
 
def role = "role_jp"
def project_base_name = "example/test01/v.10"
 
def authStrategy = jenkins.getAuthorizationStrategy()//RoleBasedAuthorizationStrategy.instance
if( authStrategy != null ){
  def users = authStrategy.getGrantedRoles(RoleBasedAuthorizationStrategy.PROJECT).entrySet()
                .findAll { entry -> entry.key.getPattern().matcher(project_base_name).matches() }
                .max { r1-> r1.key.pattern.toString().size() }
                .getValue()
  
  println users
}
else{
  println "Role Base Strategy Plugin is not in use"
}
 