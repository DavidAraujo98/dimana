import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.runtime.ParserRuleContext;

@SuppressWarnings("CheckReturnValue")
public class SemanticAnalysis extends GrammarBaseVisitor<Object> {

   private HashMap<String, ParseTree> importedTrees;

   private HashMap<String, Variable> variables = new HashMap<>();
   private HashMap<String, Dimension> dimensions = new HashMap<>();
   private HashMap<String, Prefix> prefix = new HashMap<>();
   private ArrayList<String> currentFile = new ArrayList<String>();
   private ArrayList<String> visitedFiles = new ArrayList<String>();

   HashSet<String> reservedWords = new HashSet<String>();
   private String importDirectory;

   private boolean freeID(String ID) {

      boolean dim_contains = false;
      for (Dimension dimension : dimensions.values()) {
         if (dimension.name.equals(ID) || 
             (dimension.suffix != null && dimension.suffix.contains(ID)) ||
             (dimension.unit != null && dimension.unit.contains(ID))) {
             dim_contains = true;
         }
     }

      return !(
            dim_contains ||
            this.variables.containsKey(ID) ||
            this.dimensions.containsKey(ID) ||
            this.prefix.containsKey(ID) ||
            this.reservedWords.contains(ID));
   }

   public SemanticAnalysis() {
      this.reservedWords.add("list");
      currentFile.add("");
      importDirectory = "";
   }

   public SemanticAnalysis(HashMap<String, ParseTree> importedTrees) {
      this.importedTrees = importedTrees;
      this.reservedWords.add("list");
      currentFile.add("");
      importDirectory = "";
   }

   public SemanticAnalysis(HashMap<String, ParseTree> importedTrees, String file, String impDirectory) {
      this.importedTrees = importedTrees;
      this.reservedWords.add("list");
      currentFile.add(file);
      importDirectory = impDirectory;
   }

   private void displayError(ParserRuleContext ctx, String error) throws RuntimeException {
      int line = ctx.getStart().getLine();
      String fileName = currentFile.get(currentFile.size() - 1);
      if(fileName == "")
         throw new RuntimeException("Error occurred in main code, at line " + line + ": " + error);
      else{
         throw new RuntimeException("Error occurred in file " + fileName + ", at line " + line + ": " + error);
      }
      //System.err.println("Error occurred in file " + currentFile.get(currentFile.size() - 1) + ", at line " + line + ": " + error);
   }

   /* Exceptions that don't generate any errors */
   @Override
   public Object visitProgram(GrammarParser.ProgramContext ctx) {
      /*
       * for (GrammarParser.StatContext stat : ctx.stat()) {
       * visit(stat);
       * }
       */
      return visitChildren(ctx);
   }

   @Override
   public Object visitStatAssig(GrammarParser.StatAssigContext ctx) {
      visit(ctx.assig());
      return null;
   }

   @Override
   public Object visitStatDeclare(GrammarParser.StatDeclareContext ctx) {
      visit(ctx.decl());
      return null;
   }

   @Override
   public Object visitStatWrite(GrammarParser.StatWriteContext ctx) {
      visit(ctx.write());
      return null;
   }

   @Override
   public Object visitStatAppend(GrammarParser.StatAppendContext ctx) {
      visit(ctx.appnd());
      return null;
   }

   @Override
   public Object visitStatLoop(GrammarParser.StatLoopContext ctx) {
      visit(ctx.loop());
      return null;
   }

   @Override
   public Object visitTypeReal(GrammarParser.TypeRealContext ctx) {
      return new String("real");
   }

   @Override
   public Object visitTypeInteger(GrammarParser.TypeIntegerContext ctx) {
      return new String("integer");
   }

   @Override
   public Object visitStatConditional(GrammarParser.StatConditionalContext ctx) {
      visit(ctx.cond());
      return null;
   }

   @Override
   public Object visitTypeList(GrammarParser.TypeListContext ctx) {
      return new Type(Types.LIST, visit(ctx.type()));
   }

   @Override
   public Object visitTypeNumeric(GrammarParser.TypeNumericContext ctx) {
      return new Type(Types.NUMERIC, ctx.numerictype().getText());
   }

   @Override
   public Object visitTypeString(GrammarParser.TypeStringContext ctx) {
      return new Type(Types.STRING);
   }

   @Override
   public Object visitTypeBoolean(GrammarParser.TypeBooleanContext ctx) {
      return new Type(Types.BOOLEAN);
   }

   @Override
   public Object visitExprParent(GrammarParser.ExprParentContext ctx) {
      return visit(ctx.expr());

   }
   /* Exprtions that generate may generate errors */

   @Override
   public Object visitUseModule(GrammarParser.UseModuleContext ctx) {
      // summon a new visitor, get his context and merge it with current context
      // (dimensions etc ...), ignore variables
      // check circular imports
      String file = ctx.STRING().getText();
      file = file.substring(1, file.length() - 1);
      if(!visitedFiles.contains(file)) {
         visitedFiles.add(file);
         ParseTree importedTree = importedTrees.get(file);

         // Imported tree
         //System.out.println(file + " (" + importedTree + ") tree: " + importedTree.toStringTree());

         currentFile.add(file);
         visit(importedTree);
         // File successfully processed, remove it from current files
         currentFile.remove(currentFile.size() - 1);
      }
      return null;
   }

   @Override
   public Object visitTypeDimension(GrammarParser.TypeDimensionContext ctx) {
      if (!this.dimensions.containsKey(ctx.ID().getText())) {
         this.displayError(ctx, "Undeclared dimension '" + ctx.ID().getText() + "'");
         return null;
      }

      return new Type(Types.DIMENSION,this.dimensions.get(ctx.ID().getText()));
   }

   @Override
   public Object visitDeclareVariable(GrammarParser.DeclareVariableContext ctx) {

      if (this.variables.containsKey(ctx.ID().getText())) {
         this.displayError(ctx, "Variable '" + ctx.ID().getText() + "' already declared");
         return null;
      }

      if (!this.freeID(ctx.ID().getText())) {
         this.displayError(ctx, "Use of reserved ID '" + ctx.ID().getText() + "'");
         return null;
      }

      // declare variable
      this.variables.put(ctx.ID().getText(), new Variable(ctx.ID().getText(), (Type)visit(ctx.type()), null));
      return null;
   }

   @Override
   public Object visitDeclareDimension(GrammarParser.DeclareDimensionContext ctx) {
      Type t = (Type)visit(ctx.dimension());
      Dimension d = (Dimension)t.meta;
      this.dimensions.put(d.name, d);
      return null;
   }

   @Override
   public Object visitAssigVarible(GrammarParser.AssigVaribleContext ctx) {
      if (ctx.type() != null && this.variables.containsKey(ctx.ID().getText())) {
         this.displayError(ctx, "Variable " + ctx.ID().getText() + " cannot be reassigned");
         return null;
      }

      if (ctx.type() == null && !this.variables.containsKey(ctx.ID().getText())) {
         this.displayError(ctx, "Variable " + ctx.ID().getText() + " has not been declared");
         return null;
      }

      if (ctx.type() != null && !this.freeID(ctx.ID().getText())) {
         this.displayError(ctx, "Use of reserved ID '" + ctx.ID().getText() + "'");
         return null;
      }


      Type type = ctx.type() != null ? (Type)(visit(ctx.type())) : (Type)(this.variables.get(ctx.ID().getText())).type;
      Type expr = (Type)(visit(ctx.expr()));
      
      if(ctx.type() != null && Type.MatchTypes(type, expr,true)) {
         this.variables.put(ctx.ID().getText(), new Variable(ctx.ID().getText(), expr , expr.meta));
      }


      if ((Type.MatchTypes(expr,type,true) == false)) {
         this.displayError(ctx, "Right side of the expression doesn't match '" + ctx.ID().getText() + "' type");
         return null;
         }
      

      return null;
   }

   @Override
   public Object visitAssigNonSI(GrammarParser.AssigNonSIContext ctx) {

      Type t = (Type) visit(ctx.expr());
      if (!(t.type.compareTo(Types.DIMENSION) == 0)) {
         this.displayError(ctx, "Expected dimensional type on the right side of the expression");
         return null;
      }

      if (!this.dimensions.containsKey(ctx.ID(0).getText())) {
         this.displayError(ctx, "Unknown dimension while declaring unit '" + ctx.ID(1).getText() + "'");
         return null;
      }

      Dimension d = this.dimensions.get(ctx.ID(0).getText());
      String suffix = ctx.ID(1).getText();
      String unit = ctx.ID(2).getText();

      boolean _b2 = d.addUnit(unit);
      if(!_b2)
         this.displayError(ctx, "The unit '" + suffix + "' is already in use");

      boolean _b1 = d.addSuffix(suffix);
      if(!_b1)
         this.displayError(ctx, "The suffix '" + suffix + "' is already in use");


      return null;

   }

   @Override
   public Object visitAssigDependantUnits(GrammarParser.AssigDependantUnitsContext ctx) {
      Type t = (Type) visit(ctx.expr());

      Type dim_type = (Type)visit(ctx.dimension());

      Dimension dim = (Dimension)dim_type.meta;

      if (!(t.type.compareTo(Types.DIMENSION) == 0)) {
         this.displayError(ctx, "Expected dimensional type on the right side of the expression");
         return null;
      }
      
      Dimension expr_dimension = (Dimension)t.meta;

      expr_dimension.name = dim.name;
      expr_dimension.suffix = dim.suffix;
      expr_dimension.unit = dim.unit;

      this.dimensions.put(expr_dimension.name, expr_dimension);

      return null;
   }

   @Override
   public Object visitAssigSIPrefix(GrammarParser.AssigSIPrefixContext ctx) {
      boolean is_real = ctx.REAL() != null;
      Double value = null;
      if (is_real) {
         try {
            value = Double.parseDouble(ctx.REAL().getText());
         } catch (Exception e) {
            this.displayError(ctx, e.toString());
            return null;
         }
      } else { // is integer
         try {
            value = Double.parseDouble(ctx.INTEGER().getText());
         } catch (Exception e) {
            this.displayError(ctx, e.toString());
            return null;
         }
      }

      if (this.prefix.containsKey(ctx.ID().getText())) {
         this.displayError(ctx, "Prefix '" + ctx.ID().getText() + "' already defined");
         return null;
      }

      Prefix new_prefix = new Prefix(ctx.ID().getText(), new Type(Types.NUMERIC, (String)visit(ctx.numerictype())));
      this.prefix.put(new_prefix.name, new_prefix);
      return null;
   }

   @Override
   public Object visitExprPrefix(GrammarParser.ExprPrefixContext ctx)
   {
      Type t = (Type)visit(ctx.expr());
      if(!t.type.equals(Types.NUMERIC))
      {
         displayError(ctx, "Expected a numeric type");
      }

      String ID = ctx.ID().getText();
      Dimension d1 = this.hasDimensionWithUnit(ID);
      if(d1 != null)
         return new Type(Types.DIMENSION, d1);

      for (int i = 0; i < ID.length(); i++) {
         String prefix = ID.substring(0, i);
         String suffix = ID.substring(i);
         Dimension dim = this.hasDimensionWithSuffix(suffix);

         if(this.prefix.containsKey(prefix) && dim != null)
         {
            return new Type(Types.DIMENSION, dim);
         }
      }

      this.displayError(ctx, "'" + ID + "' is not a known unit or it isn't a known prefix and suffix union");
      return null;

      
   }

   @Override
   public Object visitExprConvertNumeric(GrammarParser.ExprConvertNumericContext ctx)
   {

      Type t = (Type)visit(ctx.expr());
      String numeric_type = (String)visit(ctx.numerictype());

      if (!t.type.equals(Types.STRING) && !t.type.equals(Types.NUMERIC))
         this.displayError(ctx, "Cannot convert a non string expression to" + numeric_type);

      return new Type(Types.NUMERIC, numeric_type);
   }

   @Override
   public Object visitExprConvertString(GrammarParser.ExprConvertStringContext ctx)
   {
      Type t = (Type)visit(ctx.expr());

      Type str = new Type(Types.STRING, null);
      if(!Type.canConvertTo(t, str))
         this.displayError(ctx, "Cannot convert expression to string");
      
      return new Type(Types.STRING, null);
   }

   @Override
   public Object visitWriteOut(GrammarParser.WriteOutContext ctx) {
      for (GrammarParser.ExprContext expr : ctx.expr())
         visit(expr);

      return null;
   }

   @Override
   public Object visitWriteOutNewLine(GrammarParser.WriteOutNewLineContext ctx) {
      for (GrammarParser.ExprContext expr : ctx.expr())
            visit(expr);

      return null;
   }

   @Override
   public Object visitAppendToListID(GrammarParser.AppendToListIDContext ctx) {

      if (!this.variables.containsKey(ctx.ID().getText())) {
         this.displayError(ctx, "Variable '" + ctx.ID().getText() + " is not defined");
      }

      Variable v = this.variables.get(ctx.ID().getText());


      //verify that the variable is a list
      if (!v.type.type.equals(Types.LIST))
         this.displayError(ctx, "Variable " + v.name + " is not of type list, append at the end is not allowed");

      //if it's a list check if variable list type matches push type
      Type A = (Type)v.type.meta;
      Type B = (Type)visit(ctx.expr());

      if (!Type.MatchTypes(A, B, true)) {
         this.displayError(ctx, "Cannot append to '" + ctx.ID().getText() + "' a different type");
      }
      return null;

   }

   @Override
   public Object visitLoopFor(GrammarParser.LoopForContext ctx) {

      visit(ctx.assig());
      visit(ctx.expr());
      for (GrammarParser.StatContext stat : ctx.stat()) {
         visit(stat);
      }

      return null;
   }

   @Override
   public Object visitConditionalIf(GrammarParser.ConditionalIfContext ctx) {
      Type t = (Type)visit(ctx.expr());
      if(!t.type.equals(Types.BOOLEAN))
      {
         this.displayError(ctx, "If condition must be a boolean expression");
      }
      for (GrammarParser.StatContext stat : ctx.stat()) {
         visit(stat);
      }
      return null;
   }

   @Override
   public Object visitDimensionDeclare(GrammarParser.DimensionDeclareContext ctx) {
      if (!this.freeID(ctx.ID(0).getText())) {
         this.displayError(ctx, "Use of reserved ID '" + ctx.ID(0).getText() + "'");
      } else {
         String suffix = null;
         String unit = null;

         if (ctx.ID(1) != null)
            unit = ctx.ID(1).getText();

         if (ctx.suffix != null)
            suffix = ctx.suffix.getText();

         return new Type(Types.DIMENSION,
               new Dimension(ctx.ID(0).getText(), ctx.numerictype().getText(), unit, suffix));
      }

      return null;
   }

   @Override
   public Object visitExprExpo(GrammarParser.ExprExpoContext ctx) {
      Type expr = (Type)visit(ctx.expr());
      int i = Integer.parseInt(ctx.INTEGER().getText());

      if(!Type.MatchTypes(expr, new Type(Types.NUMERIC))){
         this.displayError(ctx, "Can't calculate '^' of type + '" + expr.type.name() + "'");
         return null;
      }

      if(Type.MatchTypes(expr, new Type(Types.NUMERIC, "real"), true)){
         return new Type(Types.NUMERIC, "real");
      }

      return new Type(Types.NUMERIC, "integer");
   }

   @Override
   public Object visitExprAddSub(GrammarParser.ExprAddSubContext ctx) {
      // can only '+' strings || '+' or '-' between numeric's, if one side of the
      // expression is real, returns a real type. Retun integer otherwise
      String operator = ctx.op.getText();
      Type left_type = (Type)visit(ctx.expr(0));
      Type right_type = (Type)visit(ctx.expr(1));

      Type string_type = new Type(Types.STRING,null);
      Type numeric_type = new Type(Types.NUMERIC,null);

      if(!Type.MatchTypes(left_type,right_type,false))
      {
         this.displayError(ctx, "Cannot '+' or '-' different types");
         return null;
      }

      if(left_type.type.equals(Types.DIMENSION) && right_type.type.equals(Types.DIMENSION))
      {
         Dimension d1 = (Dimension)left_type.meta;
         Dimension d2 = (Dimension)right_type.meta;

         if(!d1.equals(d2))
            this.displayError(ctx, "Cannot '+' or '-' different dimensions");
         
         return new Type(Types.DIMENSION, d1);
      }

      boolean is_string_op = Type.MatchTypes(left_type, string_type);
      boolean is_numeric_op = Type.MatchTypes(left_type, numeric_type);
      
      if(!is_string_op && !is_numeric_op)
      {
         this.displayError(ctx, "'+' and '-' is not defined for this expression");
         return null;
      }

      if(is_string_op && operator.equals('-'))
      {
         this.displayError(ctx, "Cannot subtract strings");
         return null;
      }

      //find return type
      if(is_string_op)
         return string_type;

      if(is_numeric_op)
      {
         boolean real_exists = ((String)left_type.meta).equals("real") || ((String)right_type.meta).equals("real");

         if(real_exists)
            return new Type(Types.NUMERIC, "real");
         else{
            return new Type(Types.NUMERIC, "integer");
         }
      }

      return null;
   }

   @Override
   public Object visitExprCreateString(GrammarParser.ExprCreateStringContext ctx) {
      Type expr = (Type)(visit(ctx.expr()));
      if(Type.canConvertTo(expr, new Type(Types.STRING, null))){
         return new Type(Types.STRING,null);
      }

      this.displayError(ctx, "Expression format is incorrect");
      return null;
   }

   @Override
   public Object visitExprComparison(GrammarParser.ExprComparisonContext ctx) {
      Type expr1 = (Type)(visit(ctx.expr(0)));
      Type expr2 = (Type)(visit(ctx.expr(1)));
      String op = ctx.op.getText();

      if(!Type.MatchTypes(expr1, expr2)){
         this.displayError(ctx, "Cannot compare different types");
      }
      
      // booleans and strings only support == && !=
      // lists not supported
      // integers and reals can be compared
      if(Type.MatchTypes(expr1, new Type(Types.BOOLEAN)) || Type.MatchTypes(expr1, new Type(Types.STRING))){
         if(!(op.equals("==") || op.equals("!="))){
            this.displayError(ctx, "Type '" + expr1.type.name() + "' cannot be compared with '" + op + "'");
         }
         return new Type(Types.BOOLEAN);
      }

      if(Type.MatchTypes(expr1, new Type(Types.LIST))){
         this.displayError(ctx, "Cannot compare list types");
         return null;
      }

      if(Type.MatchTypes(expr1, new Type(Types.NUMERIC))){
         return new Type(Types.BOOLEAN);
      }

      return null;
   }

   @Override
   public Object visitExprID(GrammarParser.ExprIDContext ctx) {

      String ID = ctx.ID().getText();

      if (freeID(ID)) {
         this.displayError(ctx,
               "Expression '" + ID + "' does not correspond to any existing identifier");
         return false;
      }

      if(this.variables.containsKey(ID))
         return (Type) this.variables.get(ID).type;

      if(this.prefix.containsKey(ID))
      {
         Type t = this.prefix.get(ID).type;
         return t;
      }


      Dimension dim = null;
      for (Dimension dimension : dimensions.values()) {
         if (dimension.name.equals(ID) || 
               (dimension.suffix != null && dimension.suffix.contains(ID)) ||
               (dimension.unit != null && dimension.unit.contains(ID))) {
               dim = dimension;
               break;
         }
      }

      if(dim != null)
         return new Type(Types.DIMENSION, dim);

      return null;

   }

   @Override
   public Object visitExprNewTypeID(GrammarParser.ExprNewTypeIDContext ctx) {
      Type A = (Type) visit(ctx.type());
      if (A.type.compareTo(Types.LIST) == 0) {
         Type B = (Type) A.meta;
         return new Type(A.type, B);
      }
      // this.displayError(ctx, "Type '" + A.type.name() + "' cannot be initiated as
      // new");
      return false;
   }

   @Override
   public Object visitExprIndexOfList(GrammarParser.ExprIndexOfListContext ctx) {

      if (!this.variables.containsKey(ctx.ID().getText())) {
         this.displayError(ctx, "Unrecognized variable '" + ctx.ID().getText() + "'");
         return null;
      }

      Variable ID_variable = this.variables.get(ctx.ID().getText());
   
      //verify that ID is of type list
      Type id_type = (Type)ID_variable.type;
      if(id_type.type.compareTo(Types.LIST) != 0)
      {
         this.displayError(ctx, "Cannot get index of a non list type");
         return null;
      }

      // check if ID meta type matches integer

      Type meta_type = (Type) id_type.meta;
      Type expected_type = new Type(Types.NUMERIC, "integer");

      Type t = (Type)visit(ctx.expr());

      if(!Type.MatchTypes(t, expected_type, true))
      {
         this.displayError(ctx, "Cannot access a non integer index");
         return null;
      }
      
      return meta_type;
   }

   @Override
   public Object visitExprBoolean(GrammarParser.ExprBooleanContext ctx) {
      return new Type(Types.BOOLEAN, null);
   }

   @Override 
   public Object visitExprAndOR(GrammarParser.ExprAndORContext ctx) {
      Type cond1 = (Type)visit(ctx.expr(0));
      Type cond2 = (Type)visit(ctx.expr(1));
      Type bool_type = new Type(Types.BOOLEAN, null);
      if(!(Type.MatchTypes(cond1,bool_type) && Type.MatchTypes(cond2,bool_type)))
         this.displayError(ctx, "The boolean expression is not of type boolean");
      
      return bool_type;
   }

   Dimension hasDimensionWithUnit(String unit) {
      for (Dimension dimension : dimensions.values()) {
          if (dimension.unit != null && dimension.unit.contains(unit)) {
              return dimension;
          }
      }
      return null;
  }

  Dimension hasDimensionWithSuffix(String suffix) {
   for (Dimension dimension : dimensions.values()) {
       if (dimension.suffix != null && dimension.suffix.contains(suffix)) {
           return dimension;
       }
   }
   return null;


   }

   @Override
   public Object visitExprCastWithUnitName(GrammarParser.ExprCastWithUnitNameContext ctx) {
      Type type = (Type)visit(ctx.type());
      Type expr = (Type)visit(ctx.expr());

      if(!Type.canConvertTo(type, expr))
      {
         this.displayError(ctx, "Cannot convert expr type to " + type.toString());
         return null;
      }

      //get dimension
      Dimension id_dimension = this.hasDimensionWithUnit(ctx.ID().getText());
      if(id_dimension != null)
      {
         if(!id_dimension.numerictype.equals(type.meta.toString()))
         {
            this.displayError(ctx, "Cannot cast with different types");
            return null;
         }
      }
      else{
         this.displayError(ctx, "Unrecognized unit name");
         return null;
      }

      return new Type(Types.DIMENSION, id_dimension);
   }

   @Override
   public Object visitExprReadIn(GrammarParser.ExprReadInContext ctx) {
      return new Type(Types.STRING, null);
   }

   @Override
   public Object visitExprUnary(GrammarParser.ExprUnaryContext ctx) {
      Type expected_type = new Type(Types.NUMERIC, null);
      Type expr_type = (Type) visit(ctx.expr());

      if (!Type.MatchTypes(expected_type, expr_type, false)) {
         this.displayError(ctx, "Expected numeric type");
         return null;
      }

      return expr_type;
   }

   @Override
   public Object visitExprString(GrammarParser.ExprStringContext ctx) {
      return new Type(Types.STRING, ctx.STRING().getText());
   }

   @Override
   public Object visitExprReal(GrammarParser.ExprRealContext ctx) {
      return new Type(Types.NUMERIC, "real");
   }

   @Override
   public Object visitExprInteger(GrammarParser.ExprIntegerContext ctx) {
      return new Type(Types.NUMERIC, "integer");
   }

   @Override
   public Object visitExprLenght(GrammarParser.ExprLenghtContext ctx) {
      Type expr = (Type) (visit(ctx.expr()));

      if (Type.MatchTypes(expr, new Type(Types.LIST)) || Type.MatchTypes(expr, new Type(Types.STRING)))
         return new Type(Types.NUMERIC, "integer");

      this.displayError(ctx, "Cannot get length of '" + expr.type.name() + "' Type");
      return false;
   }

   @Override
   public Object visitExprMulDivRem(GrammarParser.ExprMulDivRemContext ctx) {
      Type expr1 = (Type)(visit(ctx.expr(0)));
      Type expr2 = (Type)(visit(ctx.expr(1)));
      String op = ctx.op.getText();


      boolean has_1_dim = expr1.type.equals(Types.DIMENSION) ^ expr2.type.equals(Types.DIMENSION);

      boolean has_2_dim = expr1.type.equals(Types.DIMENSION) && expr2.type.equals(Types.DIMENSION);

      if(has_2_dim)
      {
         Dimension merged = Dimension.mergeDimensions((Dimension)expr1.meta,(Dimension)expr2.meta,op);
         //check if there is a saved dimension like this one  and simplify if true
         for(Dimension dim: this.dimensions.values())
         {
            if(dim.is_composed && merged.equals(dim))
               return new Type(Types.DIMENSION,dim);
         }
         return new Type(Types.DIMENSION, merged);
      }

      if(has_1_dim)
      {
         return expr1.type.equals(Types.DIMENSION) ? expr1 : expr2;
      }
      else{
         if(Type.MatchTypes(expr1, expr2) && expr1.type.compareTo(Types.NUMERIC) == 0){
            if(((String)expr1.meta).equals("real") || ((String)expr2.meta).equals("real")){
               return new Type(Types.NUMERIC, "real");
            }
            return new Type(Types.NUMERIC, "integer");
         }
      }




      if(!Type.MatchTypes(expr1, expr2)){
         this.displayError(ctx, "Operation '" + op + "' cannot be realized between types '" + expr1.type.name() + "' and '" + expr2.type.name() + "'");
         return null;
      }
      this.displayError(ctx, "Operation '" + op + "' not compatible with type '" + expr1.type.name());
      return null;
   }
}
