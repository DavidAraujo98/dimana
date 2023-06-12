import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Stack;

import org.stringtemplate.v4.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.runtime.ParserRuleContext;

class Var {
   String name;
   Comp_Unit unit;
   String type;
}

class Comp_Unit {
   String name;
   Dim dimension;
   String suffix;
   Double multiplier;
}

class Dim {
   String name;
   String numericType;
   Comp_Unit unit;
   String[] dependecies;
}

class Comp_Prefix {
   String name;
   Double value;
}

@SuppressWarnings("CheckReturnValue")
public class Compiler extends GrammarBaseVisitor<ST> {
   protected STGroup stg = null;
   protected int varCount = 0;

   private HashMap<String, ParseTree> importedTrees;

   private static HashMap<String, Var> vars = new HashMap<>();
   private static HashMap<String, Comp_Unit> units = new HashMap<>();
   private static HashMap<String, Dim> dims = new HashMap<>();
   private static HashMap<String, Comp_Prefix> prefixs = new HashMap<>();

   private ArrayList<String> currentFile = new ArrayList<String>();
   private ArrayList<String> visitedFiles = new ArrayList<String>();

   private String STGFileName = "java.stg";

   ST returnTemplate;

   public Compiler() {
      currentFile.add("");
   }

   public Compiler(HashMap<String, ParseTree> importedTrees) {
      this.importedTrees = importedTrees;
      currentFile.add("");
   }

   public Compiler(HashMap<String, ParseTree> importedTrees, String startFileName) {
      this.importedTrees = importedTrees;
      currentFile.add(startFileName);
   }

   @Override
   public ST visitProgram(GrammarParser.ProgramContext ctx) {
      stg = new STGroupFile("java.stg");
      returnTemplate = stg.getInstanceOf("module");
      visitChildren(ctx);
      // System.out.println("\nDIMENSIONS");
      // System.out.printf("%20s %20s %40s\n", "NAME", "UNIT", "DEPS");
      // for (Dim dim : dims.values()) {
      //    System.out.printf("%20s %20s %40s\n", dim.name, dim.unit.name, Arrays.toString(dim.dependecies));
      // }
      // System.out.println("\nUNITS");
      // System.out.printf("%20s %20s %7s %10s\n", "NAME", "DIM", "SUFFIX", "MULT");
      // for (Comp_Unit unit : units.values()) {
      //    System.out.printf("%20s %20s %7s %10.4f\n", unit.name,unit.dimension.name,unit.suffix,unit.multiplier);
      // }
      // System.out.println("\nVARIABLES");
      // System.out.printf("%20s %20s %20s\n", "NAME", "UNIT", "TYPE");
      // for(Var var : vars.values()) {
      //    System.out.printf("%20s %20s %20s\n", var.name, var.unit!=null ? var.unit.name : null, var.type);
      // }
      return returnTemplate;
   }

   @Override
   public ST visitPstat(GrammarParser.PstatContext ctx) {
      ST res = visit(ctx.stat());
      if( res!=null )
         returnTemplate.add("stat", visit(ctx.stat()).render());
      return null;
   }

   @Override
   public ST visitStatAssig(GrammarParser.StatAssigContext ctx) {
      return visit(ctx.assig());
   }

   @Override
   public ST visitStatDeclare(GrammarParser.StatDeclareContext ctx) {
      return visit(ctx.decl());
   }

   @Override
   public ST visitStatWrite(GrammarParser.StatWriteContext ctx) {
      return visit(ctx.write());
   }

   @Override
   public ST visitStatAppend(GrammarParser.StatAppendContext ctx) {
      return visit(ctx.appnd());
   }

   @Override
   public ST visitStatLoop(GrammarParser.StatLoopContext ctx) {
      return visit(ctx.loop());
   }

   @Override
   public ST visitStatConditional(GrammarParser.StatConditionalContext ctx) {
      return visit(ctx);
   }

   @Override
   public ST visitUseModule(GrammarParser.UseModuleContext ctx) {
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
   public ST visitTypeList(GrammarParser.TypeListContext ctx) {
      return new ST("");
   }

   @Override
   public ST visitTypeNumeric(GrammarParser.TypeNumericContext ctx) {
      return null;
   }

   @Override
   public ST visitTypeString(GrammarParser.TypeStringContext ctx) {
      return null;
      // return res;
   }

   @Override
   public ST visitTypeBoolean(GrammarParser.TypeBooleanContext ctx) {
      ST res = null;
      return visitChildren(ctx);
   }

   @Override
   public ST visitTypeDimension(GrammarParser.TypeDimensionContext ctx) {
      return visitChildren(ctx);
   }

   @Override
   public ST visitTypeReal(GrammarParser.TypeRealContext ctx) {
      return null;
   }

   @Override
   public ST visitTypeInteger(GrammarParser.TypeIntegerContext ctx) {
      return null;
   }

   @Override
   public ST visitDeclareVariable(GrammarParser.DeclareVariableContext ctx) {
      String type = ctx.type().getText();

      if (dims.containsKey(type)) {
         Dim d = dims.get(type);
         type = dims.get(type).numericType;
         // if( d.dependecies!=null ) {
         //    ST res = stg.getInstanceOf("stats");
         //    for(int i=0; i<d.dependecies.length; i+=2) {
         //       Var var = new Var();
         //       var.name = ctx.ID().toString()+i/2;
         //       var.type = dims.get(d.dependecies[i]).numericType;
         //       var.unit = d.unit;
         //       vars.put(var.name, var);
         //       ST resStat = stg.getInstanceOf("decl");
         //       resStat.add("type", var.type);
         //       resStat.add("var", var.name);
         //       res.add("stat", resStat.render());
         //    }
         //    return res;
         // }
      }
      Var var = new Var();
      var.name = ctx.ID().getText();
      if( dims.containsKey(ctx.type().getText()) ) var.unit = dims.get(ctx.type().getText()).unit;
      else var.unit = null;
      var.type = type;
      vars.put(ctx.ID().toString(), var);

      ST res = stg.getInstanceOf("decl");
      res.add("type", type);
      res.add("var", ctx.ID().toString());
      return res;
   }

   @Override
   public ST visitDeclareDimension(GrammarParser.DeclareDimensionContext ctx) {
      return visitChildren(ctx);
      // return res;
   }

   @Override
   public ST visitAssigVarible(GrammarParser.AssigVaribleContext ctx) {
      String id = ctx.ID().getText();
      ctx.varName = id;
      ST theChild = visit(ctx.expr());

      ST res;
      if( ctx.type()==null ) {
         res = stg.getInstanceOf("assign");
      } else if( ctx.type().getText().endsWith("]") ) {
         res = stg.getInstanceOf("declArrayList");
         String type = ctx.type().getText();
         type = type.substring(type.indexOf('[') + 1, type.length() - 1);
         res = stg.getInstanceOf("declArrayList");
         res.add("type", getDimType(type));
         res.add("value", ctx.expr().getText());
         Var var = new Var();
         var.name = ctx.ID().getText();
         var.type = getDimType(type);
         var.unit = null;
         vars.put(var.name, var);
      } else {
         String type = ctx.type().getText();
         Var var = new Var();
         var.unit = null;
         if( dims.containsKey(type) ) {
            var.unit = dims.get(type).unit;
            type = dims.get(type).numericType;
         }
         res = stg.getInstanceOf("decl2");
         res.add("type", type);
         var.name = ctx.ID().getText();
         var.type = type;
         vars.put(var.name, var);
      }

      // if( !vars.containsKey(id) ) {
      //    String[] expr = ctx.expr().getText().split("[\\*\\/]");
      //    System.out.println(Arrays.toString(expr));
      //    Dim d = vars.get(id+0).unit.dimension;
      //    ST r = stg.getInstanceOf("stats");
      //    for (int i = 0; i < d.dependecies.length; i+=2) {
      //       System.out.println("BBBBBB "+i+"  "+i/2);
      //       ST assig = stg.getInstanceOf("assign");
      //       assig.add("var", id+i/2);
      //       assig.add("value", expr[i/2]);
      //       r.add("stat", assig.render());
      //    }
      //    return r;
      //    // res.add("var", ctx.ID().getText());
      //    // res.add("value", ctx.expr().getText());
      // }
      if (theChild != null)
         res.add("stat", theChild.render());
      res.add("var", ctx.ID().getText());
      res.add("value", ctx.expr().varName);
      return res;
   }

   @Override
   public ST visitAssigNonSI(GrammarParser.AssigNonSIContext ctx) {
      visit(ctx.expr());
      Dim dim = dims.get(ctx.ID(0).getText());
      Comp_Unit u = new Comp_Unit();
      u.dimension = dim;
      u.name = ctx.unit.getText();
      u.suffix = ctx.suffix.getText();
      String[] expr = ctx.expr().getText().split("\\*");
      u.multiplier = Double.parseDouble(expr[0])*units.get(expr[1]).multiplier;
      units.put(u.name, u);
      units.put(u.suffix, u);
      return new ST("");
   }

   @Override
   public ST visitAssigDependantUnits(GrammarParser.AssigDependantUnitsContext ctx) {
      visit(ctx.dimension());

      Comp_Unit unit = new Comp_Unit();
      unit.name = ctx.dimension().unitName!=null ? ctx.dimension().unitName : ctx.dimension().dimName;
      unit.suffix = ctx.dimension().suffixS!=null ? ctx.dimension().suffixS : "";

      Dim dim = dims.get(ctx.dimension().dimName);

      ArrayList<String> deps = new ArrayList<>();
      String expr = ctx.expr().getText();
      for (int i = 0, li=0; i < expr.length(); i++) {
         if( expr.charAt(i)=='*' || expr.charAt(i)=='/' ) {
            deps.add(expr.substring(li, i));
            deps.add(""+expr.charAt(i));
            li=i+1;
            if( ctx.dimension().suffixS==null ) {
               Dim d = dims.get(deps.get(deps.size()-2));
               unit.suffix += d.unit.suffix + deps.get(deps.size()-1);
            }
         }
         if( i+1==expr.length() ) {
            deps.add(expr.substring(li, i+1));
            if( ctx.dimension().suffixS==null ) {
               Dim d = dims.get(deps.get(deps.size()-1));
               unit.suffix += d.unit.suffix;
            }
         }
      }
      dim.dependecies = deps.toArray(new String[deps.size()]);
      dim.unit = unit;
      unit.dimension = dim;
      units.put(unit.name, unit);
      units.put(unit.suffix, unit);
      return new ST("");
   }

   @Override
   public ST visitAssigSIPrefix(GrammarParser.AssigSIPrefixContext ctx) {
      Comp_Prefix p = new Comp_Prefix();
      p.name = ctx.ID().getText();
      if( ctx.INTEGER()!=null ) {
         p.value = Integer.parseInt(ctx.INTEGER().getText())*1.0;
      }else {
         p.value = Double.parseDouble(ctx.REAL().getText());
      }
      prefixs.put(p.name, p);
      return null;
   }

   @Override
   public ST visitWriteOut(GrammarParser.WriteOutContext ctx) {
      ST res = stg.getInstanceOf("show");
      for (GrammarParser.ExprContext expr : ctx.expr()) {
         res.add("stat", visit(expr).render());
         res.add("expr", expr.varName + "+");
      }
      res.add("endl", "\"\"");
      return res;
   }

   @Override
   public ST visitWriteOutNewLine(GrammarParser.WriteOutNewLineContext ctx) {
      ST res = stg.getInstanceOf("show");
      for (GrammarParser.ExprContext expr : ctx.expr()) {
         String suffix = "";
         res.add("stat", visit(expr).render());
         if( vars.containsKey(expr.getText()) ) suffix = "+\""+vars.get(expr.getText()).unit.suffix+"\"";
         res.add("expr", expr.varName + suffix + "+");
      }
      res.add("endl", "\"\\n\"");
      return res;
   }

   @Override
   public ST visitAppendToListID(GrammarParser.AppendToListIDContext ctx) {
      ST vExpr = visit(ctx.expr());
      ST res = stg.getInstanceOf("listAppend");
      res.add("stat", vExpr.render());
      res.add("var", ctx.ID().getText());
      res.add("value", ctx.expr().varName);
      return res;
   }

   @Override
   public ST visitLoopFor(GrammarParser.LoopForContext ctx) {
      ST vAssig = visit(ctx.assig());
      ST vExpr = visit(ctx.expr());

      ST res = stg.getInstanceOf("forLoop");
      res.add("pre", vAssig.render());
      res.add("pre", vExpr.render());
      res.add("var", ctx.assig().varName);
      res.add("max", ctx.expr().varName);
      for (GrammarParser.StatContext stat : ctx.stat()) {
         res.add("stat", visit(stat).render());
      }
      return res;
   }

   @Override
   public ST visitConditionalIf(GrammarParser.ConditionalIfContext ctx) {
      ST res = null;
      return visitChildren(ctx);
      // return res;
   }

   @Override public ST visitDimensionDeclare(GrammarParser.DimensionDeclareContext ctx) {
      Comp_Unit unit = new Comp_Unit();
      unit.suffix = ctx.suffix!=null ? ctx.suffix.getText() : "";
      unit.name = ctx.unit!=null ? ctx.unit.getText() : unit.suffix;
      unit.multiplier = 1.0;
      Dim dim = new Dim();
      dim.name = ctx.ID(0).getText();
      dim.numericType = ctx.numerictype().getText();

      unit.dimension = dim;
      dim.unit = unit;

      dims.put(dim.name, dim);
      units.put(unit.name, unit);
      units.put(unit.suffix, unit);

      ctx.dimName = dim.name;
      ctx.unitName = ctx.unit!=null ? ctx.unit.getText() : null;
      ctx.suffixS = ctx.suffix!=null ? ctx.suffix.getText() : null;

      // old
      // Dimension dim = new Dimension(
      //    ctx.ID(0).toString(),
      //    ctx.numerictype().getText(),
      //    ctx.unit!=null ? ctx.unit.getText() : null,
      //    ctx.suffix!=null ? ctx.suffix.getText() : null
      // );
      // dimensions.put(ctx.ID(0).toString(), dim);
      return visitChildren(ctx);
   }

   @Override
   public ST visitExprComparison(GrammarParser.ExprComparisonContext ctx) {
      ST res = null;
      return visitChildren(ctx);
      // return res;
   }

   @Override
   public ST visitExprCastWithUnitName(GrammarParser.ExprCastWithUnitNameContext ctx) {
      ST theChild = visit(ctx.expr());

      ctx.varName = newVarName();
      ctx.eType = ctx.type().getText();

      ST res = stg.getInstanceOf("decl2");
      res.add("stat", theChild.render());
      res.add("var", ctx.varName);
      res.add("type", ctx.eType);
      res.add("value", ctx.eType.equals("integer") ? "Integer.parseInt(" + ctx.expr().varName + ")"
            : "Double.parseDouble(" + ctx.expr().varName + ")");
      // TODO: missing unit cast
      return res;
   }

   @Override
   public ST visitExprCreateString(GrammarParser.ExprCreateStringContext ctx) {
      ST theChild = visit(ctx.expr());
      ctx.varName = newVarName();
      ctx.eType = "string";
      ST res = stg.getInstanceOf("stringFormat");
      res.add("stat", theChild.render());
      res.add("var", ctx.varName);
      res.add("text", ctx.expr().varName);
      res.add("alignment", ctx.INTEGER());
      return res;
   }

   @Override
   public ST visitExprConvertString(GrammarParser.ExprConvertStringContext ctx) {
      return null;
   }

   @Override
   public ST visitExprConvertNumeric(GrammarParser.ExprConvertNumericContext ctx) {
      ctx.varName = newVarName();
      ctx.eType = ctx.numerictype().getText();
      ST expr = visit(ctx.expr());
      ST res = stg.getInstanceOf("decl2");
      res.add("stat", expr.render());
      res.add("var", ctx.varName);
      res.add("type", ctx.numerictype().getText());
      if( ctx.numerictype().getText().equals("integer") ) {
         res.add("value", "Integer.parseInt("+ctx.expr().varName+")");
      } else {
         res.add("value", "Double.parseDouble("+ctx.expr().varName+")");
      }
      return res;
   }

   @Override
   public ST visitExprReadIn(GrammarParser.ExprReadInContext ctx) {
      ST res = stg.getInstanceOf("read");
      ctx.varName = newVarName();
      ctx.eType = "string";
      res.add("var", ctx.varName);
      if (ctx.STRING() != null)
         res.add("text", ctx.STRING().getText());
      return res;
   }

   @Override
   public ST visitExprNewTypeID(GrammarParser.ExprNewTypeIDContext ctx) {
      ST res = null;
      return visitChildren(ctx);
      // return res;
   }

   @Override
   public ST visitExprExpo(GrammarParser.ExprExpoContext ctx) {
      ST res = null;
      return visitChildren(ctx);
      // return res;
   }

   @Override
   public ST visitExprString(GrammarParser.ExprStringContext ctx) {
      ctx.varName = newVarName();
      ctx.eType = "string";
      ST res = stg.getInstanceOf("decl");
      res.add("type", ctx.eType);
      res.add("var", ctx.varName);
      res.add("value", ctx.STRING().getText());
      return res;
   }

   @Override
   public ST visitExprParent(GrammarParser.ExprParentContext ctx) {
      ST res = null;
      return visitChildren(ctx);
      // return res;
   }

   @Override
   public ST visitExprBoolean(GrammarParser.ExprBooleanContext ctx) {
      ST res = null;
      return visitChildren(ctx);
      // return res;
   }

   //    Not implemented
   @Override 
   public ST visitExprAndOR(GrammarParser.ExprAndORContext ctx) {
      ST res = null;
      return visitChildren(ctx);
      // return res;
   }

   @Override
   public ST visitExprIndexOfList(GrammarParser.ExprIndexOfListContext ctx) {
      ctx.varName = newVarName();
      ST vExpr = visit(ctx.expr());
      ST res = stg.getInstanceOf("decl2");
      res.add("stat", vExpr.render());
      res.add("type", vars.get(ctx.ID().getText()).type);
      res.add("var", ctx.varName);
      res.add("value", ctx.ID().getText() + ".get(" + ctx.expr().varName + "-1)");
      return res;
   }

   @Override
   public ST visitExprLenght(GrammarParser.ExprLenghtContext ctx) {
      ctx.varName = newVarName();
      ST res = stg.getInstanceOf("decl");
      res.add("type", "integer");
      res.add("var", ctx.varName);
      res.add("value", ctx.expr().getText() + ".size()");
      return res;
      // return res;
   }

   public ST visitExprPrefix(GrammarParser.ExprPrefixContext ctx) {
      ctx.varName = newVarName();
      ST res = stg.getInstanceOf("decl2");

      ST expr = visit( ctx.expr() );
      res.add("stat", expr.render());

      Double suffixValue = 1.0;
      if( units.containsKey(ctx.ID().getText()) ) {
         suffixValue = units.get(ctx.ID().getText()).multiplier;
      } else {
         int i;
         for(i=0; i<ctx.ID().getText().length(); i++) {
            if( prefixs.containsKey(ctx.ID().getText().substring(0,i)) ) {
               suffixValue = prefixs.get(ctx.ID().getText().substring(0,i)).value;
               break;
            }
         }
         if( i<ctx.ID().getText().length() ) {
            suffixValue *= units.get(ctx.ID().getText().substring(i,ctx.ID().getText().length())).multiplier;
         }
      }
      res.add("type", "real");
      res.add("var", ctx.varName);
      res.add("value", ctx.expr().varName+"*"+suffixValue);
      return res;
   }

   @Override
   public ST visitExprAddSub(GrammarParser.ExprAddSubContext ctx) {
      ST left = visit(ctx.expr(0));
      ST right = visit(ctx.expr(1));
      ctx.varName = newVarName();
      ctx.eType = ctx.expr(0).eType=="integer"&&ctx.expr(1).eType=="integer" ? "integer" : "real";
      ST res = stg.getInstanceOf("decl2");
      res.add("stat", left.render());
      res.add("stat", right.render());
      res.add("type", ctx.eType);
      res.add("var", ctx.varName);
      res.add("value", ctx.expr(0).varName + ctx.op.getText() + ctx.expr(1).varName);
      return res;
   }

   @Override
   public ST visitExprUnary(GrammarParser.ExprUnaryContext ctx) {
      ST res = null;
      return visitChildren(ctx);
      // return res;
   }

   @Override
   public ST visitExprMulDivRem(GrammarParser.ExprMulDivRemContext ctx) {
      ST left = visit(ctx.expr(0));
      ST right = visit(ctx.expr(1));
      ctx.varName = newVarName();
      ctx.eType = ctx.expr(0).eType.equals("integer")&&ctx.expr(1).eType.equals("integer") ? "integer" : "real";
      ST res = stg.getInstanceOf("decl2");
      res.add("stat", left.render());
      res.add("stat", right.render());
      res.add("type", ctx.eType);
      res.add("var", ctx.varName);
      res.add("value", ctx.expr(0).varName + ctx.op.getText() + ctx.expr(1).varName);
      return res;
   }

   @Override
   public ST visitExprReal(GrammarParser.ExprRealContext ctx) {
      ctx.varName = newVarName();
      ctx.eType = "real";
      ST res = stg.getInstanceOf("decl");
      res.add("type", ctx.eType);
      res.add("var", ctx.varName);
      res.add("value", ctx.REAL().getText());
      return res;
   }

   @Override
   public ST visitExprInteger(GrammarParser.ExprIntegerContext ctx) {
      ctx.varName = newVarName();
      ctx.eType = "integer";
      ST res = stg.getInstanceOf("decl");
      res.add("type", ctx.eType);
      res.add("var", ctx.varName);
      res.add("value", ctx.INTEGER().getText());
      return res;
   }

   @Override
   public ST visitExprID(GrammarParser.ExprIDContext ctx) {
      ctx.varName = newVarName();
      String id = ctx.ID().getText();
      if( units.containsKey(id) ) {
         // ctx.value = units.get(id).multiplier;
         ctx.eType = units.get(id).dimension.numericType;
         ST res = stg.getInstanceOf("decl");
         res.add("type", ctx.eType);
         res.add("var", ctx.varName);
         if( units.get(id).dimension.numericType.equals("integer") ) {
            res.add("value", units.get(id).multiplier.intValue());
         } else {
            res.add("value", units.get(id).multiplier);
         }
         return res;
      } else {
         ctx.eType = vars.get(id).type;
         ST res = stg.getInstanceOf("decl");
         res.add("type", ctx.eType);
         res.add("var", ctx.varName);
         res.add("value", ctx.ID().getText());
         return res;
      }
   }

   protected String newVarName() {
      varCount++;
      return "v" + varCount;
   }

   protected String getDimType(String dimName) {
      if (dims.containsKey(dimName))
         return dims.get(dimName).numericType;
      return dimName;
   }

   protected String getUnitType(String unit) {
      if (units.containsKey(unit))
         return units.get(unit).dimension.numericType;
      return unit;
   }
}
