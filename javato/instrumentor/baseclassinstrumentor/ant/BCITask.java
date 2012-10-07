package javato.instrumentor.baseclassinstrumentor.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;

import java.io.File;
import java.util.ArrayList;

public class BCITask extends Task
{
    private String observer;
    private File outputFile;
    private ArrayList<VisitorString> visitors;
    private Path classpath;
    private File exclusionsFile;
    private String jvmargs;

    public BCITask()
    {
        // TODO Auto-generated constructor stub
        visitors = new ArrayList<VisitorString>();
        jvmargs = "";
    }

    public void setObserver(String theClass)
    {
        observer = theClass;
    }

    public void setDestFile(File f)
    {
        outputFile = f;
    }

    public void addVisitor(VisitorString vis)
    {
        visitors.add(vis);
    }

    public void setClasspath(Path classpath)
    {
        this.classpath = classpath;
    }

    public void setClasspathRef(Reference pathRef)
    {
        this.classpath = (Path) pathRef.getReferencedObject();
    }

    public void setExclusions(File f)
    {
        this.exclusionsFile = f;
    }

    public void setJvmArgs(String args)
    {
        jvmargs = args;
    }

    public void execute() throws BuildException
    {
        if (classpath == null)
            classpath = new Path(this.getProject(), ".");
        System.out.println("Observer: " + observer);
        System.out.println("Visitors:");
        for (VisitorString c : visitors)
            System.out.println("\t" + c.getVisitorName());
        System.out.println("Output file: " + outputFile.getPath());
        Java j = new Java(this);
        j.createJvmarg().setLine("-javaagent:BCIAgent.jar=instrumenting -Xmx1400m " + jvmargs);
        j.setClasspath(classpath);
        j.setClassname("javato.instrumentor.baseclassinstrumentor.BaseClassInstrumentor");
        j.setFailonerror(true);
        j.setFork(true);
        String args = "-obs " + observer + " ";
        for (VisitorString vs : visitors)
            args += "-vis " + vs.getVisitorName() + " ";
        args += "-o \"" + outputFile.getAbsolutePath() + "\" ";
        if (exclusionsFile != null)
            args += "-ex " + "\"" + exclusionsFile.getAbsolutePath() + "\"";
        j.createArg().setLine(args);
        j.init();
        j.execute();
    }
}
