
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Created by brandonscheller on 10/3/16
 */
public class ScopeTable{

    String scopeName;
    HashMap<String,Symbol> symbolMap = new HashMap<>();
    HashMap<String,String> tempMap = new HashMap<>();
    Integer count = 1;
    Integer Pcount = 1;

    Integer variableCount = 0;

    ScopeTable(String scopeName){
        this.scopeName = scopeName;
    }

    HashMap<String,Symbol> getSymbols(){
        return symbolMap;
    }

    void print(){
        System.out.println("Symbol table " + scopeName);
        for( Symbol item : symbolMap.values()){
            item.print();
        }
        System.out.println();
    }

    Integer getVariableCount(){
        return variableCount;
    }

    void addSymbols(HashMap<String,Symbol> symbolMap){
        this.symbolMap.putAll(symbolMap);
    }

    void addLocalSymbol(String name, String type, String value){
        variableCount++;
        tempMap.put(name,"$L"+count);
        count++;
        for( String item : symbolMap.keySet()){
            if (item.equals(name)){
                throw new RuntimeException("DECLARATION ERROR "+ name);
            }
        }
        if (value == null) {
            Symbol symbol = new Symbol(name, type);
            symbolMap.put(name,symbol);
        }
        else {
            Symbol symbol = new Symbol(name, type, value);
            symbolMap.put(name,symbol);
        }
    }

    void addParamSymbol(String name, String type, String value){
        tempMap.put(name,"$P"+Pcount);
        Pcount++;
        for( String item : symbolMap.keySet()){
            if (item.equals(name)){
                throw new RuntimeException("DECLARATION ERROR "+ name);
            }
        }
        if (value == null) {
            Symbol symbol = new Symbol(name, type);
            symbolMap.put(name,symbol);
        }
        else {
            Symbol symbol = new Symbol(name, type, value);
            symbolMap.put(name,symbol);
        }
    }

    String checkType(String variableName){
        return symbolMap.get(variableName).type;
    }

    String checkValue(String variableName){
        return symbolMap.get(variableName).value;
    }

    String checkTempVar(String variableName){
        return tempMap.get(variableName);
    }
}


