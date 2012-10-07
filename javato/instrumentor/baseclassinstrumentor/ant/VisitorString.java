package javato.instrumentor.baseclassinstrumentor.ant;

public class VisitorString
{
    private String visitorName;

    public VisitorString()
    {
    }

    public void addText(String str)
    {
        visitorName = str.trim();
        if (visitorName.split("\n").length > 1)
            throw new RuntimeException("Invalid Visitor String");
    }
    public String getVisitorName()
    {
        return visitorName;
    }
    public String toString()
    {
        return visitorName;
    }
}
