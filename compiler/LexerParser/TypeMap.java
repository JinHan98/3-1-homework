import java.util.*;

public class TypeMap extends HashMap<Variable,Type>{
    void display() {
        //Iterator tm = values.iterator()
            
        //	System.out.println("type map display");
        Iterator types = keySet().iterator();
        Iterator variables = values().iterator();
        System.out.print ("{");
        while (types.hasNext() && variables.hasNext())
            {	
            System.out.print ("< " + types.next() +"," + variables.next() + " >,");	
            }
        System.out.println ( "}");
    }
}