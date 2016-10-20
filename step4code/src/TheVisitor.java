import java.lang.Object;
import java.util.ArrayList;
import java.util.Stack;

public class TheVisitor extends MicroBaseVisitor {

    SymbolWatcher global;
    Stack<SymbolTable> pointerStack = new Stack<>();
    String typePointer;
    Integer blockCount = 0;
    NodeConverter nodeConverter;

    public String checkType(String value){
        try{
            int integer = Integer.parseInt(value);
            return "INT";
        }
        catch(NumberFormatException e){
        }
        try{
            float floatnum = Float.parseFloat(value);
            return "FLOAT";
        }
        catch(NumberFormatException e){
        }
        throw new RuntimeException("Incorrect integer");
    }


    @Override
    public Object visitProgram(MicroParser.ProgramContext ctx) {
        pointerStack.push(global.getGlobal());

        visitChildren(ctx);

        global.print();
        return null;
    }

    @Override
    public Object visitFunc_decl(MicroParser.Func_declContext ctx) {
        SymbolTable table = new SymbolTable(ctx.id().getText());
        pointerStack.peek().addTable(table);
        pointerStack.push(table);

        super.visitFunc_decl(ctx);

        pointerStack.pop();
        return null;
    }

    @Override
    public Object visitCond(MicroParser.CondContext ctx) {
        blockCount++;
        SymbolTable table = new SymbolTable("BLOCK " + blockCount);
        pointerStack.peek().addTable(table);
        pointerStack.push(table);

        super.visitCond(ctx);

        pointerStack.pop();
        return null;
    }

    @Override
    public Object visitDo_while_stmt(MicroParser.Do_while_stmtContext ctx) {
        blockCount++;
        SymbolTable table = new SymbolTable("BLOCK "+ blockCount);
        pointerStack.peek().addTable(table);
        pointerStack.push(table);

        super.visitDo_while_stmt(ctx);

        pointerStack.pop();
        return null;
    }

    @Override
    public Object visitString_decl(MicroParser.String_declContext ctx) {
        pointerStack.peek().addSymbol(ctx.id().IDENTIFIER().toString(), "STRING", ctx.str().STRINGLITERAL().toString());
        return super.visitString_decl(ctx);
    }

    @Override
    public Object visitParam_decl(MicroParser.Param_declContext ctx) {
        pointerStack.peek().addSymbol(ctx.id().IDENTIFIER().toString(), ctx.var_type().getText(), null);
        return super.visitParam_decl(ctx);
    }

    @Override
    public Object visitVar_type(MicroParser.Var_typeContext ctx) {
        typePointer = ctx.getText();
        return super.visitVar_type(ctx);
    }

    @Override
    public Object visitVar_id(MicroParser.Var_idContext ctx) {
        pointerStack.peek().addSymbol(ctx.IDENTIFIER().toString(),typePointer,null);
        return super.visitVar_id(ctx);
    }

    @Override
    public Object visitAssign_expr(MicroParser.Assign_exprContext ctx) {
        return super.visitAssign_expr(ctx);
    }

    @Override
    public Object visitRead_stmt(MicroParser.Read_stmtContext ctx) {
        super.visitRead_stmt(ctx);
        if(ctx.getChild(2) == null || ctx.getChild(2).getText().equals("")){
            return null;
        }
        String[] ids = ctx.id_list().getText().split(",");
        for( String s : ids ){
            nodeConverter.addNode(new Node("READ",null,null,s));
        }
        return null;
    }

    @Override
    public Object visitWrite_stmt(MicroParser.Write_stmtContext ctx) {
        super.visitWrite_stmt(ctx);
        if(ctx.getChild(2) == null || ctx.getChild(2).getText().equals("")){
            return null;
        }
        String[] ids = ctx.id_list().getText().split(",");
        for( String s : ids ){
            nodeConverter.addNode(new Node("WRITE",null,null,s));
        }
        return null;
    }

    @Override
    public Object visitExpr(MicroParser.ExprContext ctx) {
        super.visitExpr(ctx);
        return null;
    }

    @Override
    public Object visitExpr_prefix(MicroParser.Expr_prefixContext ctx) {
        super.visitExpr_prefix(ctx);
        return null;
    }

    @Override
    public Object visitFactor(MicroParser.FactorContext ctx) {
        super.visitFactor(ctx);
        return null;
    }

    @Override
    public Object visitFactor_prefix(MicroParser.Factor_prefixContext ctx) {
        super.visitFactor_prefix(ctx);
        return null;
    }
}