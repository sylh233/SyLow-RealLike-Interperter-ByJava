package cbyj.func;

import java.util.List;
import cbyj.parser.*;
import cbyj.lexer.*;

public class Function implements Callable{
	private final Expr.FunctionDecl decl;

	public Function(Expr.FunctionDecl decl){
		this.decl = decl;
	}
	
	@Override
	public int arity(){
		return decl.parameters.size();
	}

	@Override
	public Object call(Interpreter interpreter,
					   List<Object> arguments){
		Environment previous = interpreter.environment;

		Object result = null;
		try{
			interpreter.environment = new Environment(previous);
			for(int i = 0;i < arity();i++){
				interpreter.environment.define(decl.parameters.get(i).lexeme,
											   arguments.get(i));
			}
			result = interpreter.eval(decl.body);
		}finally{
			interpreter.environment = previous;
		}
		
		return result;
	}

	@Override
	public String toString(){
		return "<func>";
	}
}
