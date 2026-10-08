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
			int i = (int)index;
			if(i < 0){
				i += array.length;
			}
			if(i < array.length){
				return array[i];
			}
		}
		return null;
	}

	public void assign(Object index,Object value){
		if(index_map != null && index_map.containsKey(index)){
			array[index_map.get(index)] = value;
		}
		if(index instanceof Integer){
			int i = (int)index;
			if(i < 0){
				i += array.length;
			}
			if(i < array.length){
				array[i] = value;
			}
		}
	}

	public Integer length(){
		return array.length;
	}

	public rl_Array append(Object rear){
		if(rear instanceof rl_Array){
			rl_Array rear_array = (rl_Array)rear;
			Object[] new_array = new Object[this.length() +
											rear_array.length()];
			System.arraycopy(this.array, 0,
							 new_array, 0, this.length());
			System.arraycopy(rear_array.array, 0,
							 new_array, this.length(), rear_array.length());
			
			Map<Object,Integer> new_index_map = new HashMap<>();
			this.index_map.forEach((k,v) -> {
					new_index_map.put(k, v);
				});
			rear_array.index_map.forEach((k,v) -> {
					new_index_map.put(k, v + this.length());
				});

			return new rl_Array(new_array, new_index_map);
		}else{
			Object[] new_array = new Object[this.length() + 1];
			System.arraycopy(this.array, 0,
							 new_array, 0, this.length());
			new_array[new_array.length - 1] = rear;
			return new rl_Array(new_array, this.index_map);
		}
	}

	public rl_Array put_front(Object front){
		Object[] new_array = new Object[this.length() + 1];
		System.arraycopy(this.array, 0,
						 new_array, 1, this.length());
		new_array[0] = front;
		return new rl_Array(new_array, this.index_map);
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
