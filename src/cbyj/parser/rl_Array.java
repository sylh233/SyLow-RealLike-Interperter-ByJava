package cbyj.parser;

import java.util.Map;
import java.util.HashMap;

public class rl_Array {
	public Object[] array;
	public Map<Object,Integer> index_map;
	public rl_Array(Object[] array,Map<Object,Integer> index_map){
		this.array = array;
		this.index_map = index_map;
	}

	public Object get(Object index){
		if(index_map != null && index_map.containsKey(index)){
			return array[index_map.get(index)];
		}
		if(index instanceof Integer){
			return array[(Integer)index];
		}
		return null;
	}

	public void assign(Object index,Object value){
		if(index_map != null && index_map.containsKey(index)){
			array[index_map.get(index)] = value;
		}
		if(index instanceof Integer){
			array[(Integer)index] = value;
		}
	}

	public Integer size(){
		return array.length;
	}

	@Override
	public String toString(){
	    String str = "(";
	    for (int i = 0;i < array.length-1;i++){
			str += Interpreter.stringify_withquote(array[i]) + ", ";
		}
		str += Interpreter.stringify_withquote(array[array.length - 1]) + ")";
		return str;
	}
}
