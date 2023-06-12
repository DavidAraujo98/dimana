import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;
@SuppressWarnings("CheckReturnValue")
public class PreCompiler extends GrammarBaseVisitor<List<String>> {

   private List<String> fileNames = new LinkedList<String>();

   public List<String> getFileNames() {
      return fileNames;
   }
   
   public void setFileNames(List<String> fileNames) {
      this.fileNames = fileNames;
   }

   @Override public List<String> visitProgram(GrammarParser.ProgramContext ctx) {
      //visit(ctx.type());
      for(var t: ctx.use())
      {
         visit(t);
      }

      return fileNames;         
   }

   @Override public List<String> visitStatAssig(GrammarParser.StatAssigContext ctx) {
      return visitChildren(ctx);
   }

   @Override public List<String> visitStatDeclare(GrammarParser.StatDeclareContext ctx) {
      return visitChildren(ctx);
   }

   @Override public List<String> visitStatWrite(GrammarParser.StatWriteContext ctx) {
      return visitChildren(ctx);
   }

   @Override public List<String> visitStatAppend(GrammarParser.StatAppendContext ctx) {
      return visitChildren(ctx);
   }

   @Override public List<String> visitStatLoop(GrammarParser.StatLoopContext ctx) {
      return visitChildren(ctx);
   }

   @Override public List<String> visitStatConditional(GrammarParser.StatConditionalContext ctx) {
      return visitChildren(ctx);
   }

   @Override public List<String> visitUseModule(GrammarParser.UseModuleContext ctx) {
      String file = ctx.STRING().getText();
      file = file.substring(1, file.length() - 1);
      fileNames.add(file);
      return null;
   }

   @Override public List<String> visitTypeList(GrammarParser.TypeListContext ctx) {
      return visitChildren(ctx);
   }

   @Override public List<String> visitTypeNumeric(GrammarParser.TypeNumericContext ctx) {
      return visitChildren(ctx);
   }

   @Override public List<String> visitTypeString(GrammarParser.TypeStringContext ctx) {
      return visitChildren(ctx);
   }

   @Override public List<String> visitTypeBoolean(GrammarParser.TypeBooleanContext ctx) {
      return visitChildren(ctx);
   }

   @Override public List<String> visitTypeDimension(GrammarParser.TypeDimensionContext ctx) {
      return visitChildren(ctx);
   }

   @Override public List<String> visitTypeReal(GrammarParser.TypeRealContext ctx) {
      return visitChildren(ctx);
   }

   @Override public List<String> visitTypeInteger(GrammarParser.TypeIntegerContext ctx) {
      return visitChildren(ctx);
   }

   @Override public List<String> visitDeclareVariable(GrammarParser.DeclareVariableContext ctx) {
      return visitChildren(ctx);
   }

   @Override public List<String> visitDeclareDimension(GrammarParser.DeclareDimensionContext ctx) {
      return visitChildren(ctx);
   }

   @Override public List<String> visitAssigVarible(GrammarParser.AssigVaribleContext ctx) {
      return visitChildren(ctx);
   }

   @Override public List<String> visitAssigNonSI(GrammarParser.AssigNonSIContext ctx) {
      return visitChildren(ctx);
   }

   @Override public List<String> visitAssigDependantUnits(GrammarParser.AssigDependantUnitsContext ctx) {
      return visitChildren(ctx);
   }

   @Override public List<String> visitAssigSIPrefix(GrammarParser.AssigSIPrefixContext ctx) {
      return visitChildren(ctx);
   }

   @Override public List<String> visitWriteOut(GrammarParser.WriteOutContext ctx) {
      return visitChildren(ctx);
   }

   @Override public List<String> visitWriteOutNewLine(GrammarParser.WriteOutNewLineContext ctx) {
      return visitChildren(ctx);
   }

   @Override public List<String> visitAppendToListID(GrammarParser.AppendToListIDContext ctx) {
      return visitChildren(ctx);
   }

   @Override public List<String> visitLoopFor(GrammarParser.LoopForContext ctx) {
      return visitChildren(ctx);
   }

   @Override public List<String> visitConditionalIf(GrammarParser.ConditionalIfContext ctx) {
      return visitChildren(ctx);
   }

   @Override public List<String> visitDimensionDeclare(GrammarParser.DimensionDeclareContext ctx) {
      return visitChildren(ctx);
   }

   @Override public List<String> visitExprComparison(GrammarParser.ExprComparisonContext ctx) {
      return visitChildren(ctx);
   }

   @Override public List<String> visitExprCastWithUnitName(GrammarParser.ExprCastWithUnitNameContext ctx) {
      return visitChildren(ctx);
   }

   @Override public List<String> visitExprCreateString(GrammarParser.ExprCreateStringContext ctx) {
      return visitChildren(ctx);
   }

   @Override public List<String> visitExprReadIn(GrammarParser.ExprReadInContext ctx) {
      return visitChildren(ctx);
   }

   @Override public List<String> visitExprNewTypeID(GrammarParser.ExprNewTypeIDContext ctx) {
      return visitChildren(ctx);
   }

   @Override public List<String> visitExprExpo(GrammarParser.ExprExpoContext ctx) {
      return visitChildren(ctx);
   }

   @Override public List<String> visitExprString(GrammarParser.ExprStringContext ctx) {
      return visitChildren(ctx);
   }

   @Override public List<String> visitExprParent(GrammarParser.ExprParentContext ctx) {
      return visitChildren(ctx);
   }

   @Override public List<String> visitExprBoolean(GrammarParser.ExprBooleanContext ctx) {
      return visitChildren(ctx);
   }

   @Override public List<String> visitExprAndOR(GrammarParser.ExprAndORContext ctx) {
      return visitChildren(ctx);
   }

   @Override public List<String> visitExprIndexOfList(GrammarParser.ExprIndexOfListContext ctx) {
      return visitChildren(ctx);
   }

   @Override public List<String> visitExprLenght(GrammarParser.ExprLenghtContext ctx) {
      return visitChildren(ctx);
   }

   @Override public List<String> visitExprAddSub(GrammarParser.ExprAddSubContext ctx) {
      return visitChildren(ctx);
   }

   @Override public List<String> visitExprUnary(GrammarParser.ExprUnaryContext ctx) {
      return visitChildren(ctx);
   }

   @Override public List<String> visitExprMulDivRem(GrammarParser.ExprMulDivRemContext ctx) {
      return visitChildren(ctx);
   }

   @Override public List<String> visitExprReal(GrammarParser.ExprRealContext ctx) {
      return visitChildren(ctx);
   }

   @Override public List<String> visitExprInteger(GrammarParser.ExprIntegerContext ctx) {
      return visitChildren(ctx);
   }

   @Override public List<String> visitExprID(GrammarParser.ExprIDContext ctx) {
      return visitChildren(ctx);
   }
}
