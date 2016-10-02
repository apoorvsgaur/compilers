import java.io.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class PrintingListener extends MicroBaseListener {
    	public void enterProgram(MicroParser.ProgramContext ctx) {
        	//System.out.println(ctx.getText());
    	}
	public void visitTerminal(TerminalNode node) {
		System.out.println(node.getClass().toString());
        	System.out.println(node.getText());
    	}
}
