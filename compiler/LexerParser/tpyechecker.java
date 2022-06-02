import java.util.*;

public class tpyechecker {
    public static TypeMap typing (Declarations d) { 
        TypeMap map = new TypeMap( );
        for (Declaration di : d) {
            map.put (di.v, di.t); 
        }
        return map; 
    }

    public static void check(boolean check, String msg) {
        if (check)  
            return;
        System.err.println(msg);
        System.exit(1);
    }

    public static void V (Declarations d) { 
        for (int i=0; i<d.size() - 1; i++){
            for (int j=i+1; j<d.size(); j++) {
                Declaration di = d.get(i);
                Declaration dj = d.get(j); check( ! (di.v.equals(dj.v)),"중복선언 " + dj.v);
            }
        }
    }
    public static void V (Program p) { 
        V (p.decpart);
        V (p.body, typing(p.decpart));
    }
    public static Type typeOf (Expression e, TypeMap tm) {
        if (e instanceof Value) return ((Value)e).type;
        if (e instanceof Variable) {
            Variable v = (Variable)e;
            check (tm.containsKey(v), "정의하지 않은 변수 " + v);
            return (Type) tm.get(v);
        }
        if (e instanceof Binary) {
            Binary b = (Binary)e;
            if (b.op.ArithmeticOp( ))
                if (typeOf(b.term1,tm)== Type.FLOAT)
                    return (Type.FLOAT);
                else return (Type.INT);
            if (b.op.RelationalOp( ) || b.op.BooleanOp( )) 
                return (Type.BOOL);
        }
        if (e instanceof Unary) {
            Unary u = (Unary)e;
            if (u.op.NotOp( ))        
                return (Type.BOOL);
            else if (u.op.NegateOp( )) 
                return typeOf(u.term,tm);
            else if (u.op.intOp( ))
                return (Type.INT);
            else if (u.op.floatOp( )) 
                return (Type.FLOAT);
            else if (u.op.charOp( ))  
                return (Type.CHAR);
        }
        throw new IllegalArgumentException("실행 에러 ");
        // 여가까지 실행되면 안됨
    } 
    public static void V (Statement s, TypeMap tm) {
        if ( s == null )
            throw new IllegalArgumentException( "null statement");
        if (s instanceof Skip) 
            return;
        if (s instanceof Assignment) {
            Assignment a = (Assignment)s;
            check( tm.containsKey(a.target), "정의 안된 target " + a.target);
            V(a.source, tm);
            Type ttype = (Type)tm.get(a.target);
            Type srctype = typeOf(a.source, tm);
            if (ttype != srctype) {
                if (ttype == Type.FLOAT)
                    check( srctype == Type.INT, "type 오류" + a.target);
                else if (ttype == Type.INT)
                    check( srctype == Type.CHAR, "type 오류" + a.target);
                else
                    check( false, "type끼리 계산 불가 " + a.target);
            }
            return;
        } 
        if (s instanceof Conditional){
            Conditional a = (Conditional)s;
            Type testType = typeOf (a.test, tm);
            check((testType == Type.BOOL),
                "boolean이 아님");
            V (a.thenbranch, tm);
            V (a.elsebranch, tm);
            return;
        }
    
         if (s instanceof Loop) {
            Loop a = (Loop)s;
            Type testType = typeOf (a.test, tm);
            check(	(testType == Type.BOOL),
                "boolean이 아님 ");	
            V (a.body, tm);
            return ;
        }
    
        if (s instanceof Block){
            Block a = (Block) s;
            Iterator statements = a.members.iterator();
                while (statements.hasNext())
                {
                Statement currentStatement = (Statement)statements.next();
                V (currentStatement ,tm);	
                }
            return ;
        }
    }
    public static void V (Expression e, TypeMap tm) {
        if (e instanceof Value) 
            return;
        if (e instanceof Variable) { 
            Variable v = (Variable)e;
            check( tm.containsKey(v), "변수가 정의되지 않음" + v);
            return;
        }
        if (e instanceof Binary) {
            Binary b = (Binary) e;
            Type typ1 = typeOf(b.term1, tm);
            Type typ2 = typeOf(b.term2, tm);
            V (b.term1, tm);
            V (b.term2, tm);
            if (b.op.ArithmeticOp( ))  
                check( typ1 == typ2 &&(typ1 == Type.INT || typ1 == Type.FLOAT), "tpye을 못찾음" + b.op);
            else if (b.op.RelationalOp( )) 
                check( typ1 == typ2 , "type error" + b.op);
            else if (b.op.BooleanOp( )) 
                check( typ1 == Type.BOOL && typ2 == Type.BOOL, b.op + "boolop가 아님");
            return;
        }

        if (e instanceof Unary){
            Unary u = (Unary) e;
            Type type = typeOf (u.term , tm);
            V (u.term, tm);
            if (u.op.NotOp())
                check ((type == Type.BOOL) ,"boolean이 아님");
            if (u.op.NegateOp())
                check ((type == Type.INT || type == Type.FLOAT),"숫자가 아님");
            return ;
        }
    }
    public static void main(String args[]) {
        Parser parser  = new Parser(new Lexer(args[0]));
        Program prog = parser.program();          
        System.out.println("Type map:");
        TypeMap map = typing(prog.decpart);
        map.display();   
        V(prog);
    }
}
