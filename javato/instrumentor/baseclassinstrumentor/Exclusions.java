package javato.instrumentor.baseclassinstrumentor;

import java.util.*;
import java.util.regex.*;
import java.io.*;

public class Exclusions
{
    private static Pattern excludedClassesPat;
    private static Set<String> excludedVisitorMethods;
    private static Set<String> attributeChangesExcluded;
    private static Set<String> excludedTCHClasses;
    private static Set<String> includedClasses;
    private static Set<String> excludedClasses;
    private static Set<String> includedPackages;
    private static Set<String> excludedPackages;
    static
    {
        excludedClassesPat = Pattern.compile(Pattern.quote("java.lang.Class") + "|"
                + Pattern.quote("java.lang.ClassLoader") + "|" + "java\\.lang\\.Thread.*"
                + "|" + "java\\.lang\\.ThreadLocal.*" + "|" + "java\\.lang\\.ref\\..*");

        excludedVisitorMethods = new HashSet<String>();

        excludedVisitorMethods.add("<java.lang.Object: void <init>()>");

        excludedTCHClasses = new HashSet<String>();

        excludedTCHClasses.add("javato.instrumentor.baseclassinstrumentor.Tracer");
        excludedTCHClasses.add("javato.instrumentor.baseclassinstrumentor.ThreadLocal");
        excludedTCHClasses.add("javato.instrumentor.baseclassinstrumentor.CalledByInstrumentation");
        excludedTCHClasses.add("javato.instrumentor.baseclassinstrumentor.Tracer$Counter");

        attributeChangesExcluded = BCIAgent.getInitialVMClasses();

        includedClasses = new HashSet<String>();
        excludedClasses = new HashSet<String>();
        includedPackages = new HashSet<String>();
        excludedPackages = new HashSet<String>();

        includedPackages.add("");
    }

    public static boolean isExcludedTCHClass(String className)
    {
        return excludedTCHClasses.contains(className);
    }

    public static boolean isExcludedVisitorMethod(String method)
    {
        return excludedVisitorMethods.contains(method);
    }

    public static boolean isClassAttributeModificationExcluded(String className)
    {
        return attributeChangesExcluded.contains(className);
    }

    public static boolean isExcludedClass(String className)
    {
        return excludedClassesPat.matcher(className).matches() || !isIncludedByConfig(className);
    }

    static boolean isIncludedByConfig(String className)
    {
        if (includedClasses.contains(className))
            return true;
        if (excludedClasses.contains(className))
            return false;
        String packName = getPackageName(className);
        if (includedClasses.contains(packName + ".*"))
            return true;
        if (excludedClasses.contains(packName + ".*"))
            return false;
        while (packName != "")
        {
            if (includedPackages.contains(packName))
                return true;
            if (excludedPackages.contains(packName))
                return false;
            packName = getPackageName(packName);
        }
        if (includedPackages.contains(""))
            return true;
        return false;
    }

    private static String getPackageName(String className)
    {
        int dotIndex = className.lastIndexOf(".");
        if (dotIndex >= 0)
            return className.substring(0, dotIndex);
        return "";
    }

    public static void loadExclusions(InputStream is) throws IOException
    {
        includedClasses = new HashSet<String>();
        excludedClasses = new HashSet<String>();
        includedPackages = new HashSet<String>();
        excludedPackages = new HashSet<String>();
        if (is.markSupported())
            is.mark(Integer.MAX_VALUE);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line = br.readLine();
        while (line != null)
        {
            try
            {
                line = line.trim();
                if (line.contains("//"))
                    line = line.substring(0, line.indexOf("//"));
                if (line.length() == 0)
                    continue;
                boolean include;
                boolean pack;
                if (line.charAt(0) == '+')
                    include = true;
                else if (line.charAt(0) == '-')
                    include = false;
                else
                {
                    if (is.markSupported())
                        is.reset();
                    throw new IOException("Lines must either be empty or start with '+' or '-'");
                }
                line = line.substring(1);
                if (line.charAt(line.length() - 1) == '.')
                    pack = true;
                else
                    pack = false;
                if (include && pack)
                    includedPackages.add(line.substring(0, line.length() - 1));
                else if (include && !pack)
                    includedClasses.add(line);
                else if (!include && pack)
                    excludedPackages.add(line.substring(0, line.length() - 1));
                else if (!include && !pack)
                    excludedClasses.add(line);
            }
            finally
            {
                line = br.readLine();
            }
        }
        if (is.markSupported())
            is.reset();
    }
}
