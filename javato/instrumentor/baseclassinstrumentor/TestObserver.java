package javato.instrumentor.baseclassinstrumentor;

import javato.observer.Observer;
import java.util.*;

public class TestObserver extends Observer
{
    private static Map<Thread, Integer> depths;
    static
    {
        depths = new HashMap<Thread, Integer>();
        ArrayList al = new ArrayList();
        al.add(new Object());
        al.add(new Object());
        //al.add(new TreeSet());
    }

    @CalledByInstrumentation
    public static void enter(int i, Object obj)
    {
        //TreeSet ts = new TreeSet();
        if (!depths.containsKey(Thread.currentThread()))
        {
            depths.put(Thread.currentThread(), 0);
        }
        depths.put(Thread.currentThread(), depths.get(Thread.currentThread()) + 1);
        String indent = "";
        int amt = depths.get(Thread.currentThread());
        amt = Thread.currentThread().getStackTrace().length - 2;
        for (int x = 0; x < amt; x++)
            indent += "  ";
        synchronized (System.out)
        {
            System.out.println(indent + "-->Thread " + Thread.currentThread().getId()
                    + " Entering  " + i + " " + obj);
        }
    }

    @CalledByInstrumentation
    public static void exit(int i, Object obj)
    {
        //System.out.println("A");
        //Tracer.calledFromObserver();
        //System.out.println("B");
        if (!depths.containsKey(Thread.currentThread()))
        {
            depths.put(Thread.currentThread(), 0);
        }
        String indent = "";
        int amt = depths.get(Thread.currentThread());
        amt = Thread.currentThread().getStackTrace().length - 2;
        for (int x = 0; x < amt; x++)
            indent += "  ";
        synchronized (System.out)
        {
            System.out.println(indent + "<--Thread " + Thread.currentThread().getId()
                    + " Exiting  " + i + " " + obj);
        }
        if (depths.get(Thread.currentThread()) > 0)
            depths.put(Thread.currentThread(), depths.get(Thread.currentThread()) - 1);
    }
}
