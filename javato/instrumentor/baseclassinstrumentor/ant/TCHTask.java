package javato.instrumentor.baseclassinstrumentor.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;

import java.io.File;
import java.util.ArrayList;

public class TCHTask extends Task
{
    private String observer;
    private File outputFile;
    private Path classpath;
    private String jvmargs;
    private boolean tchOn;

    public TCHTask()
    {
        // TODO Auto-generated constructor stub
        jvmargs = "";
        tchOn = true;
    }

    public void setObserver(String theClass)
    {
        observer = theClass;
    }

    public void setDestFile(File f)
    {
        outputFile = f;
    }

    public void setClasspath(Path classpath)
    {
        this.classpath = classpath;
    }

    public void setClasspathRef(Reference pathRef)
    {
        this.classpath = (Path) pathRef.getReferencedObject();
    }

    public void setJvmArgs(String args)
    {
        jvmargs = args;
    }

    public void setTchOn(boolean val)
    {
        tchOn = val;
    }

    public void execute() throws BuildException
    {
        if (classpath == null)
            classpath = new Path(this.getProject(), ".");
        System.out.println("Observer: " + observer);
        System.out.println("TCH is " + (tchOn ? "" : "not ") + "enabled");
        System.out.println("Output file: " + outputFile.getPath());
        Java j = new Java(this);
        j.createJvmarg().setLine("-Xmx1400m " + jvmargs);
        j.setClasspath(classpath);
        j.setClassname("javato.instrumentor.baseclassinstrumentor.TCHInstrumentor");
        j.setFailonerror(true);
        j.setFork(true);
        String args = "-obs " + observer + " ";
        args += "-o \"" + outputFile.getAbsolutePath() + "\" ";
        args += "-tch-on " + tchOn;
        j.createArg().setLine(args);
        j.init();
        j.execute();
    }
}
