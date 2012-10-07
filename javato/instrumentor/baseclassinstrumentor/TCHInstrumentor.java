package javato.instrumentor.baseclassinstrumentor;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;
import java.util.ArrayList;
import javato.instrumentor.Visitor;
import javato.observer.Observer;

import soot.Scene;

public class TCHInstrumentor
{
    private static Collection<String> observerClassNames = new ArrayList<String>();
    private static String outputFileName = "tch-output.jar";
    private static String prefix = TCHTransformer.defaultPrefix;
    private static boolean shouldTCH = true;

    public static void main(String[] args)
    {
        handleOptions(args);
        if (observerClassNames.size() == 0)
            observerClassNames.add("javato.instrumentor.baseclassinstrumentor.TestObserver");
        Scene.v().setSootClassPath(
                System.getProperty("sun.boot.class.path") + File.pathSeparator
                        + System.getProperty("java.class.path"));
        TCHTransformer tcht = new TCHTransformer();
        for (String str : observerClassNames)
            tcht.addEntryPoint(str);
        tcht.process(outputFileName, shouldTCH);
    }

    private static void handleOption(String flag, String value) throws Exception
    {
        // Observer
        if (flag.equals("-obs"))
        {
            observerClassNames.add(value);
            try
            {
                Class.forName(value).asSubclass(Observer.class);
            }
            catch (Exception e)
            {
                System.err
                        .println("Cannot find observer class or observer class does not extend javato.observer.Observer: "
                                + value);
                e.printStackTrace(System.err);
                throw e;
            }
        }
        else if (flag.equals("-o"))
        {
            outputFileName = value;
            if (!outputFileName.toLowerCase().endsWith(".jar"))
            {
                System.err.println("Output file name must end with \".jar\"");
                throw new Exception();
            }
        }
        else if (flag.equals("-prefix"))
        {
            prefix = value;
            if (!prefix.endsWith("."))
                prefix += ".";
        }
        else if (flag.equals("-tch-on"))
        {
            shouldTCH = value.equals("true");
        }
        else if (flag.equals("-ex"))
        {
            FileInputStream fis;
            Exclusions.loadExclusions(fis = new FileInputStream(value));
            fis.close();
        }
        else
        {
            throw new Exception("Unknown flag: " + flag);
        }
    }

    private static void handleOptions(String[] args)
    {
        try
        {
            for (int x = 0; x < args.length; x += 2)
            {
                if (args[x].startsWith("-"))
                {
                    handleOption(args[x], args[x + 1]);
                }
            }
        }
        catch (Exception e)
        {
            System.err.println("Error processing arguments");
            System.exit(1);
        }
    }
}
