package cbyj.parser;

import java.util.Map;
import java.util.HashMap;
import cbyj.lexer.*;

public class Environment {
	private final Environment enclosing;
	private final Map<String,Object> environment = new HashMap<>();

	public Environment(){
		enclosing = null;
	}

	public Environment(Environment enclosing){
		this.enclosing = enclosing;
	}

	public Object get(Token name){
		if(environment.containsKey(name.lexeme)){
			return environment.get(name.lexeme);
		}

		if(enclosing != null) return enclosing.get(name);
		
		throw new RuntimeError(name, "Undefined Variable "+name.lexeme+"!");
	}
	
	public void define(String name,Object value){
		environment.put(name,value);
	}

	public void assign(Token name,Object value){
		if (environment.containsKey(name.lexeme)) {
			environment.put(name.lexeme,value);
			return;
		}

		if (enclosing != null){
			enclosing.assign(name, value);
			return;
		}
		
	    throw new RuntimeError(name, "Undefined Variable "+name.lexeme+"!");
	}

	public void assign(Token name,Object index,Object value){
		if (environment.containsKey(name.lexeme)) {
			Object list = environment.get(name.lexeme);
			if(!(list instanceof rl_Array)){
				throw new RuntimeError(name,"The Variable isn't a Array!");
			}
			rl_Array array = (rl_Array)list;
			array.assign(index,value);
			environment.put(name.lexeme,array);
			return;
		}

		if (enclosing != null){
			enclosing.assign(name, index, value);
			return;
		}
		
	    throw new RuntimeError(name, "Undefined List "+name.lexeme+"!");
	}
}
