package cbyj.func;

import java.util.List;
import cbyj.parser.*;
import cbyj.lexer.*;
import java.util.Map;
import java.util.HashMap;

public class Function implements Callable{
	private final Expr.FunctionDecl decl;
	private final Map<String,Object> closure;

	public Function(Expr.FunctionDecl decl,
					Interpreter interpreter){
		this.decl = decl;
		if(decl.closure != null){
			closure = new HashMap<>();
			for (Token token: decl.closure) {
				closure.put(token.lexeme,
							interpreter.environment.get(token));
			}
		}else
			closure = null;
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
			closure.forEach((name, value) -> {
					interpreter.environment.define(name, value);
				});
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
