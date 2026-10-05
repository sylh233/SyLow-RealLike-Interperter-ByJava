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
			closure_reput(interpreter);
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
			
			if(closure != null)
				closure_put(interpreter);
			
			result = interpreter.eval(decl.body);
		}finally{
			if(closure != null)
				closure_reput(interpreter);
			interpreter.environment = previous;
		}
		
		return result;
	}

	private void closure_put(Interpreter interpreter){
		// 将闭包的变量送进环境
		closure.forEach((name, value) -> {
				interpreter.environment.define(name, value);
			});
	}

	private void closure_reput(Interpreter interpreter){
		// 将环境封入闭包
		for (Token token: decl.closure) {
			if (interpreter.environment.contain(token.lexeme)){
				closure.put(token.lexeme,
							interpreter.environment.get(token));
			}else{
				closure.put(token.lexeme,null);
			}
		}
	}

	@Override
	public String toString(){
		return "<func arity:" + this.arity() + ">";
	}
}
