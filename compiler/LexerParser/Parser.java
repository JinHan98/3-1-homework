import java.util.*;
import java.util.concurrent.locks.Condition;

public class Parser {
    // Recursive descent parser that inputs a C++Lite program and 
    // generates its abstract syntax.  Each method corresponds to
    // a concrete syntax grammar rule, which appears as a comment
    // at the beginning of the method.
  
    Token token;          // current token from the input stream
    Lexer lexer;
  
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
        // Program --> void main ( ) '{' Declarations Statements '}'
        TokenType[ ] header = {TokenType.Int, TokenType.Main,
                          TokenType.LeftParen, TokenType.RightParen};
        for (int i=0; i<header.length; i++)   // bypass "int main ( )"
            match(header[i]);
        match(TokenType.LeftBrace);
        Declarations d=declarations();
        Block b=no_block_statements();
        Program p=new Program(d, b);
        match(TokenType.RightBrace);
        return p;
    }
  
    private Declarations declarations () {
        // Declarations --> { Declaration }
        Declarations d=new Declarations();
        while(isType()){
            declaration(d);
        }
        return d;
    }
  
    private void declaration (Declarations ds) {
        // Declaration  --> Type Identifier { , Identifier } ;
        Variable v;
	    Declaration d;
	    Type t = type();
	    v = new Variable(match(TokenType.Identifier));
	    d = new Declaration(v, t);	
	    ds.add(d);

		while (isComma()) {	
			token = lexer.next();	
			v = new Variable(match(TokenType.Identifier));
			d = new Declaration(v, t);
			ds.add(d);
		}
	    match(TokenType.Semicolon);
    }
  
    private Type type () {
        // Type  -->  int | bool | float | char 
        Type t=null;
        if (token.type().equals(TokenType.Int)) {
            t=Type.INT;
        } 
        else if (token.type().equals(TokenType.Bool)) {
            t=Type.BOOL;
        }
        else if (token.type().equals(TokenType.Float)) {
            t=Type.FLOAT;
        }
        else if (token.type().equals(TokenType.Char)) {
            t=Type.CHAR;
        }
        else{
            error("there is no type");
        }
        token=lexer.next();
        return t;
    }
  
    private Statement statement() {
        // Statement --> ; | Block | Assignment | IfStatement | WhileStatement
        Statement s=null;
        if(token.type().equals(TokenType.Semicolon)){
            s = new Skip();
        }
        else if(token.type().equals(TokenType.LeftBrace)){
            s=no_block_statements();
        }
        else if(token.type().equals(TokenType.If)){
            s=ifStatement();
        }
        else if(token.type().equals(TokenType.While)){
            s=whileStatement();
        }
        else if(token.type().equals(TokenType.Identifier)){
            s=assignment();
        }
        else{
            error("there is no statement");
        }
        return s;
    }
    private Block no_block_statements () {
        // Block --> '{' Statements '}'
        Block b = new Block();
        Statement s;
        // student exercise
        while(token.type().equals(TokenType.Semicolon)||token.type().equals(TokenType.LeftBrace)||
        token.type().equals(TokenType.If)||token.type().equals(TokenType.While)
        ||token.type().equals(TokenType.Identifier)){
            s=statement();
            b.members.add(s);
        }
        return b;
    }
    private Block statements () {
        // Block --> '{' Statements '}'
        Block b = new Block();
        Statement s;
        // student exercise
        match(TokenType.LeftBrace);
        while(token.type().equals(TokenType.Semicolon)||token.type().equals(TokenType.LeftBrace)||
        token.type().equals(TokenType.If)||token.type().equals(TokenType.While)
        ||token.type().equals(TokenType.Identifier)){
            s=statement();
            b.members.add(s);
        }
        match(TokenType.RightBrace);
        return b;
    }
    private Assignment assignment () {
        // Assignment --> Identifier = Expression ;
        Variable v=new Variable(match(TokenType.Identifier));
        match(TokenType.Assign);
        Expression e=expression();
        match(TokenType.Semicolon);
        Assignment a=new Assignment(v, e);
        return a;
    }
  
    private Conditional ifStatement () {
        // IfStatement --> if ( Expression ) Statement [ else Statement ]
        Conditional c;
        if(!isIf()){
            error("there is no If");
        }
        match(TokenType.If);
        match(TokenType.LeftParen);
        Expression e=expression();
        match(TokenType.RightParen);
        Statement s=statements();
        if(isElse()){
            match(TokenType.Else);
            Statement el=statement();
            c=new Conditional(e, s, el);
        }
        else{
            c=new Conditional(e, s);
        }
        return c;
    }
  
    private Loop whileStatement () {
        // WhileStatement --> while ( Expression ) Statement
        if(!isWhile()){
            error("there is no while");
        }
        match(TokenType.While);
        match(TokenType.LeftParen);
        Expression e=expression();
        match(TokenType.RightParen);
        Statement s=statement();
        Loop l=new Loop(e, s);
        return l;
    }

    private Expression expression () {
        // Expression --> Conjunction { || Conjunction }
        Expression e=conjunction();
        while(isOrOp()){
            Operator op=new Operator(match(token.type()));
            Expression term2=conjunction();
            e= new Binary(op, e, term2);
        }
        return e;
    }
  
    private Expression conjunction () {
        // Conjunction --> Equality { && Equality }
        Expression e=equality();
        while(isAndOp()){
            Operator op=new Operator(match(token.type()));
            Expression term2=equality();
            e= new Binary(op, e, term2);
        }
        return e;
    }
  
    private Expression equality () {
        // Equality --> Relation [ EquOp Relation ]
        Expression e=relation();
        if(isEqualityOp()){
            Operator op=new Operator(match(token.type()));
            Expression term2=relation();
            e= new Binary(op, e, term2);
        }
        return e;
    }

    private Expression relation (){
        // Relation --> Addition [RelOp Addition] 
        Expression e=addition();
        if(isRelationalOp()){
            Operator op=new Operator(match(token.type()));
            Expression term2=addition();
            e= new Binary(op, e, term2);
        }
        return e;  // student exercise
    }
  
    private Expression addition () {
        // Addition --> Term { AddOp Term }
        Expression e = term();
        while (isAddOp()) {
            Operator op = new Operator(match(token.type()));
            Expression term2 = term();
            e = new Binary(op, e, term2);
        }
        return e;
    }
  
    private Expression term () {
        // Term --> Factor { MultiplyOp Factor }
        Expression e = factor();
        while (isMultiplyOp()) {
            Operator op = new Operator(match(token.type()));
            Expression term2 = factor();
            e = new Binary(op, e, term2);
        }
        return e;
    }
  
    private Expression factor() {
        // Factor --> [ UnaryOp ] Primary 
        if (isUnaryOp()) {
            Operator op = new Operator(match(token.type()));
            Expression term = primary();
            return new Unary(op, term);
        }
        else return primary();
    }
  
    private Expression primary () {
        // Primary --> Identifier | Literal | ( Expression )
        //             | Type ( Expression )
        Expression e = null;
        if (token.type().equals(TokenType.Identifier)) {
            e = new Variable(match(TokenType.Identifier));
        } else if (isLiteral()) {
            e = literal();
        } else if (token.type().equals(TokenType.LeftParen)) {
            token = lexer.next();
            e = expression();       
            match(TokenType.RightParen);
        } else if (isType( )) {
            Operator op = new Operator(match(token.type()));
            match(TokenType.LeftParen);
            Expression term = expression();
            match(TokenType.RightParen);
            e = new Unary(op, term);
        } else error("Identifier | Literal | ( | Type");
        return e;
    }

    private Value literal( ) {
        Value value = null;
	    String stval = token.value();
	    if (token.type().equals(TokenType.IntLiteral)) {
		    value = new IntValue (Integer.parseInt(stval));
            token = lexer.next();
	    }
	    else if (token.type().equals(TokenType.FloatLiteral))  {
		    value = new FloatValue(Float.parseFloat(stval));
		    token = lexer.next();
	    }
	    else if (token.type().equals(TokenType.CharLiteral))  {
		    value = new CharValue(stval.charAt(0));
		    token = lexer.next();
	    }
        else if (token.type().equals(TokenType.True))  {
            value = new BoolValue(true);
            token = lexer.next();
        }
        else if (token.type().equals(TokenType.False))  {
            value = new BoolValue(false);
            token = lexer.next();
        }
        else {
            error ("Error in literal value contruction");
        }
	    return value;
    }
  

    private boolean isAddOp( ) {
        return token.type().equals(TokenType.Plus) ||
               token.type().equals(TokenType.Minus);
    }
    
    private boolean isMultiplyOp( ) {
        return token.type().equals(TokenType.Multiply) ||
               token.type().equals(TokenType.Divide);
    }
    
    private boolean isUnaryOp( ) {
        return token.type().equals(TokenType.Not) ||
               token.type().equals(TokenType.Minus);
    }

    private boolean isOrOp(){
        return token.type().equals(TokenType.Or);
    }
    private boolean isAndOp(){
        return token.type().equals(TokenType.And);
    }
    private boolean isEqualityOp( ) {
        return token.type().equals(TokenType.Equals) ||
            token.type().equals(TokenType.NotEqual);
    }
    
    private boolean isElse(){
        return token.type().equals(TokenType.Else);
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
            || token.type().equals(TokenType.Char);
    }
    
    private boolean isComma(){
        return token.type().equals(TokenType.Comma);
    }
    private boolean isLiteral( ) {
        return token.type().equals(TokenType.IntLiteral) ||
            isBooleanLiteral() ||
            token.type().equals(TokenType.FloatLiteral) ||
            token.type().equals(TokenType.CharLiteral);
    }

    private boolean isWhile(){
        return token.type().equals(TokenType.While);
    }
    
    private boolean isIf(){
        return token.type().equals(TokenType.If);
    }

    private boolean isBooleanLiteral( ) {
        return token.type().equals(TokenType.True) ||
            token.type().equals(TokenType.False);
    }
    
    public static void main(String args[]) {
        Parser parser  = new Parser(new Lexer(args[0]));
        Program prog = parser.program();
        prog.display(0);           // display abstract syntax tree
    } //main

} // Parser
