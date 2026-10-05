package cbyj.parser;

import static cbyj.lexer.TokenType.*;
import cbyj.cbyj;
import cbyj.lexer.*;
import static cbyj.parser.Stmt.Write;
import cbyj.func.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.HashMap;

public class Interpreter implements Expr.Visitor<Object>,
									Stmt.Visitor<Object>{
	final public Environment globals = new Environment();
	public Environment environment = globals;

	public Interpreter(){
		native_func();
	}

	private void native_func(){
		globals.define("clock", new Callable() {
				@Override
				public int arity() {return 0;}

				@Override
				public Object call(Interpreter interpreter,
								   List<Object> arguments){
					long ts = System.currentTimeMillis();
					DateTimeFormatter formatter = DateTimeFormatter
						.ofPattern("HH:mm:ss yyyy/MM/dd")
						.withZone(ZoneId.systemDefault());
					String time = formatter
						.format(Instant.ofEpochMilli(ts));
						return time;
				}

				@Override
				public String toString(){
					return "<native func:clock>";
				}
			});
	}
	
	// public void interpert(List<Stmt> statements){
	// 	try {
	// 		for (Stmt stmt : statements) {
	// 			exec(stmt);
	// 		}
	// 	    // Object value = eval(expr);
	// 		// System.out.println(stringify(value));
	// 	} catch (RuntimeError error) {
	// 		cbyj.runtimeError(error);
	// 	}
	// }
	public Object interpert(Expr result){
		try {
			return eval(result);
		} catch (RuntimeError error) {
			cbyj.runtimeError(error);
			return null;
		}
	}

	public Object interpert_noStruct(List<Expr> exprs){
		Object result = null;
		try {
			try {
				for (Expr expr : exprs) {
					result = eval(expr);
				}
			} catch (ReturnException exception) {
			    result = exception.value;
			}
		} catch (RuntimeError error) {
			cbyj.runtimeError(error);
		}
		return result;
	}
	
	static public String stringify(Object object){
		if (object == null) return "NULL";
		if (object instanceof Double || object instanceof Integer){
			return object.toString();
		}
		return object.toString();
	}
	
	static public String stringify_withquote(Object object){
		if (object instanceof String) {
			return "\"" + object + "\"";
		}else return stringify(object);
	}
	
	public Object eval(Expr expr){
		if(expr != null) return expr.accept(this);
		return null;
	}
	private Object exec(Stmt stmt){
		if(stmt != null)
			return stmt.accept(this);
		else
			return null;
	}
	@Override
	public Object visitBinary(Expr.Binary binary){
		Object left = binary.left.accept(this);
		Object right = binary.right.accept(this);

		switch (binary.operator.type) {
		// case COMMA:
		// 	return right;
		case PLUS:
			if((left instanceof Number || left instanceof Character)
			   && (right instanceof Number || right instanceof Character)){
				left = ifChar2int(left);
				right = ifChar2int(right);
				if (left instanceof Integer && right instanceof Integer) {
					return (Integer)left + (Integer)right;
				} else if (left instanceof Double || right instanceof Double){
					return ((Number)left).doubleValue() +
						((Number)right).doubleValue();
				}
			}else{
				return Interpreter.stringify(left) +
				Interpreter.stringify(right);
			}
			// if (left instanceof Integer && right instanceof Integer) {
			// 	return (Integer)left + (Integer)right;
			// } else if (left instanceof Double || right instanceof Double){
			// 	return ((Number)left).doubleValue() +
			// 		((Number)right).doubleValue();
			// }else if (left instanceof String && right instanceof String){
			// 	return (String)left + (String)right;
			// }
		case MINUS:
			isValidOperand(binary.operator, left, right);
			left = ifChar2int(left);
			right = ifChar2int(right);
			if (left instanceof Integer && right instanceof Integer) {
				return (Integer)left - (Integer)right;
			} else if (left instanceof Double || right instanceof Double){
				return ((Number)left).doubleValue() -
					((Number)right).doubleValue();
			}
			break;
		case STAR:
			isValidOperand(binary.operator, left, right);
			left = ifChar2int(left);
			right = ifChar2int(right);
			if (left instanceof Integer && right instanceof Integer) {
				return (Integer)left * (Integer)right;
			} else if (left instanceof Double || right instanceof Double){
				return ((Number)left).doubleValue() *
					((Number)right).doubleValue();
			}
			break;
		case SLASH:
			isValidOperand(binary.operator, left, right);
			left = ifChar2int(left);
			right = ifChar2int(right);
			if (left instanceof Integer && right instanceof Integer) {
				if((Integer)right == 0 && (Integer)left == 0) return Double.NaN;
				if((Integer)right == 0) return Double.POSITIVE_INFINITY;
				return (Integer)left / (Integer)right;
			} else if (left instanceof Double || right instanceof Double){
				return ((Number)left).doubleValue() /
					((Number)right).doubleValue();
			}
			break;
		case GREATER:
			isValidOperand(binary.operator, left, right);
			left = ifChar2int(left);
			right = ifChar2int(right);
			return ((Number)left).doubleValue() > ((Number)right).doubleValue();
		case LESS:
			isValidOperand(binary.operator, left, right);
			left = ifChar2int(left);
			right = ifChar2int(right);
		    return ((Number)left).doubleValue() < ((Number)right).doubleValue();
		case GEQUAL:
			isValidOperand(binary.operator, left, right);
			left = ifChar2int(left);
			right = ifChar2int(right);
			return ((Number)left).doubleValue() >= ((Number)right).doubleValue();
		case RETURN:
			isValidOperand(binary.operator, left, right);
			left = ifChar2int(left);
			right = ifChar2int(right);
		    return ((Number)left).doubleValue() <= ((Number)right).doubleValue();
		case DEQUAL:
			return isEqual(left, right);
		case NEQUAL:
			return !isEqual(left, right);
		default:
			break;
		}
		return null;
	}
	@Override
	public Object visitGrouping(Expr.Grouping grouping){
		Object value = null;
		Environment previous = this.environment;
		try {
			this.environment = new Environment(previous);
			for (Expr expr : grouping.exprs) {
				value = eval(expr);
			}
		} catch (ReturnException e) {
		    value = e.value;
			// System.out.println("<= " + value);
		} finally {
			this.environment = previous;
		}
		return value;
	}
	@Override
	public Object visitLiteral(Expr.Literal literal){
		return literal.value;
	}
	@Override
	public Object visitUnary(Expr.Unary unary){
		Object value = unary.expr.accept(this);
		switch(unary.operator.type) {
		case MINUS:
			isValidOperand(unary.operator, value);
			value = ifChar2int(value);
			if (value instanceof Integer) {
				return -(Integer)value;
			}else if (value instanceof Double){
				return -(Double)value;
			}
			break;
		case BANG:
			return !isTruthy(value);
		default:
			break;
		}
		return null;
	}
	private boolean isTruthy(Object object){
		if (object == null) return false;
		if (object instanceof Boolean){
			return (boolean)object;
		}
		if (object instanceof Integer){
			if ((Integer)object == 0){
				return false;
			}
		}else if (object instanceof Double){
			if ((Double)object == 0.0){
				return false;
			}
		}else if (object instanceof String){
			if (((String)object).length() == 0){
				return false;
			}
		}
		return true;
	}
	private boolean isEqual(Object a,Object b){
		if (a == null && b == null) return true;
		if (a == null) return false;
		return a.equals(b);
	}
	private void isValidOperand(Token operator,Object operand){
		if (operand instanceof Double || operand instanceof Integer
			|| operand instanceof Character) return;
		throw new RuntimeError(operator, "Operand must be a Number!");
	}
	private void isValidOperand(Token operator,Object left,Object right){
		if ((left instanceof Double || left instanceof Integer
			 || left instanceof Character)&&
			(right instanceof Double || right instanceof Integer
			 || right instanceof Character)) return;
		throw new RuntimeError(operator, "Operand must be a Number!");
	}
	private Object ifChar2int(Object object){
		if(object instanceof Character) return (int)(char)object;
		return object;
	}
	@Override
	public Object visitVariable(Expr.Variable variable){
		// return variable.name.literal;
		return environment.get(variable.name);
	}
	@Override
	public Object visitAssign(Expr.Assign assign){
		// return variable.name.literal;
		Object value = eval(assign.value);
		if(assign.index == null){
			environment.assign(assign.name, value);
		}else{
			Object index = eval(assign.index);
			environment.assign(assign.name, index, value);
		}
		return value;
	}
	@Override
	public Object visitLogical(Expr.Logical logical) {
		Object left = eval(logical.left);
		if(logical.operator.type == OR){
			if(isTruthy(left)) return isTruthy(left);
		}else{
		    if(!isTruthy(left)) return isTruthy(left);
		}
		return isTruthy(eval(logical.right));
	}
	@Override
	public Object visitStatement(Expr.Statement statement) {
		return exec(statement.stmt);
	}
	@Override
	public rl_Array visitArray(Expr.ArrayExpr arrayExpr){
		Object[] array = Arrays.stream(arrayExpr.array)
			.map(x -> eval(x)).toArray();
		Map<Object,Integer> index_map = new HashMap<>();
		arrayExpr.index_map.forEach((k,v) -> {
				index_map.put(eval(k), v);
			});
		return new rl_Array(array, index_map);
	}
	// @Override
	// public Object visitListItem(Expr.ListItem listItem){
	// 	int index = (Integer)eval(listItem.index);
	// 	if(listItem.expr instanceof Expr.Variable){
	// 		Expr.Variable variable = (Expr.Variable)listItem.expr;
	// 		Object[] list = (Object[])environment.get(variable.name);
	// 		return list[index];
	// 	}else if(listItem.expr instanceof Expr.Assign){
	// 		Expr.Assign assign = (Expr.Assign)listItem.expr;
	// 		Object value = eval(assign.value);
	// 		environment.assign(assign.name,
	// 						   index,
	// 						   value);
	// 		return value;
	// 	}else if(listItem.expr instanceof Expr.Literal){
	// 		Object value = eval(listItem.expr);
	// 		String s = stringify(value);
	// 		return s.charAt(index);
	// 	}
	// 	return null;
	// }
	@Override
	public Object visitSelect(Expr.Select select) {
		Object condition = eval(select.condition);
		Integer index = null;
		if(condition instanceof Boolean){
			Boolean bool = (Boolean)condition;
		    if(bool) index = 0;
			else index = 1;
		}else if(condition instanceof Number){
			index = ((Number)condition).intValue();
			// if(index < 0) throw new RuntimeError(select.ques,
			// "Index must be large than 0!");
		}else if(condition instanceof Character){
			index = (int)(char)condition;
		}
		// else{
		// 	throw new RuntimeError(select.ques,
		// 	"Expect a Boolean or a Integer as a index after Quote or Sharp!");
		// }
		
	    if(select.list instanceof Expr.ArrayExpr){
			Expr.ArrayExpr list = (Expr.ArrayExpr)select.list;
			Map<Object,Integer> index_map = new HashMap<>();
			list.index_map.forEach((k,v) -> {
					index_map.put(eval(k), v);
				});
			if (index_map.containsKey(condition)){
				Integer i  = index_map.get(condition);
				if(i < list.array.length)
					return eval(list.array[i]);
			}else if(index != null){
				// index = Math.floorMod(index, list.array.length);
				if(index < 0){
					index += list.array.length;
				}
				if(index < list.array.length){
					Object result = eval(list.array[index]);
					return result;
				}
				// else{
				// 	throw new RuntimeError(select.ques,"Array out of bounds("
				// 						   + list.array.length + ")!");
				// }
			}
			// else if(bool != null){			    
			// 	if(list.array.length > 0 && bool){
			// 		return eval(list.array[0]);
			// 	}else if(list.array.length > 1 && !bool){
			// 		return eval(list.array[1]);
			// 	}
			// }else{
				
			// }
		}else if(select.list instanceof Expr.Literal){
			String str = stringify(eval(select.list));
			if(index != null){
				if(index < 0){
					index += str.length();
				}
				if(index < str.length()){
					return str.charAt(index);
				}
			}
			// else{
			// 	throw new RuntimeError(select.ques,
			// 						   "Array(String) out of bounds("
			// 						   + str.length() + ")!");
			// }
		}else if(select.list instanceof Expr.Variable){
			Expr.Variable var = (Expr.Variable)select.list;
			Object object = environment.get(var.name);
			if(object instanceof rl_Array){
				 // Object[] list = (Object[])object;
				 // if(index < list.length){
				 // 	 return list[index];
				 // }else{
				 // 	 throw new RuntimeError(select.ques,"Array out of bounds("
				 // 							+ list.length + ")!");
				 // }
				rl_Array array = (rl_Array)object;
				return array.get(condition);
			}
		}
		// if(condition instanceof Boolean){
		// 	if((Boolean)condition){
		// 		exec(selectStmt.ifstmt);
		// 	}else if(selectStmt.elsestmt.size() > 0){
		// 		exec(selectStmt.elsestmt.get(0));
		// 	}
		// }else if(condition instanceof Integer){
		// 	Integer index = (Integer)condition;
		// 	if(index < 0) {
		// 		return null;
		// 	}else if(index == 0) {
		// 		exec(selectStmt.ifstmt);
		// 	}else if(index > 0 &&
		// 			 selectStmt.elsestmt.size() >= index){
		// 		exec(selectStmt.elsestmt.get(index - 1));
		// 	}
		// }
		return null;
	}
	@Override
	public Object visitCallableExpr(Expr.CallableExpr callableExpr){
		Object callee = eval(callableExpr.callee);
		if(!(callee instanceof Callable)){
			throw new RuntimeError(callableExpr.sharp,
								   "This isn't Callable!");
		}

		List<Object> argu;
		
		if(callableExpr.argu instanceof Expr.ArrayExpr) {
			Expr.ArrayExpr list = (Expr.ArrayExpr)(callableExpr.argu);
			Object[] array = ((rl_Array)(list.accept(this))).array;
			argu = Arrays.asList(array);
		}else if(callableExpr.argu instanceof Expr.Variable){
			Expr.Variable var = (Expr.Variable)(callableExpr.argu);
			Object list = environment.get(var.name);
			Object[] list2;
			if(list instanceof Object[]){
				list2 = (Object[])list;
			}else{
				throw new RuntimeError(callableExpr.sharp,
									   "This Varibale isn't an array!");
			}
			argu = Arrays.asList(list2);
		}else{
			throw new RuntimeError(callableExpr.sharp,
									   "Expect valid arguments after callee!");
		}

		Callable function = (Callable)callee;

		if(function.arity() != argu.size()){
			throw new RuntimeError(callableExpr.sharp,"Expect " +
								   function.arity() + " arguments " +
								   "rather than " + argu.size() + "!");
		}
		
	    return function.call(this, argu);
	}
	@Override
	public Callable visitFuncDecl(Expr.FunctionDecl functionDecl) {
		Function function = new Function(functionDecl, this);
	    return function;
	}
	
	// Stmt
	@Override
	public String visitWriteStmt(Stmt.Write write){
		Object value = eval(write.expr);
		switch (write.mode) {
		case Write.Mode.LINE:
			System.out.println(stringify(value));
			break;
		case Write.Mode.FILE:
			// 未实现
			break;
		case Write.Mode.NOUN:
		default:
			System.out.print(stringify(value));
			break;
		}
	    return stringify(value);
	}
	@Override
	public Void visitExprStmt(Stmt.Expression expression){
		eval(expression.expr);
	    return null;
	}
	@Override
	public String visitDeclareStmt(Stmt.Declare declare){
		// System.out.println(declare.name.literal);
		Object value = null;
		if(declare.length == null){
			value = eval(declare.initializer);
			environment.define(declare.name.lexeme, value);
			return "$ " + declare.name.lexeme + " = "
				+ stringify_withquote(value);
		}else{
			int length = (Integer)eval(declare.length);
			Object[] list = new Object[length];
			Map<Object,Integer> index_map = null;
			
			Object init = eval(declare.initializer);
			if(init instanceof rl_Array){
				rl_Array array = (rl_Array)init;
				Object[] src = array.array;
				if(src.length <= length){
					System.arraycopy(src, 0,
									 list, 0, src.length);
				}else{
					// throw new RuntimeError("List value's length must be less than list variable's length!");
					System.arraycopy(src, 0,
									 list, 0, length);
				}
				index_map = array.index_map;
			}else{
				list[0] = init;
			}
			
			rl_Array array = new rl_Array(list,index_map);
			environment.define(declare.name.lexeme, array);
		}
		return null;
	}
	@Override
	public Object visitLiteralStmt(Stmt.LiteralStmt literal){
		String stmts = "";
		Object value = eval(literal.literal);
		if(value instanceof String){
			stmts = stringify(value);
		}else{
		    throw new RuntimeError(literal.grave,"Except 'Statement String' after Grave!");
		}
		Scanner scanner = new Scanner(stmts);
		List<Token> tokens = scanner.scanTokens();
		Parser parser = new Parser(tokens);

		Environment previous = this.environment;
		Object result = null;
		try {
			this.environment = new Environment(previous);
			result = interpert_noStruct(parser.parse_noStruct());
		} finally {
			this.environment = previous;
		}
	    return result;
	}
	@Override
	public Void visitBlock(Stmt.Block block){
		execBlock(block, new Environment(environment));
		return null;
	}
	private void execBlock(Stmt.Block block,Environment environment){
		Environment previous = this.environment;
		try {
		    this.environment = environment;
			for (Stmt stmt : block.stmts) {
				exec(stmt);
			}
		} finally {
			this.environment = previous;
		}
	    return;
	}
	@Override
	public Void visitLoopStmt(Stmt.LoopStmt loopStmt) {
		try{
			while (true) {
			    exec(loopStmt.block);
			}
		}catch(BreakException be){}
		
		return null;
	}
	@Override
	public Void visitBreakStmt(Stmt.BreakStmt breakStmt) {
		throw new BreakException();
	}
	@Override
	public Object visitReturnStmt(Stmt.ReturnStmt returnStmt){
		Object value = eval(returnStmt.expr);
		throw new ReturnException(value);
	}
	@Override
	public Object visitReadStmt(Stmt.ReadStmt readStmt){
		InputStreamReader input = new InputStreamReader(System.in);
		BufferedReader reader = new BufferedReader(input);
		String inString = "";
		try {
			inString = reader.readLine();
		} catch (IOException e) {}
		
		return inString;
	}
}

class BreakException extends RuntimeException{}
class ReturnException extends RuntimeException{
	final Object value;
	public ReturnException(Object object){
		this.value = object;
	}
}
