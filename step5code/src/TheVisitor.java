import java.lang.Object;
import java.util.ArrayList;
import java.util.Stack;

public class TheVisitor extends MicroBaseVisitor {

    SymbolWatcher global = new SymbolWatcher();
    Stack<SymbolTable> pointerStack = new Stack<>();
    String typePointer; //Stores the variable type for a continous string of variables (comma seperated list)
    Integer blockCount = 0; //Used for step3 to name blocks
    NodeConverter nodeConverter = new NodeConverter(global);

    Integer tempCount = 0; //Building IR, need temporary registers

    String currentType; //Checking variable types, for operations

    @Override
    public Object visitBase_stmt(MicroParser.Base_stmtContext ctx) {
        return super.visitBase_stmt(ctx);
    }

    @Override
    public Object visitProgram(MicroParser.ProgramContext ctx) {
        System.out.println("visitProgram");
        //visitProgram always first
        pointerStack.push(global.getGlobal()); //Pushes an empty SymbolWatcher.global onto the stack

        visitChildren(ctx); //ANTLR Function - Visits the child of a given node from left to right, aka inorder traversal


        //global.print();
        nodeConverter.print(); //Finally prints both IR Code and TinyCode
        return null;
    }

    @Override
    public Object visitFunc_decl(MicroParser.Func_declContext ctx) {
        SymbolTable table = new SymbolTable(ctx.id().getText());
        pointerStack.peek().addTable(table);
        pointerStack.push(table);

        super.visitFunc_decl(ctx);

        System.out.println("visitFunc_decl");

        //System.out.println(ctx.getText() + '\n');

        pointerStack.pop();
        return null;
    }


    @Override
    public Object visitIf_stmt(MicroParser.If_stmtContext ctx){
        //if_stmt:
        //'IF' '(' cond ')' decl stmt_list else_part 'ENDIF';
        //else_part:
        //'ELSIF' '(' cond ')' decl stmt_list else_part |  ;
        visitCond(ctx.cond());

    }



    @Override
    public Boolean visitCond(MicroParser.CondContext ctx) {
        //cond:
        //expr compop expr | 'TRUE' | 'FALSE';

        blockCount++;
        SymbolTable table = new SymbolTable("BLOCK " + blockCount);
        pointerStack.peek().addTable(table);
        pointerStack.push(table);

        NodePackage expr1 = visitExpr(ctx.expr(0));
        NodePackage expr2 = visitExpr(ctx.expr(1));
        ConditionalPackage();


        System.out.println("visitCond");
        super.visitCond(ctx);

        pointerStack.pop();
        return null;
    }

    @Override
    public Object visitDo_while_stmt(MicroParser.Do_while_stmtContext ctx) {
        // do_while_stmt:
        // 'DO' decl stmt_list 'WHILE' '(' do_cond ')' ';' ;

        blockCount++;
        SymbolTable table = new SymbolTable("BLOCK "+ blockCount);
        pointerStack.peek().addTable(table);
        pointerStack.push(table);

        //System.out.println("visitDo_while_stmt");


        super.visitDo_while_stmt(ctx);

        pointerStack.pop();
        return null;
    }


    @Override
    public Object visitString_decl(MicroParser.String_declContext ctx) {
        pointerStack.peek().addSymbol(ctx.id().IDENTIFIER().toString(), "STRING", ctx.str().STRINGLITERAL().toString());
        global.addSymbol(ctx.id().IDENTIFIER().toString(),"STRING",ctx.str().STRINGLITERAL().toString());

        System.out.println("visitString_decl");

        return super.visitString_decl(ctx);
    }

    @Override
    public Object visitParam_decl(MicroParser.Param_declContext ctx) {
        pointerStack.peek().addSymbol(ctx.id().IDENTIFIER().toString(), ctx.var_type().getText(), null);
        global.addSymbol(ctx.id().IDENTIFIER().toString(), ctx.var_type().getText(), null);

        System.out.println("visitParam_decl");

        return super.visitParam_decl(ctx);
    }

    @Override
    public Object visitVar_type(MicroParser.Var_typeContext ctx) {
        typePointer = ctx.getText();
        System.out.println("visitVar_type: " + typePointer);

        return super.visitVar_type(ctx);
    }

    @Override
    public Object visitVar_id(MicroParser.Var_idContext ctx) {
        //pointerStack - Stack of symbol tables
        //global is the symbolWatcher. symbolWatcher contains a list of symbol tables, a variableMap

        pointerStack.peek().addSymbol(ctx.IDENTIFIER().toString(),typePointer,null); //Adds the variable to the symbolTable of the top of the stack
        global.addSymbol(ctx.IDENTIFIER().toString(),typePointer,null); //Adds the variable to the variablemap of the SymbolWatcher

        System.out.println("visitVar_id: " + ctx.IDENTIFIER().toString());

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
        System.out.println("visitAssign_expr");
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

        System.out.println("visitRead_stmt");

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

        System.out.println("visitWrite_stmt");

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

        System.out.println("visitExpr");

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

        System.out.println("visitExpr_prefix");
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


        System.out.println("visitFactor");

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

        System.out.println("visitFactor_prefix");

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

        System.out.println("visitPostfix_expr");

        return visitPrimary(ctx.primary());

    }

    @Override
    public NodePackage visitPrimary(MicroParser.PrimaryContext ctx) {
        String id;

        System.out.println("visitPrimary");

        if(ctx.expr() == null){
            id = ctx.getText();
            System.out.println("New NodePackage made with id: " + id);
            return new NodePackage(id);
        } else {
            return visitExpr(ctx.expr());
        }
    }
}
