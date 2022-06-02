package funInter;
// Abstract syntax for the language C++Lite,
// exactly as it appears in Appendix B.


import javax.xml.crypto.dsig.keyinfo.RetrievalMethod;
import java.util.*;

class Fl{
    //public static final String sero = "┃\t";
    //public static final String three = "┠━━━";
    //public static final String garo = "━━━━";
    //public static final String nien = "┗━━━";
    //public static final String tab = "    ";
    public static final String sero = "\t";
    public static final String three = "\t";
    public static final String garo = "\t";
    public static final String nien = "\t";

}


class Program {
    // Program = Declarations globals ; Functions functions
    Declarations globals; // -> 전역변수로 변경
    Functions functions; // 함수들의 집합으로 변경

    Program (Declarations g, Functions f) {
    	globals = g;
    	functions = f;
    }

    public String display(){
    	String s = "";
        int i = 0;
        System.out.println("Program");
        s += ("Program\n");
        s += globals.display(++i);
        s += functions.display(i);
        return s;
    }
    
    public void V() { // 타입 체크 함수 validity check
        TypeChecker.check(functions.getFunc("main") != null, "There is no main!");

        Declarations gf = new Declarations();
        gf.addAll(globals);
        gf.addAll(functions.getAllFuncName());
        TypeChecker.dupCheck(gf);


    	TypeMap map = TypeChecker.typing(globals);    // 선언부로 타입 맵 생성

    	functions.V(map);  // body validity check
    	System.out.println("Program has no type error"); // 타입 체크 통과
    }

}

class Functions extends ArrayList<Function>{
	
	public Function getFunc(String name) { // 함수 찾는 메소드
		for(Function f : this) {
			if(f.id.equals(name)) return f;
		}
		return null;
	}

    public Declarations getAllFuncName(){ // 중복 확인 할 때 쓰는 모든 함수 이름 반환
        Declarations d = new Declarations();
        for(Function f : this){
            d.add(new Declaration(new Variable(f.id), f.t));
        }
        return d;
    }

	public String display(int k) {
		String s = "";
		for(int i = 0; i < k; i++){
            System.out.print(i == 0 ? Fl.three : Fl.garo);
            s += (i == 0 ? Fl.three : Fl.garo);
        }
		System.out.println("Functions : ");
        s += "Functions : \n";
		for(Function f : this) {
			s += f.display(k + 1);
		}
		return s;
	}

    public void V(TypeMap map){ // 타입체크
        for(Function f : this){
            Declarations d = new Declarations();
            d.addAll(f.locals);
            d.addAll(f.params);
            TypeChecker.dupCheck(d);        // 중복체크

            // extern 없어서 다 한곳에 넣고 같은 것 있으면 local이 덮어버림
            TypeMap funcUseMap = new TypeMap();
            funcUseMap.putAll(map);
            funcUseMap.putAll(TypeChecker.typing(f.params));
            funcUseMap.putAll(TypeChecker.typing(f.locals));
            f.V(funcUseMap, this); // 각 함수별로 타입 체크
        }
    }
}

class Function{
	Type t; // 함수 리턴 타입
	String id;  // 함수명
	Declarations params, locals;    // 파라미터, 지역변수
	Block body; // 함수가 수행할 명령들
	
	public Function(Type type, String name, Declarations params, Declarations locals, Block body) {
		this.t = type;
		this.id = name;
		this.params = params;
		this.locals = locals;
		this.body = body;
	}
	
	public String display(int k) {
		String s = "";
		for(int i = 0; i < k; i++){
            System.out.print(i == 0 ? Fl.three : Fl.garo);
            s += (i == 0 ? Fl.three : Fl.garo);
        }
		System.out.println("Function : " + id + ", Return : " + t.toString());
		s += ("Function : " + id + ", Return : " + t.toString() + "\n");
		for(int i = 0; i < k; i++){
            System.out.print(i == 0 ? Fl.three : Fl.garo);
            s += (i == 0 ? Fl.three : Fl.garo);
        }
		System.out.println("Params : ");
		s += ("Params : \n");
		s += params.display(k + 1);
		for(int i = 0; i < k; i++){
            System.out.print(i == 0 ? Fl.three : Fl.garo);
            s += (i == 0 ? Fl.three : Fl.garo);
        }
		System.out.println("Locals : ");
		s += ("Locals : \n");
		s += locals.display(k + 1);
		for(int i = 0; i < k; i++){
            System.out.print(i == 0 ? Fl.three : Fl.garo);
            s += (i == 0 ? Fl.three : Fl.garo);
        }
		System.out.println("Body : ");
		s += ("Body : \n");
		s += body.display(k + 1);
		return s;
	}

	public void V(TypeMap map, Functions fs) { // 타입체크
        boolean hasReturn = false;  // 리턴 여부
        for(Statement s : body.members){    // 함수 명령들 각각 타입 체크
            if(s instanceof Return) hasReturn = true;
            s.V(map, fs);
        }

        if(!(t.equals(Type.VOID)) && !(id.equals("main"))){ // void나 main 아니면 리턴 있어야 함
            TypeChecker.check(hasReturn, "Non void function " + id + " has no return");
        }
        else if(t.equals(Type.VOID)){ // void는 리턴 있으면 안됨
            TypeChecker.check(!hasReturn, "Void function " + id + "has return");
        }
    }
}

class Declarations extends ArrayList<Declaration> {
    // Declarations = Declaration*
    // (a list of declarations d1, d2, ..., dn)
    public String display(int k){
    	String s = "";
        for(int i = 0; i < k; i++){
            System.out.print(i == 0 ? Fl.three : Fl.garo);
            s += (i == 0 ? Fl.three : Fl.garo);
        }
        System.out.println("Declarations : ");
        s += "Declarations : \n";
        boolean last = false;
        for(int j = 0; j < size(); j++){
            if(j == size() - 1) last = true;
            s += (get(j).display(k + 1, last));
        }
        return s;
    }
    
}

class Declaration {
// Declaration = Variable v; Type t
    Variable v;
    Type t;

    Declaration (Variable var, Type type) {
        v = var; t = type;
    } // declaration */

    public String display(int k, boolean l){
    	String s = "";
        for(int i = 0; i < k; i++){
            if(i == 0){
                System.out.print(Fl.sero);
                s += Fl.sero;
            }else if(!l){
                System.out.print(Fl.three);
                s += Fl.three;
            }else{
                System.out.print(Fl.nien);
                s += Fl.nien;
            }
        }
        System.out.println(" (" + t + ", " + v + ") ");
        s += (" (" + t + ", " + v + ") \n");
        return s;
    }

}

class Type {        // void, undef 추가
    // Type = int | bool | char | float | void | undef
    final static Type INT = new Type("int");
    final static Type BOOL = new Type("bool");
    final static Type CHAR = new Type("char");
    final static Type FLOAT = new Type("float");
    final static Type VOID = new Type("void");
    final static Type UNDEFINED = new Type("undef");
    
    private String id;

    private Type (String t) { id = t; }

    public String toString ( ) { return id; }
}

abstract interface Statement {
    // Statement = Skip | Block | Assignment | Conditional | Loop
    // return, call, print, printCh 추가
    abstract String display(int k);
    abstract void V(TypeMap map, Functions fs);
    abstract RS M(RS state, Functions fs);
}

class Skip implements Statement {
	public String display(int k) {return "";}
	public void V(TypeMap map, Functions fs){} // skip은 체크할 것 없음
	public RS M(RS state, Functions fs) {return state;}
}

class Block implements Statement {
    // Block = Statement*
    //         (a Vector of members)
    public ArrayList<Statement> members = new ArrayList<Statement>();

    public String display(int k){
    	String s = ""; 
        for(int i = 0; i < k; ++i){
            System.out.print(i == 0 ? Fl.three : Fl.garo);
            s += (i == 0 ? Fl.three : Fl.garo);
        }
        if(k == 1) {
        	System.out.println("Statements : ");
            s += "Statements : \n";
        }
        else {
        	System.out.println("Blocks : ");
            s += "Blocks : \n";
        }
        
        for(int j = 0; j < members.size(); j++){
            s += members.get(j).display(k + 1);
        }
        return s;
    }
    
    public void V(TypeMap map, Functions fs) {
    	for(Statement st : members) { // 블락은 안에 존재하는 statement 전부 확인
    		st.V(map, fs);
    	}
    	
    }

    public RS M(RS state, Functions fs) {
    	for(Statement s : members) {
    		state = s.M(state, fs);
    	}
    	return state;
    }
}

class Assignment implements Statement {
    // Assignment = Variable target; Expression source
    Variable target;
    Expression source;

    Assignment (Variable t, Expression e) {
        target = t;
        source = e;
    }

    public String display(int k){
    	String s = "";
        for(int i = 0; i < k; ++i){
            System.out.print(i == 0 ? Fl.three : Fl.garo);
            s += (i == 0 ? Fl.three : Fl.garo);
        }
        System.out.println("Assignment : ");
        s += "Assignment : \n";
        s += target.display(++k);
        s += source.display(k);
        return s;
    }

    public void V(TypeMap map, Functions fs) { // 타겟과 소스의 타입을 확인 후 둘이 같은지 확인
    	TypeChecker.check(map.containsKey(target), "undefined target in assignment : " + target.toString());
        target.V(map, fs);
        Type tType = target.typeOf(map, fs); // 변수가 미리 선언되었는지 위에서 확인
        source.V(map, fs);
        Type srcType = source.typeOf(map, fs); // 소스의 타입 확인
    	if(srcType == Type.UNDEFINED) { // 선언되지 않은 변수가 소스로 왔을 경우 오류 출력
    		TypeChecker.check(false, "undefined source in assignment : " + source.toString());
    	} // 타입이 같으면 그냥 통과
    	if(tType != srcType) { // 다른데 동치인 경우는 통과
    		if(tType == Type.FLOAT) { // 타겟이 실수일 때 정수를 집어넣는 경우는 통과
    			TypeChecker.check(srcType == Type.INT, "type error.. " + target.toString()
    		+ " is " + tType.toString() + " but source is " + srcType.toString());
    		}
    		else if(tType == Type.INT) { // 타겟이 정수일 때 캐릭터를 집어넣는 경우는 통과
    			TypeChecker.check(srcType == Type.CHAR, "type error.. " + target.toString()
        		+ " is " + tType.toString() + " but source is " + srcType.toString());
    		}
    		else { // 나머지 경우는 전부 에러 출력
    			TypeChecker.check(false, "mixed mode assignment to " + target.toString()
    			+ " is " + tType.toString() + " but source is " + srcType.toString());
    		}
    	}
    }
    
    public RS M(RS state, Functions fs) {
    	return state.onion(target, source.Me(state, fs));
    }
}

class Conditional implements Statement {
// Conditional = Expression test; Statement thenbranch, elsebranch
    Expression test;
    Statement thenbranch, elsebranch;
    // elsebranch == null means "if... then"
    
    Conditional (Expression t, Statement tp) {
        test = t; thenbranch = tp; elsebranch = new Skip( );
    }
    
    Conditional (Expression t, Statement tp, Statement ep) {
        test = t; thenbranch = tp; elsebranch = ep;
    }
    
    public String display(int k){
    	String s = "";
        for(int i = 0; i < k; ++i){
            System.out.print(i == 0 ? Fl.three : Fl.garo);
            s += (i == 0 ? Fl.three : Fl.garo);
        }
        System.out.println("Conditional : ");
        s += "Conditional : \n";
        s += test.display(++k);
        for(int i = 0; i < k - 1; ++i){
            System.out.print(i == 0 ? Fl.three : Fl.garo);
            s += (i == 0 ? Fl.three : Fl.garo);
        }
        System.out.println("if Statements : ");
        s += "if Statements : \n";
        s += thenbranch.display(k);
        if(!(elsebranch instanceof Skip)) {
        	for(int i = 0; i < k - 1; ++i){
        		System.out.print(i == 0 ? Fl.three : Fl.garo);
        		s += (i == 0 ? Fl.three : Fl.garo);
        	}
        	System.out.println("else Statements : ");
        	s += "else Statements : \n";
        	s += elsebranch.display(k);
        }
        return s;
        
        
    }
    
    public void V(TypeMap map, Functions fs) { // 조건식이 bool이 맞는지 확인하고 then, else statement 확인
        test.V(map, fs);
        Type testType = test.typeOf(map, fs);
    	TypeChecker.check(testType == Type.BOOL, "If Condition must be Bool");
    	thenbranch.V(map, fs);
    	elsebranch.V(map, fs);
    }
    
    public RS M(RS state, Functions fs) {
    	if(test.Me(state, fs).boolValue()) {
    		return thenbranch.M(state, fs);
    	}
    	else {
    		return elsebranch.M(state, fs);
    	}
    }
}

class Loop implements Statement {
// Loop = Expression test; Statement body
    Expression test;
    Statement body;

    Loop (Expression t, Statement b) {
        test = t; body = b;
    }
    
    public String display(int k){
    	String s = "";
        for(int i = 0; i < k; ++i){
            System.out.print(i == 0 ? Fl.three : Fl.garo);
            s += (i == 0 ? Fl.three : Fl.garo);
        }
        System.out.println("Loop : ");
        s += "Loop : \n";
        s += test.display(++k);
        s += body.display(k);
        return s;
    }
    
    public void V(TypeMap map, Functions fs) { // 조건식 bool 확인하고 body 확인
        test.V(map, fs);
        Type testType = test.typeOf(map, fs);
    	TypeChecker.check(testType == Type.BOOL, "Loop Condition must be Bool");
    	body.V(map, fs);
    }
    
    public RS M(RS state, Functions fs) {
    	while(test.Me(state, fs).boolValue()) {
    		state =  body.M(state, fs);
    	}
    	return state;
    }
}

class Call implements Statement, Expression{ // 함수 부르는 call

	String name;    // 부르는 함수 명
	ArrayList<Expression> args = new ArrayList<>();     // arguments
	
	public Call(String name, ArrayList<Expression> args) {
		this.name = name;
		this.args = args;
	}

	public void V(TypeMap map, Functions fs) {  // 타입 체크
        // 함수 정의됐는지 먼저 확인
        Function f = fs.getFunc(name);
        TypeChecker.check(f != null, "Undefined Function used : " + name);
        // parameter랑 argument 개수 확인
        TypeChecker.check(f.params.size() == args.size(),
                "Incorrect number of arguments : " + name);
        // 각 위치 별로 타입 맞는지 확인
        for(int i = 0; i < args.size(); i++){
            Type paramType = f.params.get(i).t;
            args.get(i).V(map, fs);
            Type argType = args.get(i).typeOf(map, fs);
            TypeChecker.check(paramType == argType,
                    name + "'s " + f.params.get(i).v.toString() + "got a " + argType.toString() + ", expected a " + paramType.toString());
        }

    }

    public Type typeOf(TypeMap map, Functions fs) {
        Function f = fs.getFunc(name);
        TypeChecker.check(f != null, "Undefined Function:" + name);
        return f.t;
    }


    public Value Me(RS state, Functions fs) { // expression call의 semantics
        state = this.M(state, fs);  // 함수 수행 후
        Value returnValue = state.get(new Variable(name)); // 리턴하면 함수명에 리턴값 저장되어있음
        state.popState(); // 리턴됐으니까 Runtime Stack에서 제거
        return returnValue; // 리턴 값 반환
    }

    public RS M(RS state, Functions fs) { // statement call의 semantics
		Function f = fs.getFunc(name);  // 함수 불러오기
        State funcState = new State();  // 함수가 쓸 state

        for(Declaration d : f.locals){  // state에 지역변수 추가
            funcState.put(d.v, Value.mkValue(d.t));
        }
        // 지역변수에 argument로 들어온 값 대입
        Iterator<Expression> argIter = args.iterator();
        Iterator<Declaration> paramIter = f.params.iterator();
        while(argIter.hasNext()){
            Expression exp = argIter.next();
            Declaration dec = paramIter.next();
            Value v = exp.Me(state, fs);
            funcState.put(dec.v, v);
        }
        state.pushState(funcState);

        if(!name.equals(Token.mainTok) && !(fs.getFunc(name).t.equals(Type.VOID))){
            //main이나 void 아니면 반환값을 위한 공간 확보
            state.put(new Variable(name), Value.mkValue(fs.getFunc(name).t));
        }

        for(Statement members : f.body.members){    //함수 statement 수행
            // 다른 곳에서 return시 해당 함수의 이름이 있으니까 종료를 위해 return
            // main은 undef로 저장되어있음, void는 상관없음
            if(state.get(new Variable(name)) != null && !state.get(new Variable(name)).isUndef()){
                return state;
            }
            // 명령들 계속 수행
            state = members.M(state, fs);
        }
        //state.display();
        return state;

	}

	public String display(int k) {
        String s = "";
        for(int i = 0; i < k; ++i){
            System.out.print(i == 0 ? Fl.three : Fl.garo);
            s += (i == 0 ? Fl.three : Fl.garo);
        }
        System.out.println("Call : " + name);
        s += ("Loop : " + name + "\n");

        for(int i = 0; i < k + 1; ++i){
            System.out.print(i == 0 ? Fl.three : Fl.garo);
            s += (i == 0 ? Fl.three : Fl.garo);
        }
        System.out.println("Arguments :");
        s += ("Arguments : \n");
        for(Expression e : args){
            s += e.display(k + 2);
        }
        return s;
	}
	
}

class Return implements Statement{  // return state
    Variable func;
    Expression result;

    public Return(Variable name, Expression result){
        this.func = name;
        this.result = result;
    }

    public String display(int k) {
        String s = "";
        for(int i = 0; i < k; ++i){
            System.out.print(i == 0 ? Fl.three : Fl.garo);
            s += (i == 0 ? Fl.three : Fl.garo);
        }
        System.out.println("Return :");
        s += ("Return : \n");
        s += result.display(k+1);
        return s;
    }

    public void V(TypeMap map, Functions fs) {  // return 타입 체크
        // 함수 먼저 찾고
        Function f = fs.getFunc(func.toString());
        // 우변 타입 계산
        Type res = result.typeOf(map, fs);
        // 함수의 타입과 리턴 타입이 다르면 에러 출력
        TypeChecker.check(res.equals(f.t), "Return type is " + f.t.toString() + " but get " + res);
    }


    public RS M(RS state, Functions fs) {   // 리턴 semantics
        // return 값 계산
        Value returnValue = result.Me(state, fs);
        // 함수 이름과 함께 값 저장
        state.put(func, returnValue);
        //System.out.println("return statement");
        //state.display();
        return state;
    }
}

class Print implements Statement{
	Expression source;
	
	Print(Expression s){
		source = s;
		
	}
	
	public String display(int k) {
		String s = "";
		for(int i = 0; i < k; ++i){
            System.out.print(i == 0 ? Fl.three : Fl.garo);
            s += (i == 0 ? Fl.three : Fl.garo);
        }
		System.out.println("Print : ");
		s += "print : \n";
		s += source.display(k);
		return s;
	}


	public void V(TypeMap map, Functions fs) {  // 프린트 타입 char아니면 다 가능
        source.V(map, fs);
		Type srcType = source.typeOf(map, fs);
		TypeChecker.check(srcType != Type.CHAR, "print Char use printCh");
	}
	
	public RS M(RS state, Functions fs) {   // 프린트 수행
		Value v = source.Me(state, fs);
		Type type = v.type();
		if (type == Type.INT) {IntValue vv = (IntValue)v; System.out.print(vv);}
        if (type == Type.BOOL) {BoolValue vv = (BoolValue)v; System.out.print(vv);}
        if (type == Type.FLOAT) {FloatValue vv = (FloatValue)v; System.out.print(vv);}

    	return state;
	}
}

class PrintCh implements Statement{ // char 전용 프린트
	Expression source;
	
	PrintCh(Expression s){
		source = s;
	}
	
	public String display(int k) {
		String s = "";
		for(int i = 0; i < k; ++i){
            System.out.print(i == 0 ? Fl.three : Fl.garo);
            s += (i == 0 ? Fl.three : Fl.garo);
        }
		System.out.println("PrintCh : ");
		s += "printCh : \n";
		s += source.display(k);
		return s;
	}

	public void V(TypeMap map, Functions fs) {
        source.V(map, fs);
		Type srcType = source.typeOf(map, fs);
		TypeChecker.check(srcType == Type.CHAR, "printCh only print Char");
	}
	
	public RS M(RS state, Functions fs) {
		CharValue v = (CharValue)source.Me(state, fs);
    	System.out.print(v.toString());
    	return state;
	}
}

abstract interface Expression {
    // Expression = Variable | Value | Binary | Unary
    abstract String display(int k);
    abstract void V(TypeMap map, Functions fs);
    abstract Type typeOf(TypeMap map, Functions fs);
    abstract Value Me(RS state, Functions fs);

}

class Variable implements Expression {
    // Variable = String id
    private String id;

    Variable (String s) { id = s; }

    public String toString( ) { return id; }
    
    public boolean equals (Object obj) {
        String s = ((Variable) obj).id;
        return id.equals(s); // case-sensitive identifiers
    }
    
    public int hashCode ( ) { return id.hashCode( ); }

    public String display(int k){
    	String s = "";
        for(int i = 0; i < k; ++i){
            System.out.print(i == 0 ? Fl.three : Fl.garo);
            s += (i == 0 ? Fl.three : Fl.garo);
        }
        System.out.println("Variable " + id);
        s += ("Variable " + id + "\n");
        return s;
    }
    
    public void V(TypeMap map, Functions fs) { // 변수는 타입 맵에 있으면 해당 타입 반환, 아니면 undefined 반환
    	Type type = map.get(this);
    	TypeChecker.check(type != null, "Undefined Variable");

    }


    public Type typeOf(TypeMap map, Functions fs) {
        Type type = map.get(this);
        if(type == null) return Type.UNDEFINED;
        return type;
    }

    public Value Me(RS state, Functions fs) {
    	return state.get(this);
    }
    // 값 찾아서 반환
}

abstract class Value implements Expression {
    // Value = IntValue | BoolValue |
    //         CharValue | FloatValue
    // VoidValue 추가(함수때문에)
    protected Type type;
    protected boolean undef = true;

    int intValue ( ) {
        assert false : "should never reach here";
        return 0;
    } // implementation of this function is unnecessary can can be removed.
    
    boolean boolValue ( ) {
        assert false : "should never reach here";
        return false;
    }
    
    char charValue ( ) {
        assert false : "should never reach here";
        return ' ';
    }
    
    float floatValue ( ) {
        assert false : "should never reach here";
        return 0.0f;
    }

    boolean isUndef( ) { return undef; }

    Type type ( ) { return type; }

    static Value mkValue (Type type) {
        if (type == Type.INT) return new IntValue( );
        if (type == Type.BOOL) return new BoolValue( );
        if (type == Type.CHAR) return new CharValue( );
        if (type == Type.FLOAT) return new FloatValue( );
        if (type == Type.VOID) return new VoidValue();
        throw new IllegalArgumentException("Illegal type in mkValue");
    }
    public void V(TypeMap map, Functions fs) {} // 상수는 해당 타입 반환
    public Type typeOf(TypeMap map, Functions fs) {return type;}
    public Value Me(RS state, Functions fs) {
    	return this;
    }
}

class VoidValue extends Value{
    private String value = "void";
    VoidValue(){type = Type.VOID;}
    VoidValue(String v){this(); value=v;undef=false;}

    @Override
    public String display(int k) {
        return null;
    }
}


class IntValue extends Value {
    private int value = 0;

    IntValue ( ) { type = Type.INT; }

    IntValue (int v) { this( ); value = v; undef = false; }

    int intValue ( ) {
        assert !undef : "reference to undefined int value";
        return value;
    }

    public String toString( ) {
        if (undef)  return "undef";
        return "" + value;
    }

    public String display(int k){
    	String s = "";
        for(int i = 0; i < k; ++i){
            System.out.print(i == 0 ? Fl.three : Fl.garo);
            s += (i == 0 ? Fl.three : Fl.garo);
        }
        System.out.println("IntLiteral : " + value);
        s += ("IntLiteral : " + value + "\n");
        return s;
    }
    
    
}

class BoolValue extends Value {
    private boolean value = false;

    BoolValue ( ) { type = Type.BOOL; }

    BoolValue (boolean v) { this( ); value = v; undef = false; }

    boolean boolValue ( ) {
        assert !undef : "reference to undefined bool value";
        return value;
    }

    int intValue ( ) {
        assert !undef : "reference to undefined bool value";
        return value ? 1 : 0;
    }

    public String toString( ) {
        if (undef)  return "undef";
        return "" + value;
    }

    public String display(int k){
    	String s = "";
        for(int i = 0; i < k; ++i){
            System.out.print(i == 0 ? Fl.three : Fl.garo);
            s += (i == 0 ? Fl.three : Fl.garo);
        }
        System.out.println("BoolLiteral : " + value);
        s += ("BoolLiteral : " + value + "\n");
        return s;
    }
}

class CharValue extends Value {
    private char value = ' ';

    CharValue ( ) { type = Type.CHAR; }

    CharValue (char v) { this( ); value = v; undef = false; }

    char charValue ( ) {
        assert !undef : "reference to undefined char value";
        return value;
    }

    public String toString( ) {
        if (undef)  return "undef";
        return "" + value;
    }

    public String display(int k){
    	String s = "";
        for(int i = 0; i < k; ++i){
            System.out.print(i == 0 ? Fl.three : Fl.garo);
            s += (i == 0 ? Fl.three : Fl.garo);
        }
        System.out.println("CharLiteral : " + value);
        s += ("CharLiteral : " + value + "\n");
        return s;
    }
}

class FloatValue extends Value {
    private float value = 0;

    FloatValue ( ) { type = Type.FLOAT; }

    FloatValue (float v) { this( ); value = v; undef = false; }

    float floatValue ( ) {
        assert !undef : "reference to undefined float value";
        return value;
    }

    public String toString( ) {
        if (undef)  return "undef";
        return "" + value;
    }

    public String display(int k){
    	String s = ""; 
        for(int i = 0; i < k; ++i){
            System.out.print(i == 0 ? Fl.three : Fl.garo);
            s += (i == 0 ? Fl.three : Fl.garo);
        }
        System.out.println("FloatLiteral : " + value);
        s += ("FloatLiteral : " + value + "\n");
        return s;
    }
}

class Binary implements Expression {
// Binary = Operator op; Expression term1, term2
    Operator op;
    Expression term1, term2;

    Binary (Operator o, Expression l, Expression r) {
        op = o; term1 = l; term2 = r;
    } // binary

    public String display(int k){
    	String s = "";
        for(int i = 0; i < k; i++){
            System.out.print(i == 0 ? Fl.three : Fl.garo);
            s += (i == 0 ? Fl.three : Fl.garo);
        }
        System.out.print("Binary : ");
        s += ("Binary : ");
        s += op.display(++k);
        s += term1.display(k);
        s += term2.display(k);
        return s;
    }
    
    public void V(TypeMap map, Functions fs) {
        term1.V(map, fs);
        term2.V(map, fs);
    	Type type1 = term1.typeOf(map, fs);
    	Type type2 = term2.typeOf(map, fs);
    	// 먼저 둘의 타입이 같은지 확인
    	TypeChecker.check(type1 == type2, "Term1 and Term2 must have same type");
    	if(type1 == Type.UNDEFINED) { // 선언되었는지 확인
    		TypeChecker.check(false, "Term1 is undefined");
    	}else if(type2 == Type.UNDEFINED) { // 선언되었는지 확인
    		TypeChecker.check(false, "Term2 is undefined");
    	}

        if(op.ArithmeticOp() ) {
            TypeChecker.check((type1 == Type.INT || type1 == Type.FLOAT), "type error : " + op.toString());
        }
        else if(op.isMod()){    // % 연산자 추가 (int만 가능)
            TypeChecker.check(type1 == Type.INT, "type error : " + op.toString() + " must terms are INT");
        }
        else if(op.BooleanOp()) { // && 나 || 이고 타입이 불리언이면 불 반환
            TypeChecker.check((type1 == Type.BOOL), "Operand is not bool : " + op.toString());
        }
        else if(op.RelationalOp()) { // 관계연산자이고 둘이 같으면 불리언 반환
            TypeChecker.check((type1 == type2), "type error : " + op.toString());
        }
        else { // 나머지는 다 에러 반환
            TypeChecker.check(false, "wrong operator " + op.toString());
        }

    }

    public Type typeOf(TypeMap map, Functions fs){ // 타입체크랑 타입 반환 분리함
        Type type1 = term1.typeOf(map, fs);
        Type type2 = term2.typeOf(map, fs);
        // 사칙연산이고 타입이 정수나 실수이면 term1 타입 반환
        if(op.ArithmeticOp() && (type1 == Type.INT || type1 == Type.FLOAT)) {
            return type1;
        }
        else if(op.isMod() && type1 == Type.INT){
            return Type.INT;
        }
        else if(op.BooleanOp() && (type1 == Type.BOOL)) { // && 나 || 이고 타입이 불리언이면 불 반환
            return Type.BOOL;
        }
        else if(op.RelationalOp()) { // 관계연산자이고 둘이 같으면 불리언 반환
            return Type.BOOL;
        }
        else { // 나머지는 다 에러 반환
            TypeChecker.check(false, "wrong operator " + op.toString());
            return null;
        }
    }
    
    public Value Me(RS state, Functions fs) {   // 계산한 값 반환
    	return Semantics.applyBinary(op, term1.Me(state, fs), term2.Me(state, fs));
    }
}

class Unary implements Expression {
    // Unary = Operator op; Expression term
    Operator op;
    Expression term;

    Unary (Operator o, Expression e) {
        op = o; term = e;
    } // unary

    public String display(int k){
    	String s = "";
        for(int i = 0; i < k; ++i){
            System.out.print(i == 0 ? Fl.three : Fl.garo);
            s += (i == 0 ? Fl.three : Fl.garo);
        }
        System.out.print("Unary : ");
        s += ("Unary : ");
        s += op.display(++k);
        s += term.display(k);
        return s;
    }
    
    public void V(TypeMap map, Functions fs) { // 단항연산자 체크
        term.V(map, fs);
    	Type type = term.typeOf(map, fs);
    	if(op.NotOp()) { // !이고 불리언이면
    		TypeChecker.check(type == Type.BOOL, op.toString() + ": non bool operand");
    	}
    	else if(op.NegateOp()) { // -이고 정수나 실수면 해당 타입 반환
            TypeChecker.check((type == Type.INT || type == Type.FLOAT), op.toString() + ": non int of non float operand");
    	}//char 캐스팅이고 타입이 int나 char면 char 반환
    	else if(op.charOp()) {
            TypeChecker.check((type == Type.INT || type == Type.CHAR), op.toString() + ": non int operand");
    	}// float 캐스팅이고 타입이 int나 float이면 float 반환
    	else if(op.floatOp()) {
            TypeChecker.check((type == Type.INT || type == Type.FLOAT), op.toString() + ": non int operand");
    	} // int 캐스팅이고 타입이 char, float, int이면 int 반환
    	else if(op.intOp()) {
            TypeChecker.check((type == Type.CHAR || type == Type.FLOAT || type == Type.INT), op.toString() + ": non float or non char operand");
    	}
    	else { // 나머지는 에러 출력
    		TypeChecker.check(false, "wrong operator" + op.toString());
    	}
    }

    public Type typeOf(TypeMap map, Functions fs){
        Type type = term.typeOf(map, fs);
        if(op.NotOp() && type == Type.BOOL) { // !이고 불리언이면 불리언 반환
            return type;
        }
        else if(op.NegateOp() && (type == Type.INT || type == Type.FLOAT)) { // -이고 정수나 실수면 해당 타입 반환
            return type;
        }//char 캐스팅이고 타입이 int나 char면 char 반환
        else if(op.charOp() && (type == Type.INT || type == Type.CHAR)) {
            return Type.CHAR;
        }// float 캐스팅이고 타입이 int나 float이면 float 반환
        else if(op.floatOp() && (type == Type.INT || type == Type.FLOAT)) {
            return Type.FLOAT;
        } // int 캐스팅이고 타입이 char, float, int이면 int 반환
        else if(op.intOp() && (type == Type.CHAR || type == Type.FLOAT || type == Type.INT)) {
            return Type.INT;
        }
        else { // 나머지는 에러 출력
            TypeChecker.check(false, "wrong operator" + op.toString());
            return null;
        }
    }
    
    public Value Me(RS state, Functions fs) {
    	return Semantics.applyUnary(op, term.Me(state, fs));
    }
}

class Operator {
    // Operator = BooleanOp | RelationalOp | ArithmeticOp | UnaryOp
    // BooleanOp = && | ||
    final static String AND = "&&";
    final static String OR = "||";
    // RelationalOp = < | <= | == | != | >= | >
    final static String LT = "<";
    final static String LE = "<=";
    final static String EQ = "==";
    final static String NE = "!=";
    final static String GT = ">";
    final static String GE = ">=";
    // ArithmeticOp = + | - | * | /
    final static String PLUS = "+";
    final static String MINUS = "-";
    final static String TIMES = "*";
    final static String DIV = "/";
    final static String MOD = "%";
    // UnaryOp = !    
    final static String NOT = "!";
    final static String NEG = "-";
    // CastOp = int | float | char
    final static String INT = "int";
    final static String FLOAT = "float";
    final static String CHAR = "char";
    // Typed Operators
    // RelationalOp = < | <= | == | != | >= | >
    final static String INT_LT = "INT<";
    final static String INT_LE = "INT<=";
    final static String INT_EQ = "INT==";
    final static String INT_NE = "INT!=";
    final static String INT_GT = "INT>";
    final static String INT_GE = "INT>=";
    // ArithmeticOp = + | - | * | /
    final static String INT_PLUS = "INT+";
    final static String INT_MINUS = "INT-";
    final static String INT_TIMES = "INT*";
    final static String INT_DIV = "INT/";
    // UnaryOp = !    
    final static String INT_NEG = "-";
    // RelationalOp = < | <= | == | != | >= | >
    final static String FLOAT_LT = "FLOAT<";
    final static String FLOAT_LE = "FLOAT<=";
    final static String FLOAT_EQ = "FLOAT==";
    final static String FLOAT_NE = "FLOAT!=";
    final static String FLOAT_GT = "FLOAT>";
    final static String FLOAT_GE = "FLOAT>=";
    // ArithmeticOp = + | - | * | /
    final static String FLOAT_PLUS = "FLOAT+";
    final static String FLOAT_MINUS = "FLOAT-";
    final static String FLOAT_TIMES = "FLOAT*";
    final static String FLOAT_DIV = "FLOAT/";
    // UnaryOp = !    
    final static String FLOAT_NEG = "-";
    // RelationalOp = < | <= | == | != | >= | >
    final static String CHAR_LT = "CHAR<";
    final static String CHAR_LE = "CHAR<=";
    final static String CHAR_EQ = "CHAR==";
    final static String CHAR_NE = "CHAR!=";
    final static String CHAR_GT = "CHAR>";
    final static String CHAR_GE = "CHAR>=";
    // RelationalOp = < | <= | == | != | >= | >
    final static String BOOL_LT = "BOOL<";
    final static String BOOL_LE = "BOOL<=";
    final static String BOOL_EQ = "BOOL==";
    final static String BOOL_NE = "BOOL!=";
    final static String BOOL_GT = "BOOL>";
    final static String BOOL_GE = "BOOL>=";
    // Type specific cast
    final static String I2F = "I2F";
    final static String F2I = "F2I";
    final static String C2I = "C2I";
    final static String I2C = "I2C";
    
    String val;
    
    Operator (String s) { val = s; }

    public String toString( ) { return val; }
    public boolean equals(Object obj) { return val.equals(obj); }
    
    boolean BooleanOp ( ) { return val.equals(AND) || val.equals(OR); }
    boolean RelationalOp ( ) {
        return val.equals(LT) || val.equals(LE) || val.equals(EQ)
            || val.equals(NE) || val.equals(GT) || val.equals(GE);
    }
    boolean ArithmeticOp ( ) {
        return val.equals(PLUS) || val.equals(MINUS)
            || val.equals(TIMES) || val.equals(DIV);
    }

    boolean isMod(){
        return val.equals(MOD);
    }
    boolean NotOp ( ) { return val.equals(NOT) ; }
    boolean NegateOp ( ) { return val.equals(NEG) ; }
    boolean intOp ( ) { return val.equals(INT); }
    boolean floatOp ( ) { return val.equals(FLOAT); }
    boolean charOp ( ) { return val.equals(CHAR); }

    final static String intMap[ ] [ ] = {
        {PLUS, INT_PLUS}, {MINUS, INT_MINUS},
        {TIMES, INT_TIMES}, {DIV, INT_DIV},
        {EQ, INT_EQ}, {NE, INT_NE}, {LT, INT_LT},
        {LE, INT_LE}, {GT, INT_GT}, {GE, INT_GE},
        {NEG, INT_NEG}, {FLOAT, I2F}, {CHAR, I2C}
    };

    final static String floatMap[ ] [ ] = {
        {PLUS, FLOAT_PLUS}, {MINUS, FLOAT_MINUS},
        {TIMES, FLOAT_TIMES}, {DIV, FLOAT_DIV},
        {EQ, FLOAT_EQ}, {NE, FLOAT_NE}, {LT, FLOAT_LT},
        {LE, FLOAT_LE}, {GT, FLOAT_GT}, {GE, FLOAT_GE},
        {NEG, FLOAT_NEG}, {INT, F2I}
    };

    final static String charMap[ ] [ ] = {
        {EQ, CHAR_EQ}, {NE, CHAR_NE}, {LT, CHAR_LT},
        {LE, CHAR_LE}, {GT, CHAR_GT}, {GE, CHAR_GE},
        {INT, C2I}
    };

    final static String boolMap[ ] [ ] = {
        {EQ, BOOL_EQ}, {NE, BOOL_NE}, {LT, BOOL_LT},
        {LE, BOOL_LE}, {GT, BOOL_GT}, {GE, BOOL_GE},
    };

    final static private Operator map (String[][] tmap, String op) {
        for (int i = 0; i < tmap.length; i++)
            if (tmap[i][0].equals(op))
                return new Operator(tmap[i][1]);
        assert false : "should never reach here";
        return null;
    }

    final static public Operator intMap (String op) {
        return map (intMap, op);
    }

    final static public Operator floatMap (String op) {
        return map (floatMap, op);
    }

    final static public Operator charMap (String op) {
        return map (charMap, op);
    }

    final static public Operator boolMap (String op) {
        return map (boolMap, op);
    }

    public String display(int k){
    	String s = "";
        System.out.println(val);
        s += (val + "\n");
        return s;
    }
    
    
    
}



