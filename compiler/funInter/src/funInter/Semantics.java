package funInter;
import java.time.temporal.ValueRange;
import java.util.*;

class State extends HashMap<Variable, Value> { 
    // Defines the set of variables and their associated values 
    // that are active during interpretation
    
    public State( ) { }
    
    public State(Variable key, Value val) {
        put(key, val);
    }
    
    public State onion(Variable key, Value val) {
        put(key, val);
        return this;
    }
    
    public State onion (State t) {
        for (Variable key : t.keySet( ))
            put(key, t.get(key));
        return this;
    }

    void display() {
	Iterator<Variable> types = keySet().iterator();
	Iterator<Value> variables = values().iterator();
	System.out.print ("{");
	while (types.hasNext() && variables.hasNext()) {
		System.out.print ("< " + types.next() +"," + variables.next() + " >,");	
	}
	System.out.println ( "}");
	
   }
}

class RS extends Stack<State>{
	public RS(){}

	public RS pushState(State state){
		push(state);
		return this;
	}

	public RS popState(){
		pop();
		return this;
	}

	public State peekState(){
		return peek();
	}

	public RS put(Variable key, Value val){
		State top = peek();
		top.put(key, val);
		return this;
	}

	public RS remove(Variable key){
		State top = peek();
		top.remove(key);
		return this;
	}

	public Value get(Variable key){
		State top = peek();
		Iterator<State> stateIterator = listIterator();
		State global = stateIterator.next();	
		if(top.get(key) != null){
			return top.get(key);
		}
		else{
			return global.get(key);
		}
	}

	public RS onion(Variable key, Value val){
		State top = peek();
		Iterator<State> stateIterator = listIterator();
		State global = stateIterator.next();	
		if(top.get(key) != null){
			top.put(key, val);
		}
		else{
			global.put(key, val);
		}
		return this;
	}

	public RS onion(State st){
		State top = peek();
		for(Variable key : st.keySet()){
			top.put(key, st.get(key));
		}
		return this;
	}

	public void display(){
		System.out.println("\tGlobals and top frame:");
		System.out.println("----------------------------");
		Iterator<State> stateIterator = iterator();
		State global = stateIterator.next();
		State top = peek();
		if(global.equals(top)){
			global.display();
		}
		else{
			global.display();
			top.display();
		}
		System.out.println("----------------------------");
	}

}



public class Semantics {
	
    RS M (Program p) {
		RS rs = new RS();
		rs.pushState(initialState(p.globals));
		rs = new Call("main", new ArrayList<>()).M(rs, p.functions);
		rs.popState();
		return rs;
    }
  
    State initialState (Declarations d) {
        State state = new State();
        for (Declaration decl : d)
            state.put(decl.v, Value.mkValue(decl.t));
        return state;
    }


    static Value applyBinary (Operator op, Value v1, Value v2) {
        TypeChecker.check( ! v1.isUndef( ) && ! v2.isUndef( ),
               "reference to undef value");
        
        if(v1.type().equals(Type.INT)) {
        	switch(op.val) {
        	// INT ARITHMETIC
        	case Operator.PLUS:
        		return new IntValue(v1.intValue( ) + v2.intValue( ));
        	case Operator.MINUS:
        		return new IntValue(v1.intValue( ) - v2.intValue( ));
        	case Operator.TIMES:
        		return new IntValue(v1.intValue( ) * v2.intValue( ));
        	case Operator.DIV:
        		return new IntValue(v1.intValue( ) / v2.intValue( ));
			case Operator.MOD:
				return new IntValue(v1.intValue() % v2.intValue());
        	// INT RELATION
        	case Operator.LT:
        		return new BoolValue(v1.intValue( ) < v2.intValue( ));
        	case Operator.LE:
        		return new BoolValue(v1.intValue( ) <=v2.intValue( ));
        	case Operator.EQ:
        		return new BoolValue(v1.intValue( ) == v2.intValue( ));
        	case Operator.NE:
        		return new BoolValue(v1.intValue( ) != v2.intValue( ));	
        	case Operator.GT:
        		return new BoolValue(v1.intValue( ) > v2.intValue( ));
        	case Operator.GE:
        		return new BoolValue(v1.intValue( ) >= v2.intValue( ));
        	}
        }
        else if(v1.type().equals(Type.FLOAT)){
        	switch(op.val) {
        	// FLOAT ARITHMETIC
        	case Operator.PLUS:
        		return new FloatValue(v1.floatValue( ) + v2.floatValue( ));
        	case Operator.MINUS:
                return new FloatValue(v1.floatValue( ) - v2.floatValue( ));
        	case Operator.TIMES:
        		return new IntValue(v1.intValue( ) * v2.intValue( ));
        	case Operator.DIV:
        		return new FloatValue(v1.floatValue( ) / v2.floatValue( ));
        	// FLOAT RELATION
        	case Operator.LT:
        		return new BoolValue(v1.floatValue( ) < v2.floatValue( ));
        	case Operator.LE:
        		return new BoolValue(v1.floatValue( ) <= v2.floatValue( ));
        	case Operator.EQ:
        		return new BoolValue(v1.floatValue( ) == v2.floatValue( ));
        	case Operator.NE:
        		return new BoolValue(v1.floatValue( ) != v2.floatValue( ));
        	case Operator.GT:
        		return new BoolValue(v1.floatValue( ) > v2.floatValue( ));
        	case Operator.GE:
        		return new BoolValue(v1.floatValue( ) >= v2.floatValue( ));
        	}
        }
        else if(v1.type().equals(Type.CHAR)){
        	switch(op.val) {	// CHAR RELATION
        	case Operator.LT:
        		return new BoolValue(v1.charValue( ) < v2.charValue( ));
        	case Operator.LE:
        		return new BoolValue(v1.charValue( ) <=v2.charValue( ));
        	case Operator.EQ:
        		return new BoolValue(v1.charValue( ) == v2.charValue( ));
        	case Operator.NE:
        		return new BoolValue(v1.charValue( ) != v2.charValue( ));
        	case Operator.GT:
        		return new BoolValue(v1.charValue( ) > v2.charValue( ));
        	case Operator.GE:
        		return new BoolValue(v1.charValue( ) >= v2.charValue( ));
        	}
        }
        else if(v1.type().equals(Type.BOOL)){
        	switch(op.val) {	// BOOL RELATION
        	case Operator.LT:
        		return new BoolValue(v1.intValue() < v2.intValue());
        	case Operator.LE:
        		return new BoolValue(v1.intValue() <= v2.intValue());
        	case Operator.EQ:
        		return new BoolValue(v1.intValue() == v2.intValue());
        	case Operator.NE:
        		return new BoolValue(v1.intValue() != v2.intValue());
        	case Operator.GT:
        		return new BoolValue(v1.intValue() > v2.intValue());
        	case Operator.GE:
        		return new BoolValue(v1.intValue() >= v2.intValue());
        	case Operator.AND:
        		return new BoolValue(v1.boolValue() && v2.boolValue());
        	case Operator.OR:
        		return new BoolValue(v1.boolValue() || v2.boolValue());
        	}
        }
        else {
        	TypeChecker.check(false,"type is undefined");
        }
        return v1;
    }

    static Value applyUnary (Operator op, Value v) {
        TypeChecker.check( ! v.isUndef( ),
               "reference to undef value");
        switch(op.val) {
        case Operator.NOT:
        	return new BoolValue(!v.boolValue( ));
        case Operator.MINUS:
        	if(v.type().equals(Type.INT)) {
        		return new IntValue(-v.intValue( ));
        	}
        	else if(v.type().equals(Type.FLOAT)) {
        		return new FloatValue(-v.floatValue( ));
        	}
        case Operator.INT:
        	if(v.type().equals(Type.FLOAT)) {
        		return new IntValue((int)(v.floatValue( )));
        	}
        	else if(v.type().equals(Type.CHAR)) {
        		return new IntValue((int)(v.charValue( )));
        	}
        case Operator.FLOAT:
        	if(v.type().equals(Type.INT)) {
        		return new IntValue((int)(v.floatValue( )));
        	}
        case Operator.CHAR:
        	if(v.type().equals(Type.INT)) {
        		return new IntValue((int)(v.charValue( )));
        	}
        }
        return v;
    } 


    public static void main(String args[]) {
        //Parser parser  = new Parser(new Lexer("p2.cl"));
		Parser parser  = new Parser(new Lexer("p.txt"));
        Program prog = parser.program();
        prog.display();   
        System.out.println("\nBegin type checking...");
        System.out.println("Type map:");
        TypeMap map = TypeChecker.typing(prog.globals); 
        map.display();   
        prog.V(); //type check (validate)
        Semantics semantics = new Semantics( );
		System.out.println("Program start-------------------:");
        RS state = semantics.M(prog);
        //System.out.println("Final State");
        //state.display( );  // print final state
    }
}

