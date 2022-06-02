package funInter;
import java.util.*;

public class Parser {
    // Recursive descent parser that inputs a C++Lite program and 
    // generates its abstract syntax.  Each method corresponds to
    // a concrete syntax grammar rule, which appears as a comment
    // at the beginning of the method.
  
    Token token;          // current token from the input stream
    Lexer lexer;
    Variable currentFunc;

  
    public Parser(Lexer ts) { // Open the C++Lite source program
        lexer = ts;                          // as a token stream, and
        token = lexer.next();            // retrieve its first Token
    }
  
    private String match (TokenType t) { // * return the string of a token if it matches with t *
        String value = token.value();
        if (token.type().equals(t))
            token = lexer.next();
        else
            error(t);
        return value;
    }
  
    private void error(TokenType tok) {
        System.err.println("Syntax error: expecting: " + tok 
                           + "; saw: " + token);
        System.exit(1);
    }
  
    private void error(String tok) {
        System.err.println("Syntax error: expecting: " + tok 
                           + "; saw: " + token);
        System.exit(1);
    }
  
    public Program program() {
        // Program --> '{' Type id funcOrGlob '}' int main ( )
        Declarations globals = new Declarations();
        Functions functions = new Functions();

        while(isType()){
            functionOrGlobals(globals, functions);
        }

        Function mainFunc = mainFunction();
        functions.add(mainFunc);

        return new Program(globals, functions);

    }

    private void functionOrGlobals(Declarations gs, Functions fs){
        Type t = type();

        if(t.equals(Type.INT) && token.type().equals(TokenType.Main)){
            return;
        }

        Variable v = new Variable(match(TokenType.Identifier));
        // '(' 확인
        if(token.type().equals(TokenType.LeftParen)){
            currentFunc = v;
            token = lexer.next();
            Declarations params = new Declarations();
            if(isType()) params = Parameters();
            match(TokenType.RightParen);    // ')'
            match(TokenType.LeftBrace);     // '{'
            Declarations locals = declarations();
            Block body = programStatements();
            match(TokenType.RightBrace);    // '}'
            fs.add(new Function(t, v.toString(), params, locals, body));
        }
        else{
            if(t.equals(Type.VOID)) error("Variable can't have void type");
            gs.add(new Declaration(v, t));

            while(token.type().equals(TokenType.Comma)){
                token = lexer.next();
                v = new Variable(match(TokenType.Identifier));
                gs.add(new Declaration(v, t));
            }
            match(TokenType.Semicolon);
        }
    }

    private Declarations Parameters(){
        // [Type Id {, Type Id}]
        Declarations params = new Declarations();
        Type t = type();
        Variable v = new Variable(match(TokenType.Identifier));
        params.add(new Declaration(v, t));

        while(token.type().equals(TokenType.Comma)){
            token = lexer.next();
            t = type();
            v = new Variable(match(TokenType.Identifier));
            params.add(new Declaration(v, t));
        }

        return params;
    }

    private Function mainFunction(){
        match(TokenType.Main);
        match(TokenType.LeftParen);
        Declarations params = declarations();   // 양식 맞추기용
        match(TokenType.RightParen);
        match(TokenType.LeftBrace); // '{' 확인
        Declarations dec = declarations(); // 선언부
        Block b = programStatements(); // 실행부
        match(TokenType.RightBrace); // '}' 확인
        return new Function(Type.INT, Token.mainTok.toString(), params, dec, b);
    }
  
    private Declarations declarations () {
        // Declarations --> { Declaration }
        Declarations ds = new Declarations(); // 선언문 배열
        while(isType()){ // int, float, char, bool 타입 중 하나면 반복
            declaration(ds);  // 선언문 추가
        }
        return ds;  //선언문 배열 반환
        // student exercise
    }
  
    private void declaration (Declarations ds) {
        // Declaration  --> Type Identifier { , Identifier } ;
        Type t = type(); // 현재 토큰 타입 (int|float|char|bool)
        Variable v = new Variable(match(TokenType.Identifier)); // 변수 값반환하고 다음 토큰 가리킴
        ds.add(new Declaration(v, t)); // 선언문 배열에 추가

        while(token.type().equals(TokenType.Comma)){ // 콤마 나오면 반복
            token = lexer.next(); // , 다음 토큰 가리킴
            v = new Variable(match(TokenType.Identifier)); // 위와 동일
            ds.add(new Declaration(v, t)); // 타입은 같으니 변수명만 바꿔서 추가
        }
        match(TokenType.Semicolon); // ';' 확인
        // student exercise
    }
  
    private Type type () {
        // Type  -->  int | bool | float | char | void
        Type t = null;
        // student exercise
        // int, bool, char, float 타입 확인 후 반환
        if(token.type().equals(TokenType.Int)){
            t = Type.INT;
        } else if(token.type().equals(TokenType.Bool)){
            t = Type.BOOL;
        } else if(token.type().equals(TokenType.Char)){
            t = Type.CHAR;
        } else if(token.type().equals(TokenType.Float)){
            t = Type.FLOAT;
        } else if(token.type().equals(TokenType.Void)){
            t = Type.VOID;
        } else{
            error("Type is only int | bool | char | float | void");
        }
        token = lexer.next(); // 다음 토큰 가리킴
        return t;          // 타입 반환
    }

    private Block programStatements(){ // statements -> {statement} 를 위한 함수
        Block b = new Block();
        Statement s;
        while(isStatement()){ // statement확인해서 반복
            s = statement();
            b.members.add(s); // 실행문 추가
        }
        return b;
    }
  
    private Statement statement() { // 문맥에 맞게 다음 선택해서 반환
        // Statement --> ; | Block | Assignment | IfStatement | WhileStatement
    	// print | printCh 추가
        // Call | return 추가
        Statement s = new Skip();
        // student exercise
        if(token.type().equals(TokenType.LeftBrace)){ // '{' 확인하면 block
            s = statements();
        } else if(token.type().equals(TokenType.Identifier)){ // 변수명을 확인하면
            Variable v = new Variable(match(TokenType.Identifier));
            if(token.type().equals(TokenType.Assign)){ //assignment
                s = assignment(v);
            }
            else if(token.type().equals(TokenType.LeftParen)){
                s = callStatement(v);
            }
            else{
                error("wrong Statement (skip|block|assignment|if|loop|return|call)");
            }

        } else if(token.type().equals(TokenType.If)){ // if면 조건문
            s = ifStatement();
        } else if(token.type().equals(TokenType.While)){ //while 이면 반복문
            s = whileStatement();
        } else if(token.type().equals(TokenType.Return)){
            s = returnStatement();
        }
        else if(token.type().equals(TokenType.Semicolon)){
        	token = lexer.next();
        }else if(token.type().equals(TokenType.Print)) {
        	s = printStatement();
        }else if(token.type().equals(TokenType.PrintCh)) {
        	s = printChStatement();
        }
        return s;
    }
  
    private Block statements () { // block 중괄호 안에 실행문 반복
        // Block --> '{' {Statement} '}'
        Block b = new Block();
        // student exercise
        Statement s;
        match(TokenType.LeftBrace); // 먼저 '{' 확인
        while(isStatement()){ // statement이면 반복
            s = statement(); // 현재 토큰에 맞는 실행문 반환받음
            b.members.add(s); // block의 멤버 배열에 실행문 추가
        }
        match(TokenType.RightBrace); // '}' 확인
        return b; // 블록 반환
    }

    private boolean isStatement(){ // statement 인지 확인하기 위해 추가 함수
        return token.type().equals(TokenType.Semicolon) ||
                token.type().equals(TokenType.LeftBrace) ||
                token.type().equals(TokenType.Identifier) ||
                token.type().equals(TokenType.If) ||
                token.type().equals(TokenType.While) ||
                token.type().equals(TokenType.Print) ||
                token.type().equals(TokenType.PrintCh) ||
                token.type().equals(TokenType.Return);
    }
  
    private Assignment assignment (Variable target) { // 대입문
        // Assignment --> Identifier = Expression ;
        match(TokenType.Assign); // '=' 확인
        Expression source = expression(); // 우변 식 추출
        match(TokenType.Semicolon); //';'확인
        // 대입문 노드로 만들어 반환
        return new Assignment(target, source);  // student exercise
    }
  
    private Conditional ifStatement () { // 조건문
        // IfStatement --> if ( Expression ) Statement [ else Statement ]
        Conditional cond;
        match(TokenType.If); // if 확인
        match(TokenType.LeftParen); // '(' 확인
        Expression test = expression(); // 조건식 추출
        match(TokenType.RightParen); // ')' 확인
        Statement thenBranch = statement(); // 참일 경우 실행문 추출
        if(token.type().equals(TokenType.Else)){ //else
            token = lexer.next(); // else 다음 토큰 가져옴
            Statement elseBranch = statement(); // else 실행문 추출
            cond = new Conditional(test, thenBranch, elseBranch); //else가 있으니 인자 3개 노드 생성
        } else { // else 없으면 인자 2개 노드 생성
            cond = new Conditional(test, thenBranch);
        }
        // 조건식 노드 반환
        return cond;  // student exercise
    }
  
    private Loop whileStatement () { // 반복문
        // WhileStatement --> while ( Expression ) Statement
        match(TokenType.While); // while 확인
        match(TokenType.LeftParen); // '(' 확인
        Expression test = expression(); // 조건식 추출
        match(TokenType.RightParen); // ')' 확인
        Statement body = statement(); // 반복할 실행문 추출
        // 반복문 노드 반환
        return new Loop(test, body);  // student exercise
    }

    private Call callStatement(Variable name){
        match(TokenType.LeftParen);
        ArrayList<Expression> args = new ArrayList<>();
        while(!(token.type().equals(TokenType.RightParen))){
            args.add(expression());
            if(token.type().equals(TokenType.Comma)){
                match(TokenType.Comma);
            }
        }
        match(TokenType.RightParen);
        return new Call(name.toString(), args);
    }

    private Return returnStatement(){
        match(TokenType.Return);
        Expression e = expression();
        match(TokenType.Semicolon);
        return new Return(currentFunc, e);
    }
    
    private Print printStatement() {
    	match(TokenType.Print);
    	Expression src = expression();
    	match(TokenType.Semicolon); //';'확인
    	return new Print(src);
    }
    
    private PrintCh printChStatement() {
    	match(TokenType.PrintCh);
    	Expression src = expression();
    	match(TokenType.Semicolon); //';'확인
    	return new PrintCh(src);
    }

    private Expression expression () { // 가장 우선순위가 낮은 or연산식 부터 시작
        // Expression --> Conjunction { || Conjunction }
        Expression c1 = conjunction(); // 항 하나 뽑고
        while(token.type().equals(TokenType.Or)){ // '||' or 가 있으면 연산자와 뒤 항 추가
            Operator op = new Operator(match(token.type())); 
            Expression c2 = conjunction();
            c1 = new Binary(op, c1, c2); // binary로 추출한 두 항과 연산자를 합침
        }
        // 최종 식 노드 반환
        return c1;  // student exercise
    }
  
    private Expression conjunction () { // 다음 우선순위 높은 and 연산식
        // Conjunction --> Equality { && Equality }
        Expression e1 = equality(); // 좌항
        while(token.type().equals(TokenType.And)){ // && 있으면 반복 연산자, 뒤 항 추가
            Operator op = new Operator(match(token.type())); 
            Expression e2 = equality();
            e1 = new Binary(op, e1, e2); // 두 항과 연산자 한 노드로 합침
        }
        // 최종 식 노드 반환
        return e1;  // student exercise
    }
  
    private Expression equality () { // 다음 우선순위 동등 비교 연산 (== | !=)
        // Equality --> Relation [ EquOp Relation ]
        Expression r1 = relation(); // 항 추출
        while(isEqualityOp()){ // ==, !=  연산자 있으면 반복
            Operator op = new Operator(match(token.type()));
            Expression r2 = relation(); 
            r1 = new Binary(op, r1, r2); // 연산자와 두 항 합침
        }
        //  최종 식 노드 반환
        return r1;  // student exercise
    }

    private Expression relation (){ // 다음 우선순위 (크기 비교) (> | >= | <= | <)
        // Relation --> Addition [RelOp Addition] 
        Expression a1 = addition(); // 항 추출
        while(isRelationalOp()){ // > >= < <=  연산자 있으면 반복
            Operator op = new Operator(match(token.type()));
            Expression a2 = addition();
            a1 = new Binary(op, a1, a2);// 두 항과 연산자 합침
        }
        //최종 노드 식 반환
        return a1;  // student exercise
    }

    
  
    private Expression addition () { // 덧셈, 뺄셈 (+ | -)
        // Addition --> Term { AddOp Term }
        Expression e = term(); // 항 추출
        while (isAddOp()) { // + - 있으면 반복
            Operator op = new Operator(match(token.type())); 
            Expression term2 = term();
            e = new Binary(op, e, term2); // 두 항과 연산자 합침
        }
        // 최종 노드 식 반환
        return e;
    }
  
    private Expression term () { // 더 우선순위 높은 곱셈 나눗셈 (* | / | %)
        // Term --> Factor { MultiplyOp Factor }
        Expression e = factor(); // 항 추출
        while (isMultiplyOp()) { // * / 곱, 나누기면 반복
            Operator op = new Operator(match(token.type()));
            Expression term2 = factor();
            e = new Binary(op, e, term2); // 두 항 합침
        }
        // 최종 노드 식
        return e;
    }
  
    private Expression factor() { // 가장 우선 순위가 높은 부정, 음수 (! | -)
        // Factor --> [ UnaryOp ] Primary 
        if (isUnaryOp()) { // !, - 있는지 확인 (항 앞에 있으니까, 우결합)
            Operator op = new Operator(match(token.type())); 
            Expression term = primary(); 
            return new Unary(op, term); // 연산자와 항 합쳐서 반환
        }
        else return primary(); // 아니면 다음 단계로 이동
    }
  
    private Expression primary () { // 변수, 상수, 괄호, 타입캐스팅, 함수호출
        // Primary --> Identifier | Literal | ( Expression )
        //             | Type ( Expression )
        Expression e = null;
        if (token.type().equals(TokenType.Identifier)) { // 변수 확인시 변수 반환
            Variable v = new Variable(match(TokenType.Identifier));
            if(token.type().equals(TokenType.LeftParen)){
                e = callStatement(v);
            }
            else{
                e = v;
            }
        } else if (isLiteral()) { // 상수 확인시 그냥 상수값 반환
            e = literal();
        } else if (token.type().equals(TokenType.LeftParen)) { // '(' 확인시
            token = lexer.next(); // '(' 다음 토큰으로 이동후
            e = expression();       // 식 처음부터 전개
            match(TokenType.RightParen); // 식 확인 되면 ')' 확인
        } else if (isType( )) { // 타입 캐스팅
            Operator op = new Operator(match(token.type())); // 타입 자체가 연산자로
            match(TokenType.LeftParen); //'(' 확인
            Expression term = expression(); // 괄호안 식 전개
            match(TokenType.RightParen); // ')' 확인
            e = new Unary(op, term); // 우결합 식 반환
        } else error("Id | Literal | ( | Type"); // 그 외는 에러
        return e; // 최종 식 반환
    }

    private Value literal( ) { // 상수값 반환
        Value v = null;
        String s = token.value();
        if(token.type().equals(TokenType.IntLiteral)){ // 정수
            v = new IntValue(Integer.parseInt(s));
        } 
        else if(token.type().equals(TokenType.FloatLiteral)){ //실수
            v = new FloatValue(Float.parseFloat(s));
        } 
        else if(token.type().equals(TokenType.CharLiteral)){ // 문자
        	if(s.charAt(0) == '\\' && s.charAt(1) == 'n') {v = new CharValue('\n');}
        	else if(s.charAt(0) == '\\' && s.charAt(1) == 't') {v = new CharValue('\t');}
        	else {v = new CharValue(s.charAt(0));}
        } 
        else if(token.type().equals(TokenType.True)){ // 불리언 참
            v = new BoolValue(true);
        } 
        else if(token.type().equals(TokenType.False)){ // 불리언 거짓
            v = new BoolValue(false);
        } 
        else { // 에러
            error("literal value Error");
        }
        token = lexer.next(); // 다음 토큰 가리키게 하고 상수값 반환
        return v;  // student exercise
    }
  

    private boolean isAddOp( ) {
        return token.type().equals(TokenType.Plus) ||
               token.type().equals(TokenType.Minus);
    }
    
    private boolean isMultiplyOp( ) {
        return token.type().equals(TokenType.Multiply) ||
               token.type().equals(TokenType.Divide) || token.type().equals(TokenType.Mod);
    }
    
    private boolean isUnaryOp( ) {
        return token.type().equals(TokenType.Not) ||
               token.type().equals(TokenType.Minus);
    }
    
    private boolean isEqualityOp( ) {
        return token.type().equals(TokenType.Equals) ||
            token.type().equals(TokenType.NotEqual);
    }
    
    private boolean isRelationalOp( ) {
        return token.type().equals(TokenType.Less) ||
               token.type().equals(TokenType.LessEqual) || 
               token.type().equals(TokenType.Greater) ||
               token.type().equals(TokenType.GreaterEqual);
    }
    
    private boolean isType( ) {
        return token.type().equals(TokenType.Int)
            || token.type().equals(TokenType.Bool) 
            || token.type().equals(TokenType.Float)
            || token.type().equals(TokenType.Char)
            || token.type().equals(TokenType.Void);
    }
    
    private boolean isLiteral( ) {
        return token.type().equals(TokenType.IntLiteral) ||
            isBooleanLiteral() ||
            token.type().equals(TokenType.FloatLiteral) ||
            token.type().equals(TokenType.CharLiteral);
    }
    
    private boolean isBooleanLiteral( ) {
        return token.type().equals(TokenType.True) ||
            token.type().equals(TokenType.False);
    }
    
    public static void main(String args[]) {
        Parser parser  = new Parser(new Lexer("p2.cl"));
        Program prog = parser.program();
        prog.display();           // display abstract syntax tree
    } //main

} // Parser
