package javato.instrumentor.baseclassinstrumentor;

interface ThreadLocal<T>
{
    T get();
    void remove();
    void set(T value);
}
