import org.antlr.v4.runtime.tree.ParseTree;

import java.lang.Object;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;

public class TheVisitor extends MicroBaseVisitor {

    SymbolWatcher watcher = new SymbolWatcher();
    String typePointer; //Stores the variable type for a continous string of variables (comma seperated list)
    Integer blockCount = 0; //Used for step3 to name blocks
    NodeConverter nodeConverter = new NodeConverter(watcher);

    Integer tempCount = 1;
    Integer labelCount = 0;
    Integer ifCount = 0;
    Integer elseCount = 0;
    Integer count;

    String currentType; //Checking variable types, for operations
    String functionType;

    ArrayList<String> targetList = new ArrayList<>();

    @Override
    public Object visitBase_stmt(MicroParser.Base_stmtContext ctx) {
        return super.visitBase_stmt(ctx);
    }

    @Override
    public Object visitProgram(MicroParser.ProgramContext ctx) {

        visitChildren(ctx); //ANTLR Function - Visits the child of a given node from left to right, aka inorder traversal

        //global.print();
        //watcher.print();
        nodeConverter.print();
        return null;
    }

    @Override
    public Object visitFunc_decl(MicroParser.Func_declContext ctx) {
        functionType = ctx.any_type().getText();
        watcher.addScope(ctx.id().getText());
        tempCount = 1;

        nodeConverter.addNode(new Node("LABEL", null, null, ctx.id().getText()));
        watcher.functionList.add(ctx.id().getText());
        nodeConverter.addNode(new Node("LINK", null, null, null));
        super.visitFunc_decl(ctx);
        if(!nodeConverter.nodeList.get(nodeConverter.nodeList.size()-1).op.equals("RET")) {
            nodeConverter.addNode(new Node("RET", null, null, null));
        }
        nodeConverter.addNode(new Node(null, null, null, null));
        watcher.popScope();
        return null;
    }

    @Override
    public Object visitReturn_stmt(MicroParser.Return_stmtContext ctx) {

        NodePackage finalPack = visitExpr(ctx.expr());
        nodeConverter.addNodes(finalPack.getNodes());
        if (functionType.equals("INT")){
            nodeConverter.addNode(new Node("STOREI",finalPack.id,null,"$R"));
        }
        else if (functionType.equals("FLOAT")){
            nodeConverter.addNode(new Node("STOREF",finalPack.id,null,"$R"));
        } else {
            throw new RuntimeException("Invalid Type");
        }
        nodeConverter.addNode(new Node("RET", null, null, null));
        return null;
    }

    @Override
    public Object visitIf_stmt(MicroParser.If_stmtContext ctx) {

        ConditionalPackage condition = visitCond(ctx.cond());
        Integer count = ifCount;
        String tempLabel = "t_if"+count;
        ifCount++;
        System.out.println("In visitIf_stmt for ifCount" + count);
        nodeConverter.addNode(new Node(condition.op, condition.target1, condition.target2, tempLabel));

        String elseLabel =  "t_else" + elseCount;
        elseCount++;

        if(ctx.else_part().stmt_list() != null){
            nodeConverter.addNode(new Node("JUMP", null, null, elseLabel));
        } else  {
            nodeConverter.addNode(new Node("JUMP", null, null, "END"+count));
        }

        nodeConverter.addNode(new Node("LABEL", null, null, tempLabel));
        visitStmt_list(ctx.stmt_list());
        nodeConverter.addNode(new Node("JUMP", null, null, "END"+count));
        if(ctx.else_part().stmt_list() != null) //Else needs to be there
        {
            nodeConverter.addNode(new Node("LABEL", null, null, elseLabel));
            visitElse_part(ctx.else_part());
        }
        nodeConverter.addNode(new Node("LABEL", null, null, "END" + count));

        return null;
    }


    @Override
    public Object visitElse_part(MicroParser.Else_partContext ctx){

        ConditionalPackage condition = visitCond(ctx.cond());
        Integer count = ifCount;
        String tempLabel = "t_if"+count;
        ifCount++;
        System.out.println("In visitElse_stmt for ifCount" + count);
        nodeConverter.addNode(new Node(condition.op, condition.target1, condition.target2, tempLabel));

        String elseLabel =  "t_else" + elseCount;
        elseCount++;

        if(ctx.else_part().stmt_list() != null){
            nodeConverter.addNode(new Node("JUMP", null, null, elseLabel));
        } else  {
            nodeConverter.addNode(new Node("JUMP", null, null, "END"+count));
        }

        nodeConverter.addNode(new Node("LABEL", null, null, tempLabel));
        visitStmt_list(ctx.stmt_list());
        nodeConverter.addNode(new Node("JUMP", null, null, "END"+count));
        if(ctx.else_part().stmt_list() != null) //Else needs to be there
        {
            nodeConverter.addNode(new Node("LABEL", null, null, elseLabel));
            visitElse_part(ctx.else_part());
        } else {
            /* no-op */
        }
        nodeConverter.addNode(new Node("LABEL", null, null, "END"+count));
            return null;
    }

    @Override
    public ConditionalPackage visitCond(MicroParser.CondContext ctx) {
        blockCount++;
        watcher.addScope("BLOCK " + blockCount);
        if ( ctx.getText().equals("TRUE")){
            watcher.popScope();
            return new ConditionalPackage("EQ", "1", "1");
        } else if (ctx.getText().equals("FALSE")  ){
            watcher.popScope();
            return new ConditionalPackage("EQ", "1", "0");
        } else {
            NodePackage expr1 = visitExpr(ctx.expr(0));
            NodePackage expr2 = visitExpr(ctx.expr(1));
            if(expr1.getNodes().isEmpty()){
                String checker = expr1.id;
                if (watcher.checkTempVar(checker) != null){
                    expr1.id = watcher.checkTempVar(checker);
                }
            }
            if(expr2.getNodes().isEmpty()){
                String checker = expr2.id;
                if (watcher.checkTempVar(checker) != null){
                    expr2.id = watcher.checkTempVar(checker);
                }
            }
            nodeConverter.addNodes(expr1.getNodes());
            nodeConverter.addNodes(expr2.getNodes());
            String op;
            switch (ctx.compop().getText()) {
                case ">":
                    op = "GT";
                    break;
                case ">=":
                    op = "GE";
                    break;
                case "<":
                    op = "LT";
                    break;
                case "<=":
                    op = "LE";
                    break;
                case "!=":
                    op = "NE";
                    break;
                case "=":
                    op = "EQ";
                    break;
                default:
                    op = "ERROR";
            }

            watcher.popScope();
            return new ConditionalPackage(op, expr1.id, expr2.id);
        }
    }

    @Override
    public Object visitDo_while_stmt(MicroParser.Do_while_stmtContext ctx) {
        blockCount++;
        watcher.addScope("BLOCK " + blockCount);
        String tempLabel = "t_do_while"+labelCount;
        labelCount++;
        nodeConverter.addNode(new Node("LABEL", null, null, tempLabel));
        visitStmt_list(ctx.stmt_list());
        ConditionalPackage finalPack = visitDo_cond(ctx.do_cond());
        nodeConverter.addNode(new Node(finalPack.op, finalPack.target1, finalPack.target2, tempLabel));
        watcher.popScope();
        return null;
    }

    @Override
    public ConditionalPackage visitDo_cond(MicroParser.Do_condContext ctx) {
        if ( ctx.getText().equals("TRUE")){
            return new ConditionalPackage("EQ", "1", "1");
        } else if (ctx.getText().equals("FALSE")  ){
            return new ConditionalPackage("EQ", "1", "0");
        } else {
            NodePackage expr1 = visitExpr(ctx.expr(0));
            NodePackage expr2 = visitExpr(ctx.expr(1));
            nodeConverter.addNodes(expr1.getNodes());
            nodeConverter.addNodes(expr2.getNodes());
            String op;
            switch (ctx.compop().getText()) {
                case ">":
                    op = "GT";
                    break;
                case ">=":
                    op = "GE";
                    break;
                case "<":
                    op = "LT";
                    break;
                case "<=":
                    op = "LE";
                    break;
                case "!=":
                    op = "NE";
                    break;
                case "=":
                    op = "EQ";
                    break;
                default:
                    op = "ERROR";
            }
            return new ConditionalPackage(op, expr1.id, expr2.id);
        }
    }

    @Override
    public Object visitString_decl(MicroParser.String_declContext ctx) {
        watcher.addSymbol(ctx.id().IDENTIFIER().toString(), "STRING", ctx.str().STRINGLITERAL().toString());
        return super.visitString_decl(ctx);
    }

    @Override
    public Object visitParam_decl(MicroParser.Param_declContext ctx) {
        watcher.addParamSymbol(ctx.id().IDENTIFIER().toString(),ctx.var_type().getText(), null);
        return super.visitParam_decl(ctx);
    }

    @Override
    public Object visitVar_type(MicroParser.Var_typeContext ctx) {
        typePointer = ctx.getText();
        return super.visitVar_type(ctx);
    }

    @Override
    public Object visitVar_id(MicroParser.Var_idContext ctx) {
        watcher.addSymbol(ctx.IDENTIFIER().toString(),typePointer,null);
        return super.visitVar_id(ctx);
    }






    @Override
    public Object visitAssign_expr(MicroParser.Assign_exprContext ctx) {
        String id = watcher.checkTempVar(ctx.id().getText());
        if (id == null) id = ctx.id().getText();
        String type = watcher.checkType(ctx.id().getText());
        currentType = type;
        NodePackage finalPack = visitExpr(ctx.expr());
        Node assignNode;
        if (type.equals("INT")){
            assignNode = new Node("STOREI",finalPack.id,null,id);
        }
        else if (type.equals("FLOAT")){
            assignNode = new Node("STOREF",finalPack.id,null,id);
        } else {
            throw new RuntimeException("Invalid Type");
        }
        ArrayList<Node> nodeList = finalPack.getNodes();
        nodeList.add(assignNode);
        nodeConverter.addNodes(nodeList);
        return null;
    }

    @Override
    public Object visitRead_stmt(MicroParser.Read_stmtContext ctx) {
        super.visitRead_stmt(ctx);
        if(ctx.getChild(2) == null || ctx.getChild(2).getText().equals("")){
            return null;
        }
        String[] ids = ctx.id_list().getText().split(",");
        for( String s : ids ){
            String id = watcher.checkTempVar(s);
            if (id == null) id = s;
            String type = watcher.checkType(s);
            if(type.equals("INT")){
                nodeConverter.addNode(new Node("READI",null,null,id));
            } else if (type.equals("FLOAT")){
                nodeConverter.addNode(new Node("READF",null,null,id));
            } else {
                throw new RuntimeException();
            }
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
            String id = watcher.checkTempVar(s);
            if (id == null) id = s;
            String type = watcher.checkType(s);
            if(type.equals("INT")){
                nodeConverter.addNode(new Node("WRITEI",null,null, id));
            } else if (type.equals("FLOAT")){
                nodeConverter.addNode(new Node("WRITEF",null,null, id));
            } else if (type.equals("STRING")) {
                nodeConverter.addNode(new Node("WRITES", null, null, s));
            } else {
                throw new RuntimeException();
            }
        }
        return null;
    }

    @Override
    public NodePackage visitExpr(MicroParser.ExprContext ctx) {
        if(ctx.expr_prefix()==null && ctx.factor()==null){
            return new NodePackage();
        }
        NodePackage exprPack = visitExpr_prefix(ctx.expr_prefix());
        NodePackage factorPack = visitFactor(ctx.factor());
        ArrayList<Node> nodeList = new ArrayList<>();
        nodeList.addAll(exprPack.getNodes());
        nodeList.addAll(factorPack.getNodes());
        NodePackage finalPack;

        String id;
        if(exprPack.type == NodePackage.NodeType.EMPTY){
            //Make a ID node package
            id = factorPack.id;
            finalPack = new NodePackage(id,nodeList);

        } else if (exprPack.type == NodePackage.NodeType.ID_OP) {
            //Make a pack and generate node
            id = "$T"+tempCount.toString();
            tempCount++;
            Node newNode;
            if(currentType.equals("INT")){
                newNode = new Node("I"+exprPack.op, exprPack.id, factorPack.id, id);
            } else if (currentType.equals("FLOAT")){
                newNode = new Node("F"+exprPack.op, exprPack.id, factorPack.id, id);
            } else {
                throw new RuntimeException("Invalid Type");
            }
            nodeList.add(newNode);
            finalPack = new NodePackage(id,nodeList);
        } else {
            finalPack = new NodePackage();
            System.out.println("Error pack type: "+exprPack.type.toString());
        }

        return finalPack;
    }

    @Override
    public NodePackage visitExpr_prefix(MicroParser.Expr_prefixContext ctx) {
        if(ctx.expr_prefix()==null && ctx.factor()==null){
            return new NodePackage();
        }
        NodePackage exprPack = visitExpr_prefix(ctx.expr_prefix());
        NodePackage factorPack = visitFactor(ctx.factor());
        ArrayList<Node> nodeList = new ArrayList<>();
        nodeList.addAll(exprPack.getNodes());
        nodeList.addAll(factorPack.getNodes());
        NodePackage finalPack;

        String id;
        String op;
        if(exprPack.type == NodePackage.NodeType.EMPTY){
            //Make a ID_OP node package
            id = factorPack.id;
            op = ctx.addop().getText();
            finalPack = new NodePackage(id,op,nodeList);

        } else if (exprPack.type == NodePackage.NodeType.ID_OP) {
            //Make a pack and generate node
            id = "$T"+tempCount.toString();
            tempCount++;
            op = ctx.addop().getText();
            Node newNode;
            if(currentType.equals("INT")){
                newNode = new Node("I"+exprPack.op, exprPack.id, factorPack.id, id);
            } else if (currentType.equals("FLOAT")){
                newNode = new Node("F"+exprPack.op, exprPack.id, factorPack.id, id);
            } else {
                throw new RuntimeException("Invalid Type");
            }
            nodeList.add(newNode);
            finalPack = new NodePackage(id,op,nodeList);
        } else {
            finalPack = new NodePackage();
            System.out.println("Error pack type: "+exprPack.type.toString());
        }

        return finalPack;
    }

    @Override
    public NodePackage visitFactor(MicroParser.FactorContext ctx) {
        if(ctx.factor_prefix()==null && ctx.postfix_expr()==null){
            return new NodePackage();
        }
        NodePackage postPack = visitPostfix_expr(ctx.postfix_expr());
        NodePackage factorPack = visitFactor_prefix(ctx.factor_prefix());
        ArrayList<Node> nodeList = new ArrayList<>();
        nodeList.addAll(factorPack.getNodes());
        nodeList.addAll(postPack.getNodes());
        NodePackage finalPack;

        String id;
        if(factorPack.type == NodePackage.NodeType.EMPTY){
            //Make a ID node package
            id = postPack.id;
            finalPack = new NodePackage(id,nodeList);

        } else if (factorPack.type == NodePackage.NodeType.ID_OP) {
            //Make a pack and generate node
            id = "$T"+tempCount.toString();
            tempCount++;
            Node newNode;
            if(currentType.equals("INT")){
                newNode = new Node("I"+factorPack.op, factorPack.id, postPack.id, id);
            } else if (currentType.equals("FLOAT")){
                newNode = new Node("F"+factorPack.op, factorPack.id, postPack.id, id);
            } else {
                throw new RuntimeException("Invalid Type");
            }
            nodeList.add(newNode);
            finalPack = new NodePackage(id,nodeList);
        } else {
            finalPack = new NodePackage();
            System.out.println("Error pack type: "+factorPack.type.toString());
        }
        return finalPack;
    }

    @Override
    public NodePackage visitFactor_prefix(MicroParser.Factor_prefixContext ctx) {
        if(ctx.factor_prefix()==null && ctx.postfix_expr()==null){
            return new NodePackage();
        }
        NodePackage postPack = visitPostfix_expr(ctx.postfix_expr());
        NodePackage factorPack = visitFactor_prefix(ctx.factor_prefix());
        ArrayList<Node> nodeList = new ArrayList<>();
        nodeList.addAll(postPack.getNodes());
        nodeList.addAll(factorPack.getNodes());
        NodePackage finalPack;

        String id;
        String op;
        if(factorPack.type == NodePackage.NodeType.EMPTY){
            //Make a ID_OP node package
            id = postPack.id;
            op = ctx.mulop().getText();
            finalPack = new NodePackage(id,op,nodeList);

        } else if (factorPack.type == NodePackage.NodeType.ID_OP) {
            //Make a pack and generate node
            id = "$T"+tempCount.toString();
            tempCount++;
            op = ctx.mulop().getText();
            Node newNode;
            if(currentType.equals("INT")){
                newNode = new Node("I"+factorPack.op, factorPack.id, postPack.id, id);
            } else if (currentType.equals("FLOAT")){
                newNode = new Node("F"+factorPack.op, factorPack.id, postPack.id, id);
            } else {
                throw new RuntimeException("Invalid Type");
            }
            nodeList.add(newNode);
            finalPack = new NodePackage(id,op,nodeList);
        } else {
            finalPack = new NodePackage();
            System.out.println("Error pack type: "+factorPack.type.toString());
        }

        return finalPack;
    }

    @Override
    public NodePackage visitPostfix_expr(MicroParser.Postfix_exprContext ctx) {
        if(ctx.primary() == null){
            //System.out.println("Not supported FUNCTIONS");
            //throw new RuntimeException();
            return visitCall_expr(ctx.call_expr());
        } else {
            return visitPrimary(ctx.primary());
        }
    }

    @Override
    public NodePackage visitCall_expr(MicroParser.Call_exprContext ctx) {
        visitExpr_list(ctx.expr_list());
        nodeConverter.addNode(new Node("JSR", null, null, ctx.id().getText()));
        String id = "$T"+tempCount.toString();
        tempCount++;
        for ( String target : targetList){
            nodeConverter.addNode(new Node("POP", null, null, null));
        }
        nodeConverter.addNode(new Node("POP", null, null, id));
        return new NodePackage(id);
    }

    @Override
    public Object visitExpr_list(MicroParser.Expr_listContext ctx) {
        targetList.clear();
        visitExpr_list_tail(ctx.expr_list_tail());
        NodePackage pack = visitExpr(ctx.expr());
        nodeConverter.addNodes(pack.getNodes());
        targetList.add(pack.id);
        //PUSH FOR RESULT SPACE
        nodeConverter.addNode(new Node("PUSH", null, null, null));
        Collections.reverse(targetList);
        for ( String target : targetList){
            if ( watcher.checkTempVar(target) != null) {
                nodeConverter.addNode(new Node("PUSH", null, null, watcher.checkTempVar(target)));
            } else {
                nodeConverter.addNode(new Node("PUSH", null, null, target));
            }
        }
        return null;
    }

    @Override
    public Object visitExpr_list_tail(MicroParser.Expr_list_tailContext ctx) {
        if(ctx.expr_list_tail() == null){
            return null;
        } else {
            visitExpr_list_tail(ctx.expr_list_tail());
            NodePackage pack = visitExpr(ctx.expr());
            nodeConverter.addNodes(pack.getNodes());
            targetList.add(pack.id);
        }
        return null;
    }

    @Override
    public NodePackage visitPrimary(MicroParser.PrimaryContext ctx) {
        String id;
        if(ctx.expr() == null){
            id = watcher.checkTempVar(ctx.getText());
            if (id == null) id = ctx.getText();
            return new NodePackage(id);
        } else {
            return visitExpr(ctx.expr());
        }
    }
}
