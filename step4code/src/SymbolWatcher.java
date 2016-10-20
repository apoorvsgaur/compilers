import java.util.HashMap;
import java.util.Map;

/**
 * Created by brandonscheller on 10/20/16
 */
public class SymbolWatcher {
    SymbolTable global = new SymbolTable("GLOBAL");
    Map<String,String> variableMap = new HashMap<>();
    void addSymbol(String name, String type, String value){
        global.addSymbol(name,type,value);
        variableMap.put(name,type);
    }
    void addTable(SymbolTable table){
        global.addTable(table);
    }
    SymbolTable getGlobal(){
        return global;
    }
    void print(){
        global.print();
    }
    String checkType(String variableName){
        return variableMap.get(variableName);
    }
}
