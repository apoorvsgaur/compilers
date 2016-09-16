import java.io.*;
import java.util.Arrays;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class Micro {
	public static void main(String[] args) throws Exception{
		ANTLRFileStream file = new ANTLRFileStream(args[0]);
		Lexer lexer = new MicroLexer((CharStream) file);
		//CommonTokenStream tokens = new CommonTokenStream(lexer);
		//MicroParser parser = new MicroParser(tokens);
		for (Token token = lexer.nextToken(); token.getType() != Token.EOF; token = lexer.nextToken()){
			String type = "INDENTIFIER";
			Integer[] keywords = {1,2,3,4,7,8,9,11,14,15,16,21,22,23,24,25,32,33,34};
			Integer[] operators = {5,6,10,12,13,17,18,19,20,26,27,28,29,30,31,35};
			Integer typeNum = token.getType();
			if ( Arrays.asList(keywords).contains(typeNum) ){
				type = "KEYWORD";
			} else if( Arrays.asList(operators).contains(typeNum) ){
				type = "OPERATOR";
			} else if( typeNum == 36){
				type = "IDENTIFIER";
			} else if( typeNum == 37){
				type = "INTLITERAL";
			} else if( typeNum == 38){
				type = "FLOATLITERAL";
			} else if( typeNum == 39){
				type = "STRINGLITERAL";
			} else {
				type = "meh";
				System.out.println(typeNum);
			}
			System.out.println("Token Type: " + type );
			System.out.println("Value: " + token.getText());
		}
		/*
		MicroParser.ProgramContext program = parser.program();
		ParseTreeWalker walker = new ParseTreeWalker();
    		PrintingListener listener = new PrintingListener();
    		walker.walk(listener, program);*/
	}
}
