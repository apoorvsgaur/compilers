import java.io.*;
import java.util.List;
import java.util.Stack;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
public class PrintingListener extends MicroBaseListener {

	SymbolTable global;
	Stack<SymbolTable> pointerStack = new Stack<>();
	String typePointer;
	Integer blockCount = 0;
	Integer skipCount = 0;

	@Override
	public void enterProgram(MicroParser.ProgramContext ctx) {
		global = new SymbolTable("GLOBAL");
		pointerStack.push(global);
	}

	@Override
	public void exitProgram(MicroParser.ProgramContext ctx){
		global.print();
	}

	//CODE FOR ADDING TABLES
	@Override
	public void enterFunc_decl(MicroParser.Func_declContext ctx) {
		SymbolTable table = new SymbolTable(ctx.id().getText());
		pointerStack.peek().addTable(table);
		pointerStack.push(table);
	}
	@Override
	public void exitFunc_decl(MicroParser.Func_declContext ctx) {
		pointerStack.pop();
	}

	@Override
	public void enterCond(MicroParser.CondContext ctx) {
		if (skipCount == 0) {
			blockCount++;
			SymbolTable table = new SymbolTable("BLOCK " + blockCount);
			pointerStack.peek().addTable(table);
			pointerStack.push(table);
		}
	}
	@Override
	public void exitCond(MicroParser.CondContext ctx) {
		if (skipCount > 0){
			skipCount--;
		} else {
			pointerStack.pop();
		}
	}

	@Override public void enterDo_while_stmt(MicroParser.Do_while_stmtContext ctx) {
		blockCount++;
		SymbolTable table = new SymbolTable("BLOCK "+ blockCount);
		pointerStack.peek().addTable(table);
		pointerStack.push(table);
		skipCount++;
	}
	@Override public void exitDo_while_stmt(MicroParser.Do_while_stmtContext ctx) {
		pointerStack.pop();
	}


	//CODE FOR ADDING SYMBOLS
	@Override
	public void enterString_decl(MicroParser.String_declContext ctx) {
		pointerStack.peek().addSymbol(ctx.id().IDENTIFIER().toString(), "STRING", ctx.str().STRINGLITERAL().toString());
	}

	@Override
	public void enterParam_decl(MicroParser.Param_declContext ctx) {
		pointerStack.peek().addSymbol(ctx.id().IDENTIFIER().toString(), ctx.var_type().getText(), null);
	}

	@Override
	public void enterVar_type(MicroParser.Var_typeContext ctx) {
		typePointer = ctx.getText();
	}

	@Override
	public void enterVar_id(MicroParser.Var_idContext ctx) {
		pointerStack.peek().addSymbol(ctx.IDENTIFIER().toString(),typePointer,null);
	}
}

