
import java.util.ArrayList;
import java.util.List;

/**
 * Created by brandonscheller on 10/3/16
 */
public class SymbolTable{

    String scopeName;
    ArrayList symbolList = new ArrayList();


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
        for( Object item : symbolList){
            if (item.getClass()==Symbol.class){
                if (((Symbol)item).name.equals(name)){
                    throw new RuntimeException("DECLARATION ERROR "+ name);
                }
            }
        }
        if (value == null)
            symbolList.add(new Symbol(name, type));
        else
            symbolList.add(new Symbol(name, type, value));
    }

    void addTable(SymbolTable table){
        symbolList.add(table);
    }

    private class Function {
        List<Symbol> symbolList = new ArrayList<>();
        List<Block> blockList = new ArrayList<>();
    }

    private class Block {
        List<Symbol> symbolList = new ArrayList<>();
        List<Block> blockList = new ArrayList<>();
    }

    private class Symbol {
        String name;
        String type;
        String value = null;

        //Symbol with value;
        Symbol( String name, String type, String value){
            this.name = name;
            this.type = type;
            this.value = value;
        }
        //Symbol without value
        Symbol( String name, String type){
            this.name = name;
            this.type = type;
        }

        void print(){
            if ( value == null)
                System.out.println("name " + name + " type " + type);
            else
                System.out.println("name " + name + " type " + type + " value " + value);
        }
    }
}


