package cbyj.parser;

import cbyj.lexer.*;

public class RuntimeError extends RuntimeException{
	public final Token token;
	public RuntimeError(Token token,String msg){
		super(msg);
		this.token = token;
	}
	public RuntimeError(String msg){
		super(msg);
		this.token = null;
	}
}
