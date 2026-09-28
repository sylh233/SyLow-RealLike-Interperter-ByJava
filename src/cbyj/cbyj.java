package cbyj;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import cbyj.lexer.*;
import cbyj.parser.*;

public class cbyj {
	static boolean hadError = false;
	static boolean hadRuntimeError = false;
	private static final Interpreter interpreter = new Interpreter();
	
	public static void main(String[] args) throws IOException{
	    if(args.length > 1){
			System.out.println("0/1 parameters!");
			System.exit(0x40);
		}else if (args.length == 1) {
			runFile(args[0]);
		} else {
			runPrompt();
		}
	}

	private static void runFile(String file) throws IOException{
		byte[] bytes = Files.readAllBytes(Paths.get(file));
		Object value = run(new String(bytes,Charset.defaultCharset()));
		if (hadError) {
			System.exit(0x41);
		}
		if (hadRuntimeError) {
			System.exit(0x46);
		}
		
		printValue(value);
	}

	private static void runPrompt() throws IOException{
		InputStreamReader input = new InputStreamReader(System.in);
		BufferedReader reader = new BufferedReader(input);
		while (true) {
			System.out.print(">>> ");
			String line = reader.readLine();
			if (line == null) {
				break;
			}
		    Object value = run_noStruct(line);
			printValue(value);
			hadError = false;
		}
	}

	private static void printValue(Object value){
		String valueStr = "";
		if(value instanceof String){
			valueStr = "\"" + (String)value + "\"";
			System.out.println("Value: " + valueStr);
		}else{
			System.out.println("Value: " + value);
		}
	}

	private static Object run(String bytes) throws IOException{
		System.out.println("Start:");
		
		Scanner scanner = new Scanner(bytes);
		List<Token> tokens = scanner.scanTokens();

		Parser parser = new Parser(tokens);
		Expr result  = parser.parse();
		
		// for (Token token : tokens) {
		// 	System.out.println(token);
		// }

		if(hadError) return null;

		// System.out.println("Ast: ");
		// System.out.println(new AstPrinter().print(expr));
		// System.out.print("Value: ");
		// interpreter.interpert(expr);
		return interpreter.interpert(result);
	}

	private static Object run_noStruct(String bytes) throws IOException{
		System.out.println("Start:");
		
		Scanner scanner = new Scanner(bytes);
		List<Token> tokens = scanner.scanTokens();

		Parser parser = new Parser(tokens);
		List<Expr> result  = parser.parse_noStruct();
		
		if(hadError) return null;
		
		return interpreter.interpert_noStruct(result);
	}

	public static void throwError(int cLine,String message){
		report(cLine, "", message);
	}

	public static void throwError(Token token,String message){
		if (token.type == TokenType.EOF) {
			report(token.cLine, "at end", message);
		}else{
			report(token.cLine, "at '"+token.lexeme+"'", message);
		}
	}
	
	private static void report(int cLine,String where,String msg){
		System.err.println("[lines " + cLine + "] "
						   + where + ": " + msg);
		hadError = true;
	}

	public static void runtimeError(RuntimeError error){
		System.err.println(error.getMessage() +
						   "\n[Line " + error.token.cLine + "]");
		hadRuntimeError = true;
	}
}
