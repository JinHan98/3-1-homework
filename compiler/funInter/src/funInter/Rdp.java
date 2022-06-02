package funInter;
import java.io.*;

public class Rdp {
    static Parser parser;
    public static void main(String[] args) throws IOException{
        parser = new Parser(new Lexer("p2.cl"));
        Program prog = parser.program();
        String s = prog.display();
        FileOutputStream outst = new FileOutputStream("output.txt", false);
        OutputStreamWriter wr = new OutputStreamWriter(outst, "UTF-8");
        BufferedWriter out = new BufferedWriter(wr);
        out.write(s);
        out.close();
    }
}