import java.io.*;
import java.util.Arrays;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;
public class Micro {
	public static void main(String[] args) throws Exception{
		ANTLRFileStream file = new ANTLRFileStream(args[0]);

		Lexer lexer = new MicroLexer((CharStream) file);
		lexer.removeErrorListeners();
		lexer.addErrorListener(ThrowingErrorListener.INSTANCE);

		CommonTokenStream tokens = new CommonTokenStream(lexer);

		MicroParser parser = new MicroParser(tokens);
		parser.removeErrorListeners();
		parser.addErrorListener(ThrowingErrorListener.INSTANCE);
        MicroParser.ProgramContext program = parser.program();
		ParseTreeWalker walker = new ParseTreeWalker();
		PrintingListener listener = new PrintingListener();
        try {
            walker.walk(listener, program);
        } catch (RuntimeException e){
            System.out.println(e.getMessage());
        }
	}
}
