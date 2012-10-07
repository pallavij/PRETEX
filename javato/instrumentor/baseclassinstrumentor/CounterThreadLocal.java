package javato.instrumentor.baseclassinstrumentor;

import javato.instrumentor.baseclassinstrumentor.Tracer.Counter;

final class CounterThreadLocal implements ThreadLocal<Tracer.Counter>
{
    public CounterThreadLocal()
    {
        tl = new java.lang.ThreadLocal<Tracer.Counter>()
        {
            protected Tracer.Counter initialValue()
            {
                return new Tracer.Counter(0);
            }
        };
    }

    private java.lang.ThreadLocal<Tracer.Counter> tl;

    public Counter get()
    {
        return tl.get();
    }

    public void remove()
    {
        tl.remove();
    }

    public void set(Counter value)
    {
       tl.set(value);
    }

}
