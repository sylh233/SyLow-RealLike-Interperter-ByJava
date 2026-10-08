package cbyj.parser;

import cbyj.lexer.*;

public class rl_Refer {
	final Token name;

	public rl_Refer(Token name){
		this.name = name;
	}

	public Object get(Interpreter interpreter){
		return interpreter.environment.get(name);
	}

	public Object assign(Interpreter interpreter,Object value){
		interpreter.environment.assign(name, value);
		return value;
	}

	public Object assign(Interpreter interpreter,Object index,Object value){
		interpreter.environment.assign(name, index, value);
		return value;
	}

	@Override
	public String toString(){
		return "<refer:" + name.lexeme + ">";
	}
}
