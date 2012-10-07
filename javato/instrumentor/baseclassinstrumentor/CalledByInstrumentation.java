package javato.instrumentor.baseclassinstrumentor;
import java.lang.annotation.*;

@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CalledByInstrumentation
{
}
