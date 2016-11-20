import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Created by brandonscheller on 10/20/16
 */
public class SymbolWatcher {

    Stack<ScopeTable> pointerStack = new Stack<>();

    Map<String, ScopeTable> scopeMap = new HashMap<>();

    ArrayList<String> functionList = new ArrayList<>();

    String currentScope;
    String currentFunction;

    SymbolWatcher() {
        addScope("GLOBAL");
    }

    void addSymbol(String name, String type, String value) {
        scopeMap.get(currentScope).addLocalSymbol(name, type, value);
    }

    void addParamSymbol(String name, String type, String value) {
        scopeMap.get(currentScope).addParamSymbol(name, type, value);
    }

    void addScope(String name) {
        if( !name.startsWith("BLOCK")){
            currentFunction = name;
        }
        currentScope = name;
        ScopeTable newTable = new ScopeTable(name);
        for (ScopeTable scope : pointerStack) {
            newTable.addSymbols(scope.getSymbols());
        }
        pointerStack.push(newTable);
        scopeMap.put(name, newTable);
    }

    void popScope() {
        pointerStack.pop();
        currentScope = pointerStack.peek().scopeName;
    }

    ScopeTable getGlobal() {
        return scopeMap.get("GLOBAL");
    }

    void print() {
        for (ScopeTable table : scopeMap.values()) {
            table.print();
        }
    }

    String checkType(String variableName) {
        return scopeMap.get(currentScope).checkType(variableName);
    }

    String checkValue(String variableName) {
        return scopeMap.get(currentScope).checkValue(variableName);
    }

    String checkTempVar(String variableName) {
        if (null !=scopeMap.get(currentScope).checkTempVar(variableName)){
            return scopeMap.get(currentScope).checkTempVar(variableName);
        } else if (null !=scopeMap.get(currentFunction).checkTempVar(variableName)){
            return scopeMap.get(currentFunction).checkTempVar(variableName);
        } else {
            return null;
        }

    }

    Integer getPCount(String scope) {
        return scopeMap.get(scope).Pcount;
    }

    Integer getVarCount(String scope) {
        return scopeMap.get(scope).variableCount;
    }
}
