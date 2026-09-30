package cbyj.func;

import cbyj.parser.*;
import java.util.List;

public interface Callable {
	public int arity();
	public Object call(Interpreter interpreter,List<Object> arguments);
}
