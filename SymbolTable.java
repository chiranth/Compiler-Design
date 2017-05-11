package cop5556sp17;



import cop5556sp17.AST.Dec;
import java.util.*;


public class SymbolTable {
	
	int current_level,next_level;
	HashMap<String,ArrayList<node>> symmap;
	Stack<Integer> scope_counter;
	
	public class node{
		int level;
		Dec dec;
		public node(Dec decl,int lev)
		{
			dec=decl;
			level=lev;
		}
	}
	//TODO  add fields

	/** 
	 * to be called when block entered
	 */
	public void enterScope(){
		//TODO:  IMPLEMENT THIS
		current_level=++next_level;
		scope_counter.add(current_level);
	}
	
	
	/**
	 * leaves scope
	 */
	public void leaveScope(){
		//TODO:  IMPLEMENT THIS
		scope_counter.pop();
		current_level = scope_counter.peek();
	}
	
	public boolean insert(String ident, Dec dec){
		//TODO:  IMPLEMENT THIS
		ArrayList<node> decls;
		node value=new node(dec,current_level);
		if(!symmap.containsKey(ident))
		{
			decls=new ArrayList<>();
			decls.add(value);
			symmap.put(ident, decls);
		}
		else{
			decls=symmap.get(ident);
			for(ListIterator<node> nodeiter = decls.listIterator();nodeiter.hasNext();)
			{
				node value1=nodeiter.next();
				if(value1.level==current_level)
				{
					return false;
				}
			}
			symmap.remove(ident);
			decls.add(0,value);
			symmap.put(ident, decls);
			
		}
		return true;
	}
	
	public Dec lookup(String ident){
		//TODO:  IMPLEMENT THIS
		Dec decl=null;
		int min=-1;
		if(symmap.containsKey(ident))
		{
			ArrayList<node> nodes=symmap.get(ident);
			for(ListIterator<node> nodeiter = nodes.listIterator();nodeiter.hasNext();)
			{
				node value=nodeiter.next();
				if((min==-1 || min>scope_counter.search(value.level))&& scope_counter.search(value.level)!=-1)
				{
					min=scope_counter.search(value.level);
					decl=value.dec;
				}
				
			}
		}
		return decl;
	}
		
	public SymbolTable() {
		//TODO:  IMPLEMENT THIS
		symmap=new HashMap<>();
		current_level=0;
		next_level=0;
		scope_counter=new Stack<Integer>();
		scope_counter.add(current_level);
	}


	@Override
	public String toString() {
		//TODO:  IMPLEMENT THIS
		return "";
	}
	
	


}
