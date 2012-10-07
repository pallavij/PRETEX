package javato.instrumentor.baseclassinstrumentor;

public class Tracer
{
    static class Counter
    {
        private int count;

        public Counter(int count)
        {
            this.count = count;
        }

        @Override
        protected Object clone() throws CloneNotSupportedException
        {
            return new Counter(count);
        }

        public void decrementCount()
        {
            count--;
        }

        @Override
        public boolean equals(Object obj)
        {
            return this == obj;
        }

        @Override
        protected void finalize() throws Throwable
        {
        }

        public int getCount()
        {
            return count;
        }

        @Override
        public int hashCode()
        {
            throw new RuntimeException("HashCode not supported!");
        }

        public void incrementCount()
        {
            count++;
        }

        public boolean isActive()
        {
            return count > 0;
        }

        @Override
        public String toString()
        {
            return "Counter: " + count;
        }
    }

    private static int bailoutCount = 0;

    private static Object bailoutCountLock = new Object();

    private static ThreadLocal<Counter> locals;

    public static void setLocals(ThreadLocal<Counter> locals)
    {
        Tracer.locals = locals;
    }

    private static boolean override = false;

    public static boolean calledFromObserver()
    {
        boolean active = locals.get().isActive();
        if (!override && active)
        {
            synchronized (bailoutCountLock)
            {
                bailoutCount++;
            }
        }
        return override || active;
    }

    public static int getBailoutCount()
    {
        return bailoutCount;
    }

    public static void mark()
    {
        locals.get().incrementCount();
    }

    public static void overrideAll(boolean value)
    {
        override = value;
    }

    public static void unmark()
    {
        locals.get().decrementCount();
    }
}
