import org.antlr.v4.runtime.tree.ParseTree;

import java.lang.Object;
import java.util.ArrayList;
import java.util.Stack;

public class TheVisitor extends MicroBaseVisitor {

    SymbolWatcher global = new SymbolWatcher();
    Stack<SymbolTable> pointerStack = new Stack<>();
    String typePointer;
    Integer blockCount = 0;
    NodeConverter nodeConverter = new NodeConverter(global);

    Integer tempCount = 0;

    String currentType;


    @Override
    public Object visitProgram(MicroParser.ProgramContext ctx) {
        pointerStack.push(global.getGlobal());

        visitChildren(ctx);

        //global.print();
        nodeConverter.print();
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
        global.addSymbol(ctx.id().IDENTIFIER().toString(),"STRING",ctx.str().STRINGLITERAL().toString());
        return super.visitString_decl(ctx);
    }

    @Override
    public Object visitParam_decl(MicroParser.Param_declContext ctx) {
        pointerStack.peek().addSymbol(ctx.id().IDENTIFIER().toString(), ctx.var_type().getText(), null);
        global.addSymbol(ctx.id().IDENTIFIER().toString(), ctx.var_type().getText(), null);
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
        global.addSymbol(ctx.IDENTIFIER().toString(),typePointer,null);
        return super.visitVar_id(ctx);
    }

    @Override
    public Object visitAssign_expr(MicroParser.Assign_exprContext ctx) {
        String id = ctx.id().getText();
        String type = global.checkType(id);
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
            String type = global.checkType(s);
            if(type.equals("INT")){
                nodeConverter.addNode(new Node("READI",null,null,s));
            } else if (type.equals("FLOAT")){
                nodeConverter.addNode(new Node("READF",null,null,s));
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
            String type = global.checkType(s);
            if(type.equals("INT")){
                nodeConverter.addNode(new Node("WRITEI",null,null,s));
            } else if (type.equals("FLOAT")){
                nodeConverter.addNode(new Node("WRITEF",null,null,s));
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
            System.out.println("Not supported FUNCTIONS");
            throw new RuntimeException();
        }
        return visitPrimary(ctx.primary());
    }

    @Override
    public NodePackage visitPrimary(MicroParser.PrimaryContext ctx) {
        String id;
        if(ctx.expr() == null){
            id = ctx.getText();
            return new NodePackage(id);
        } else {
            return visitExpr(ctx.expr());
        }
    }
}