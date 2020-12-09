package com.tsoft.plugins.scheduler.utils

import static com.cloudbees.plugins.credentials.CredentialsMatchers.firstOrNull
import static com.cloudbees.plugins.credentials.CredentialsMatchers.withId
import static com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials
import hudson.EnvVars
import hudson.FilePath
import hudson.security.ACL
import jenkins.model.Jenkins
import org.eclipse.jgit.api.AddCommand
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.PushCommand
import org.eclipse.jgit.api.errors.CanceledException
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException
import org.eclipse.jgit.api.errors.DetachedHeadException
import org.eclipse.jgit.api.errors.GitAPIException
import org.eclipse.jgit.api.errors.InvalidConfigurationException
import org.eclipse.jgit.api.errors.InvalidRemoteException
import org.eclipse.jgit.api.errors.JGitInternalException
import org.eclipse.jgit.api.errors.NoFilepatternException
import org.eclipse.jgit.api.errors.NoHeadException
import org.eclipse.jgit.api.errors.NoMessageException
import org.eclipse.jgit.api.errors.RefNotFoundException
import org.eclipse.jgit.api.errors.WrongRepositoryStateException
import org.eclipse.jgit.transport.CredentialsProvider
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.transport.PushResult
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider

class GitClient {

    private HashMap<String, Object> binding
    private FilePath workspace
    private EnvVars environment
    private Git git
    private String url
    private Repository localRepository
    private CredentialsProvider cp
    private StandardUsernamePasswordCredentials creds = null
    private String name = ""
    private String password = ""

    GitClient(HashMap<String, Object> binding) {
        this.binding = binding
        this.workspace = (FilePath)binding.get("workspace")
        this.environment = Jenkins.getInstanceOrNull().getComputer('(master)').getEnvironment()
    }

    GitClient setUrl(String url){
        this.url = url
        return this
    }

    GitClient setDir(String dir){
        this.workspace = new FilePath(workspace, dir)
        // se debe crear la carpeta al interior del directorio temporal
        if( !workspace.exists() ){
            workspace.mkdirs()
        }
        this.localRepository = new FileRepository(workspace.getRemote() + "/.git")
        this.git = new Git(localRepository)
        return this
    }

    GitClient setUserAndPassword(String user, String pass){
        this.cp = new UsernamePasswordCredentialsProvider(user, pass)
        return this
    }

    GitClient setCredentials(String credentialsId){
        creds = firstOrNull(lookupCredentials(
                StandardUsernamePasswordCredentials.class,
                Jenkins.getInstanceOrNull(),
                ACL.SYSTEM,
                Collections.emptyList()),
                withId(credentialsId))
        if( creds!=null )
            setUserAndPassword(creds.username, creds.password.plainText)
        return this
    }

    def _clone() throws IOException, NoFilepatternException, GitAPIException {
        Git.cloneRepository().setURI(url).setDirectory(new File(workspace.getRemote())).call()
    }

    def _add(String filepattern) throws IOException, NoFilepatternException, GitAPIException {
        GitClient.checkInfo(this)
        AddCommand add = git.add()
        def pattern  = filepattern==null? "." : filepattern
        add.addFilepattern(pattern).call()
    }

    def _commit(String message) throws IOException, NoHeadException,
            NoMessageException, ConcurrentRefUpdateException,
            JGitInternalException, WrongRepositoryStateException, GitAPIException {

        GitClient.checkInfo(this)
        git.commit().setMessage(message).call()
    }

    def _push() throws IOException, JGitInternalException,
            InvalidRemoteException, GitAPIException {
        PushCommand pc = git.push()
        pc.setCredentialsProvider(cp)
                .setForce(true)
                .setPushAll()
        try {
            GitClient.checkInfo(this)
            Iterator<PushResult> it = pc.call().iterator()
            if (it.hasNext()) {
                println(it.next().toString())
            }
        } catch (InvalidRemoteException e) {
            e.printStackTrace()
        }
    }

    def _pull() throws IOException, WrongRepositoryStateException,
            InvalidConfigurationException, DetachedHeadException,
            InvalidRemoteException, CanceledException, RefNotFoundException,
            NoHeadException, GitAPIException {

        GitClient.checkInfo(this)
        git.pull().call()
    }


    FilePath getWorkspace(){ return this.workspace }

    String getUrl() { return this.url }

    Repository getRepository() { return this.repository }
    
    static void checkInfo(GitClient client){
        assert client.url != null
        assert client.localRepository != null
        assert client.workspace != null
    }

}
