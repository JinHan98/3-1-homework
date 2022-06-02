package funInter;
import java.util.*;

class TypeMap extends HashMap<Variable, Type>{
	public void display() {
		for(Entry<Variable, Type> entry : entrySet()) {
			System.out.println("(" + entry.getKey().toString() + ", " + entry.getValue().toString() + ")");
		}
	}
	
}

public class TypeChecker {
	public static TypeMap typing(Declarations d) { //Declarations
		TypeMap map = new TypeMap();
		for(Declaration dec : d) {
			if(map.containsKey(dec.v)) {
				error(dec);
			}
			else { //
				map.put(dec.v, dec.t);
			}
		}
		return map;
	}

	public static void dupCheck(Declarations d){
		for(int i = 0; i < d.size() - 1; i++){
			Declaration s = d.get(i);
			for(int j = i + 1; j < d.size(); j++){
				Declaration t = d.get(j);
				check(!(s.v.equals(t.v)), "duplicate declaration : " + t.v);
			}
		}
	}
	
	private static void error(Declaration dec) {
        System.err.println("duplicate declaration : " + dec.v );
        System.exit(1);
    }
	
	public static void check(Boolean contain, String msg) {
		if(contain) {
			return;
		}
		else {
			System.err.println(msg);
			System.exit(1);
		}
	}
	
	public static void main(String[] args) {
		parser = new Parser(new Lexer("p2.cl"));
        Program prog = parser.program();
        //prog.display();
        prog.V();
	}
	static Parser parser;
}
