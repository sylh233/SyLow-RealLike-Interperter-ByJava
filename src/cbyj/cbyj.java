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
		// 参数: mode filename
		// mode: repl file load
		try{
			if(args.length <= 0){
				runPrompt();
			}else if(args.length == 1){
				if("repl".equals(args[0])){
					runPrompt();
				}
				else{
					runFile(args[0]);
					// System.out.println("Have no the Mode:" +
					// 				   args[0] + "!");
					// System.exit(0x40);
				}
			}else if(args.length == 2){
				switch (args[0]) {
				case "file":
					runFile(args[1]);
				case "load":
					runLoad(args[1]);
				default:
					System.out.println("Have no the Mode:" +
									   args[0] + "!");
					System.exit(0x40);
					break;
				}
			}else {
				System.out.println("Too More Arguments!");
				System.exit(0x40);
			}
		}catch(Interpreter.ExitException e){
			System.out.println("Exit!");
		}
	}

	private static void runFile(String file) throws IOException{
		byte[] bytes = Files.readAllBytes(Paths.get(file));
		
		System.out.println("Start:");
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
			System.out.println("Start:");
			Object value = run_noStruct(line);
			printValue(value);
			hadError = false;
		}
	}

	private static void printValue(Object value){
		System.out.println("\nValue: "
						   + Interpreter.stringify_withquote(value));
	}

	private static Object run(String bytes) throws IOException{
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
			report(token.cLine, "at end ", message);
		}else{
			report(token.cLine, "at '"+token.lexeme+"' ", message);
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

	private static void runLoad(String file) throws IOException{
		System.out.println("Start:");
		Object value = loadFile(file,false);
		if (hadError) {
			System.exit(0x41);
		}
		if (hadRuntimeError) {
			System.exit(0x46);
		}
		printValue(value);
		
		runPrompt();
	}

	public static Object loadFile(String file,
								  Boolean entry) throws IOException{
		byte[] bytes = Files.readAllBytes(Paths.get(file));
		String filesrc = new String(bytes,Charset.defaultCharset());
		if(entry != null && entry){
			return run(filesrc);
		}
		else{
			return run_noStruct(filesrc);
		}
	}
}
