package javato.instrumentor.baseclassinstrumentor.ant;

import java.io.File;

import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Commandline.Argument;

public class BCIJava extends Java
{
    private Path tchArg;
    private Argument bciArg;
    private Argument debugArg;
    private Path otherLibs;

    public BCIJava()
    {
        debugArg = super.createJvmarg();
        bciArg = super.createJvmarg();
        super.setFork(true);
    }

    public void setTchLib(File f)
    {
        super.createBootclasspath().setPath(f.getPath());
    }

    public void setBciLib(File f)
    {
        bciArg.setValue("-javaagent:BCIAgent.jar=" + f.getPath());
    }

    public void setObserverEnabled(boolean enabled)
    {
        super.createJvmarg().setLine("-Dbci.observerOn=" + (enabled ? "true" : "false"));
    }
    
    public void setDebuggerPort(int port)
    {
        debugArg.setLine("-agentlib:jdwp=transport=dt_socket,server=y,address=" + port + ",suspend=y");
    }

    public void setObserverClasspath(Path p)
    {
        super.createBootclasspath().add(p);
    }

    public void setFork(boolean fork)
    {
        return;
    }
}
