
import java.util.ArrayList;
import java.util.List;

/**
 * Created by brandonscheller on 10/3/16
 */
public class SymbolTable{

    String scopeName;
    ArrayList<Symbol> symbolList = new ArrayList<>();
    ArrayList<SymbolTable> childrenTable = new ArrayList<>();

    SymbolTable(String scopeName){
        this.scopeName = scopeName;
    }

    void print(){
        System.out.println("Symbol table " + scopeName);
        for( Object item : symbolList){
            if (item.getClass() == SymbolTable.class){
                System.out.println("");
                ((SymbolTable) item).print();
            } else if (item.getClass()==Symbol.class){
                ((Symbol) item).print();
            }
        }
    }

    void addSymbol(String name, String type, String value){
        for( Symbol item : symbolList){
            if (item.name.equals(name)){
                throw new RuntimeException("DECLARATION ERROR "+ name);
            }
        }
        if (value == null) {
            Symbol symbol = new Symbol(name, type);
            symbolList.add(symbol);
        }
        else {
            Symbol symbol = new Symbol(name, type, value);
            symbolList.add(symbol);
        }
    }

    void addTable(SymbolTable table){
        childrenTable.add(table);
    }

}


