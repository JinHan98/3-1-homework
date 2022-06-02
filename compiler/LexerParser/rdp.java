import java.util.List;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class rdp {
    public static void main(String[] args) throws IOException {
        Path path = Paths.get("/Users/gwonjinhan/Desktop/p2.cl");
        Charset cs = StandardCharsets.UTF_8;
        Lexer lexer;
        //파일 내용담을 리스트
        List<String> list = new ArrayList<String>();
        try{
            list = Files.readAllLines(path,cs);
        }catch(IOException e){
            e.printStackTrace();
        }
        for(String readLine : list){
            lexer=new Lexer(readLine);
        }
    }   
}
