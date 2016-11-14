import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by brandonscheller on 10/20/16
 */
@SuppressWarnings("ALL")
public class NodeConverter {
    private ArrayList<Node> nodeList = new ArrayList<>(); //nodeList contains IR code
    SymbolWatcher global; //SymbolWatcher contains all symbol tables

    NodeConverter(SymbolWatcher g){
        this.global = g;
    }

    void addNode(Node node){
        nodeList.add(node);
    }

    void addNodes(ArrayList<Node> nodeList){

        this.nodeList.addAll(nodeList);
    }

    void print(){
        //System.out.println(";IR code");
        //nodeList.forEach(Node::print);
        //System.out.println(";tiny code");
        //convertNodes();
        //lineList.forEach(s -> System.out.println(s));
    }

    ArrayList<String> lineList = new ArrayList<>(); //Linelist contains eventual tinycode
    Integer regCount = 0;
    String refReg;
    void convertNodes(){
        global.variableMap.keySet().forEach(s -> lineList.add("var "+s));
        Map<String,String> tempMap = new HashMap<>();
        for(Node node: nodeList){
            String val1, val2, target, temp;
            target = node.result;
            temp = "r"+regCount;
            switch (node.op){
                case "STOREI":
                case "STOREF"://lineList.add("move "+temp+" "+node.result);
                    val1 = "";
                    if (checkType(node.operand1).equals("INT") || checkType(node.operand1).equals("FLOAT")) {
                        lineList.add("move " + node.operand1 + " " + temp);
                        val1 = temp;
                        regCount++;
                    }   else if (!node.operand1.startsWith("$")) {
                        lineList.add("move " + node.operand1 + " " + temp);
                        val1 = temp;
                        regCount++;
                        tempMap.put(target, temp);
                    }   else if (node.operand1.startsWith("$")){
                        val1 = tempMap.get(node.operand1);
                    }
                    lineList.add("move "+val1+" "+target);
                    break;
                case "WRITEI": lineList.add("sys writei "+ node.result);
                    break;
                case "WRITEF": lineList.add("sys writer "+ node.result);
                    break;
                case "F*":
                    val1 = "";
                    val2 = "";
                    target = node.result;
                    temp = "r" + regCount;
                    if( node.operand1 != null) {
                        if (checkType(node.operand1).equals("INT") || checkType(node.operand1).equals("FLOAT")) {
                            lineList.add("move " + node.operand1 + " " + temp);
                            val1 = temp;
                            regCount++;
                            temp = "r"+regCount;

                        }   else if (!node.operand1.startsWith("$")) {
                            lineList.add("move " + node.operand1 + " " + temp);
                            val1 = temp;
                            regCount++;
                            temp = "r"+regCount;
                        }   else if (node.operand1.startsWith("$")){
                            val1 = tempMap.get(node.operand1);
                        }
                    }
                    if( node.operand2 != null){
                        if (checkType(node.operand2).equals("INT") || checkType(node.operand2).equals("FLOAT")) {
                            lineList.add("move " + node.operand2 + " " + temp);
                            val2 = temp;
                            regCount++;
                        } else if (!node.operand2.startsWith("$")) {
                            lineList.add("move " + node.operand2 + " " + temp);
                            val2 = temp;
                            regCount++;
                        } else if (node.operand2.startsWith("$")){
                            val2 = tempMap.get(node.operand2);
                        }
                    }
                    tempMap.put(target,val2);
                    lineList.add("mulr "+val1+" "+val2);
                    break;
                case "F/": //lineList.add("divr "+val1+" "+val2);
                    val1 = "";
                    val2 = "";
                    target = node.result;
                    temp = "r" + regCount;
                    if( node.operand1 != null) {
                        if (checkType(node.operand1).equals("INT") || checkType(node.operand1).equals("FLOAT")) {
                            lineList.add("move " + node.operand1 + " " + temp);
                            val1 = temp;
                            regCount++;
                            temp = "r"+regCount;

                        }   else if (!node.operand1.startsWith("$")) {
                            lineList.add("move " + node.operand1 + " " + temp);
                            val1 = temp;
                            regCount++;
                            temp = "r"+regCount;
                        }   else if (node.operand1.startsWith("$")){
                            val1 = tempMap.get(node.operand1);
                        }
                    }
                    if( node.operand2 != null){
                        if (checkType(node.operand2).equals("INT") || checkType(node.operand2).equals("FLOAT")) {
                            lineList.add("move " + node.operand2 + " " + temp);
                            val2 = temp;
                            regCount++;

                        } else if (!node.operand2.startsWith("$")) {
                            lineList.add("move " + node.operand2 + " " + temp);
                            val2 = temp;
                            regCount++;
                        } else if (node.operand2.startsWith("$")){
                            val2 = tempMap.get(node.operand2);
                        }
                    }
                    tempMap.put(target,val1);
                    lineList.add("divr "+val2+" "+val1);
                    break;
                case "F+": //lineList.add("addr "+val1+" "+val2);
                    val1 = "";
                    val2 = "";
                    target = node.result;
                    temp = "r" + regCount;
                    if( node.operand1 != null) {
                        if (checkType(node.operand1).equals("INT") || checkType(node.operand1).equals("FLOAT")) {
                            lineList.add("move " + node.operand1 + " " + temp);
                            val1 = temp;
                            regCount++;
                            temp = "r"+regCount;

                        }   else if (!node.operand1.startsWith("$")) {
                            lineList.add("move " + node.operand1 + " " + temp);
                            val1 = temp;
                            regCount++;
                            temp = "r"+regCount;
                        }   else if (node.operand1.startsWith("$")){
                            val1 = tempMap.get(node.operand1);
                        }
                    }
                    if( node.operand2 != null){
                        if (checkType(node.operand2).equals("INT") || checkType(node.operand2).equals("FLOAT")) {
                            lineList.add("move " + node.operand2 + " " + temp);
                            val2 = temp;
                            regCount++;

                        } else if (!node.operand2.startsWith("$")) {
                            lineList.add("move " + node.operand2 + " " + temp);
                            val2 = temp;
                            regCount++;
                        } else if (node.operand2.startsWith("$")){
                            val2 = tempMap.get(node.operand2);
                        }
                    }
                    tempMap.put(target,val2);
                    lineList.add("addr "+val1+" "+val2);
                    break;
                case "F-": //lineList.add("subr "+val1+" "+val2);
                    val1 = "";
                    val2 = "";
                    target = node.result;
                    temp = "r" + regCount;
                    if( node.operand1 != null) {
                        if (checkType(node.operand1).equals("INT") || checkType(node.operand1).equals("FLOAT")) {
                            lineList.add("move " + node.operand1 + " " + temp);
                            val1 = temp;
                            regCount++;
                            temp = "r"+regCount;

                        }   else if (!node.operand1.startsWith("$")) {
                            lineList.add("move " + node.operand1 + " " + temp);
                            val1 = temp;
                            regCount++;
                            temp = "r"+regCount;
                        }   else if (node.operand1.startsWith("$")){
                            val1 = tempMap.get(node.operand1);
                        }
                    }
                    if( node.operand2 != null){
                        if (checkType(node.operand2).equals("INT") || checkType(node.operand2).equals("FLOAT")) {
                            lineList.add("move " + node.operand2 + " " + temp);
                            val2 = temp;
                            regCount++;
                        } else if (!node.operand2.startsWith("$")) {
                            lineList.add("move " + node.operand2 + " " + temp);
                            val2 = temp;
                            regCount++;
                        } else if (node.operand2.startsWith("$")){
                            val2 = tempMap.get(node.operand2);
                        }
                    }
                    tempMap.put(target,val1);
                    lineList.add("subr "+val2+" "+val1);
                    break;
                case "I*":
                    val1 = "";
                    val2 = "";
                    target = node.result;
                    temp = "r" + regCount;
                    if( node.operand1 != null) {
                        if (checkType(node.operand1).equals("INT") || checkType(node.operand1).equals("FLOAT")) {
                            lineList.add("move " + node.operand1 + " " + temp);
                            val1 = temp;
                            regCount++;
                            temp = "r"+regCount;

                        }   else if (!node.operand1.startsWith("$")) {
                            lineList.add("move " + node.operand1 + " " + temp);
                            val1 = temp;
                            regCount++;
                            temp = "r"+regCount;
                        }   else if (node.operand1.startsWith("$")){
                            val1 = tempMap.get(node.operand1);
                        }
                    }
                    if( node.operand2 != null){
                        if (checkType(node.operand2).equals("INT") || checkType(node.operand2).equals("FLOAT")) {
                            lineList.add("move " + node.operand2 + " " + temp);
                            val2 = temp;
                            regCount++;
                        } else if (!node.operand2.startsWith("$")) {
                            lineList.add("move " + node.operand2 + " " + temp);
                            val2 = temp;
                            regCount++;
                        } else if (node.operand2.startsWith("$")){
                            val2 = tempMap.get(node.operand2);
                        }
                    }
                    tempMap.put(target,val2);
                    lineList.add("muli "+val1+" "+val2);
                    break;
                case "I/":
                    val1 = "";
                    val2 = "";
                    target = node.result;
                    temp = "r" + regCount;
                    if( node.operand1 != null) {
                        if (checkType(node.operand1).equals("INT") || checkType(node.operand1).equals("FLOAT")) {
                            lineList.add("move " + node.operand1 + " " + temp);
                            val1 = temp;
                            regCount++;
                            temp = "r"+regCount;

                        }   else if (!node.operand1.startsWith("$")) {
                            lineList.add("move " + node.operand1 + " " + temp);
                            val1 = temp;
                            regCount++;
                            temp = "r"+regCount;
                        }   else if (node.operand1.startsWith("$")){
                            val1 = tempMap.get(node.operand1);
                        }
                    }
                    if( node.operand2 != null){
                        if (checkType(node.operand2).equals("INT") || checkType(node.operand2).equals("FLOAT")) {
                            lineList.add("move " + node.operand2 + " " + temp);
                            val2 = temp;
                            regCount++;

                        } else if (!node.operand2.startsWith("$")) {
                            lineList.add("move " + node.operand2 + " " + temp);
                            val2 = temp;
                            regCount++;
                        } else if (node.operand2.startsWith("$")){
                            val2 = tempMap.get(node.operand2);
                        }
                    }
                    tempMap.put(target,val1);

                    lineList.add("divi "+val2+" "+val1);
                    break;
                case "I+":
                    val1 = "";
                    val2 = "";
                    target = node.result;
                    temp = "r" + regCount;
                    if( node.operand1 != null) {
                        if (checkType(node.operand1).equals("INT") || checkType(node.operand1).equals("FLOAT")) {
                            lineList.add("move " + node.operand1 + " " + temp);
                            val1 = temp;
                            regCount++;
                            temp = "r"+regCount;

                        }   else if (!node.operand1.startsWith("$")) {
                            lineList.add("move " + node.operand1 + " " + temp);
                            val1 = temp;
                            regCount++;
                            temp = "r"+regCount;
                        }   else if (node.operand1.startsWith("$")){
                            val1 = tempMap.get(node.operand1);
                        }
                    }
                    if( node.operand2 != null){
                        if (checkType(node.operand2).equals("INT") || checkType(node.operand2).equals("FLOAT")) {
                            lineList.add("move " + node.operand2 + " " + temp);
                            val2 = temp;
                            regCount++;

                        } else if (!node.operand2.startsWith("$")) {
                            lineList.add("move " + node.operand2 + " " + temp);
                            val2 = temp;
                            regCount++;
                        } else if (node.operand2.startsWith("$")){
                            val2 = tempMap.get(node.operand2);
                        }
                    }
                    tempMap.put(target,val2);
                    lineList.add("addi "+val1+" "+val2);
                    break;
                case "I-":
                    val1 = "";
                    val2 = "";
                    target = node.result;
                    temp = "r" + regCount;
                    if( node.operand1 != null) {
                        if (checkType(node.operand1).equals("INT") || checkType(node.operand1).equals("FLOAT")) {
                            lineList.add("move " + node.operand1 + " " + temp);
                            val1 = temp;
                            regCount++;
                            temp = "r"+regCount;

                        }   else if (!node.operand1.startsWith("$")) {
                            lineList.add("move " + node.operand1 + " " + temp);
                            val1 = temp;
                            regCount++;
                            temp = "r"+regCount;
                        }   else if (node.operand1.startsWith("$")){
                            val1 = tempMap.get(node.operand1);
                        }
                    }
                    if( node.operand2 != null){
                        if (checkType(node.operand2).equals("INT") || checkType(node.operand2).equals("FLOAT")) {
                            lineList.add("move " + node.operand2 + " " + temp);
                            val2 = temp;
                            regCount++;
                        } else if (!node.operand2.startsWith("$")) {
                            lineList.add("move " + node.operand2 + " " + temp);
                            val2 = temp;
                            regCount++;
                        } else if (node.operand2.startsWith("$")){
                            val2 = tempMap.get(node.operand2);
                        }
                    }
                    tempMap.put(target,val1);
                    lineList.add("subi "+val2+" "+val1);
                    break;
                default: throw new RuntimeException();
            }
        }
        lineList.add("sys halt");
    }

    public String checkType(String value){
        try{
            int integer = Integer.parseInt(value);
            return "INT";
        }
        catch(Exception e){
        }
        try{
            float floatnum = Float.parseFloat(value);
            return "FLOAT";
        }
        catch(Exception e){
        }
        return "";
    }


}
