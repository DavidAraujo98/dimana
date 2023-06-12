import java.util.LinkedList;

enum OP_TYPE
{
    PRODUCT,
    DIVISION
};

public class Dimension {
    public String name;
    public LinkedList<String> unit = new LinkedList<>();
    public LinkedList<String> suffix = new LinkedList<>();
    public String numerictype;

    boolean is_composed = false;
    LinkedList<Object> expr_tokens;

    public Dimension(String name, String numerictype, String unit, String suffix)
    {
        this.name = name;
        this.unit.add(unit);
        this.suffix.add(suffix);
        this.numerictype = numerictype;

        this.expr_tokens = new LinkedList<>();
    }

    public boolean addUnit(String unit)
    {
        if(this.unit.contains(unit))
            return false;
        this.unit.add(unit);
        return true;
    }

    public boolean addSuffix(String suffix)
    {
        if(this.suffix.contains(suffix))
            return false;
        this.suffix.add(suffix);
        return true;
    }

    @Override
    public boolean equals(Object obj)
    {
        if(this == obj)
            return true;

        if(obj == null || getClass() != obj.getClass())
            return false;

        Dimension d = (Dimension)obj;

        if(this.is_composed && d.is_composed)
        {
            return this.expr_tokens.equals(d.expr_tokens);
        }
        else{
            if(this.name != null && d.name != null)
                return this.name.equals(d.name);
            return false;
        }
    }

    public static Dimension mergeDimensions(Dimension A, Dimension B, String operator) {
        Dimension mergedDimension = new Dimension(null,A.numerictype,null,null);
        mergedDimension.is_composed = true;

        OP_TYPE op_type = operator.compareTo("*") == 0 ? OP_TYPE.PRODUCT : OP_TYPE.DIVISION;

        if (op_type == OP_TYPE.PRODUCT) {
            mergedDimension.expr_tokens.add(A);
            mergedDimension.expr_tokens.add("*");
            mergedDimension.expr_tokens.add(B);
        } else if (op_type == OP_TYPE.DIVISION) {
            mergedDimension.expr_tokens.add(A);
            mergedDimension.expr_tokens.add("/");
            mergedDimension.expr_tokens.add(B);
        }

        return mergedDimension;
    }
}
