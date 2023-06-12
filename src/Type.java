import javax.lang.model.element.Element;

public class Type {
    
    //store dimension object for example
    public Object meta;

    public Types type;
    public Type(Types type)
    {
        this.type = type;
        this.meta = null;
    }

    public static boolean canConvertTo(Type A, Type B)
    {
        //check if Type A can be converted in Type B
        if(Type.MatchTypes(A,B))
        {
            return true;
        }

        if((A.type.equals(Types.STRING) && B.type.equals((Types.NUMERIC))) || (A.type.equals(Types.NUMERIC) && B.type.equals((Types.STRING))))
        {
            return true;
        }
        
        if(A.type.equals(Types.DIMENSION))
        {
            Type t = new Type(Types.NUMERIC, ((Dimension)A.meta).numerictype);
            return Type.canConvertTo(t, B);
        }

        if(B.type.equals(Types.DIMENSION))
        {
            Type t = new Type(Types.NUMERIC, ((Dimension)B.meta).numerictype);
            return Type.canConvertTo(A, t);
        }

        return false;
    }

    public static boolean MatchTypes (Type A, Type B)
    {
        return MatchTypes(A, B, false);
    }

    public static boolean MatchTypes (Type A, Type B, boolean restricted)
    {
        if(!A.type.equals(B.type))
        {
            return false;
        }
        else{
            Types t = A.type;

            if(A.type.equals(Types.DIMENSION))
            {
                Dimension DA = (Dimension)A.meta;
                Dimension DB = (Dimension)B.meta;

                return DA.equals(DB);
            }

            if(t.compareTo(Types.LIST) == 0)
            {
                if((Type)A.meta == null || (Type)B.meta == null)
                    return true;
                    
                return MatchTypes((Type)A.meta, (Type)B.meta, restricted);
            }
            else if(t.compareTo(Types.NUMERIC) == 0 && restricted){
                return A.meta.toString().equals(B.meta.toString());
            }
            else{
                return true;
            }
        }
    }

    public Type(Types type, Object meta)
    {
        this.type = type;
        this.meta = meta;

    }
}
