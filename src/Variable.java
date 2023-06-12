public class Variable {
    public Object value;
    
    public Type type;
    public String name;

    public Variable(String name, Type type, Object value)
    {
        this.name = name;
        this.type = type;
        this.value = value;
    }

}
