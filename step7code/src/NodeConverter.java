import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by brandonscheller on 10/20/16
 */
public class NodeConverter {
    ArrayList<Node> nodeList = new ArrayList<>();
    SymbolWatcher global;
    ArrayList<String> lineList = new ArrayList<>();
    Integer regCount = 0;
    String refReg;
    String currentFunction;

    Integer pushPopper = 6;
    Integer tempVarCount = 1;
    boolean isInt;

    NodeConverter(SymbolWatcher g){
        this.global = g;
    }
    void addNode( Node node ){
        nodeList.add(node);
    }

    void addNodes(ArrayList<Node> nodeList){
        this.nodeList.addAll(nodeList);
    }

    void addVarNum(Integer varNum){
        tempVarCount = varNum;
    }

    void generateTempVars(){
        for( int i = 1; i < tempVarCount; i++ ){
            lineList.add(0,"var TempVar"+ i);
        }
    }

    void print(){
        System.out.println(";IR code");
        for( Node theNode : nodeList){
            theNode.print();
        }
        nodeList.forEach(Node::print);
        System.out.println(";tiny code");
        convertNodes();
        generateTempVars();
        for(String s : lineList) {
            String hold = s.replace("$T", "TempVar");
            System.out.println(hold);
        }
    }

    void convertNodes(){
        //global.variableMap.keySet().forEach(s -> lineList.add("var "+s));
        for(Symbol item : global.getGlobal().symbolMap.values() ){
            if(item.type.equals("STRING")){
                lineList.add("str " + item.name + " " + item.value );
            } else if(item.type.equals("INT")){
                lineList.add("var " + item.name);
            } else if(item.type.equals("FLOAT")) {
                lineList.add("var " + item.name);
            } else {
                throw new RuntimeException();
            }
        }
        lineList.add("push");
        lineList.add("push r0");
        lineList.add("push r1");
        lineList.add("push r2");
        lineList.add("push r3");
        lineList.add("jsr main");
        lineList.add("sys halt");
        Map<String,String> tempMap = new HashMap<>();
        for(Node node: nodeList){
            String val1, val2, target, temp, temp2;
            target = node.result;
            if (target != null && target.startsWith("$L")) {
                target = "$-" + node.result.substring(2);
            }
            if (node.operand1 != null && node.operand1.startsWith("$L")) {
                node.operand1 = "$-" + node.operand1.substring(2);
            }
            if (node.operand2 != null && node.operand2.startsWith("$L")) {
                node.operand2 = "$-" + node.operand2.substring(2);
            }
            temp = "r"+regCount;
            if( node.op != null) {
                switch (node.op) {
                    case "READI":
                            lineList.add("sys readi "+target);
                        break;
                    case "READF":
                            lineList.add("sys readr "+target);
                        break;
                    case "LINK":
                            lineList.add("link "+ global.getVarCount(currentFunction));
                        break;
                    case "PUSH":
                        pushPopper++;
                        if (node.result == null) {
                            lineList.add("push");
                        } else {
                            if(node.result.startsWith("$T")){
                                lineList.add("move " + node.result + " r0");
                                lineList.add("push r0");
                            } else if (node.result.startsWith("$L")){
                                lineList.add("push $-" + node.result.substring(2));
                            } else {
                                lineList.add("push " + node.result);
                            }
                        }
                        break;
                    case "POP":
                        pushPopper--;
                        if (node.result == null) {
                            lineList.add("pop");
                        } else {
                            tempMap.put(node.result,temp);
                            lineList.add("pop r0");
                            lineList.add("move r0 "+node.result);
                            regCount++;
                        }
                        break;
                    case "JSR":
                        lineList.add("push r0");
                        lineList.add("push r1");
                        lineList.add("push r2");
                        lineList.add("push r3");
                        lineList.add("jsr " + node.result);
                        lineList.add("pop r3");
                        lineList.add("pop r2");
                        lineList.add("pop r1");
                        lineList.add("pop r0");
                        break;
                    case "RET":
                        lineList.add("unlnk");
                        lineList.add("ret");
                        break;
                    case "STOREI":
                    case "STOREF"://lineList.add("move "+temp+" "+node.result);
                        val1 = "";
                        if (checkType(node.operand1).equals("INT") || checkType(node.operand1).equals("FLOAT")) {
                            if(node.operand1.startsWith("$L")) {
                                lineList.add("move $-" + node.operand1.substring(2) + " r3");
                            } else {
                                lineList.add("move " + node.operand1 + " r3");
                            }
                            val1 = temp;
                            regCount++;
                        } else if (!node.operand1.startsWith("$T")) {
                            if(node.operand1.startsWith("$L")) {
                                lineList.add("move $-" + node.operand1.substring(2) + " r3");
                            } else {
                                lineList.add("move " + node.operand1 + " r3");
                            }
                            val1 = temp;
                            regCount++;
                            tempMap.put(target, temp);
                        } else if (node.operand1.startsWith("$T")) {
                            lineList.add("move " + node.operand1 + " r3");
                            val1 = node.operand1;
                        }
                        if(target != null && target.equals("$R")){
                            target = "$"+(5+global.getPCount(currentFunction));
                        }
                        val1 = "r3";
                        lineList.add("move " + val1 + " " + target);
                        break;
                    case "WRITEI":
                        if( node.result.startsWith("$P")) {
                            lineList.add("sys writei $" + (5+global.getPCount(currentFunction)));
                        } else if( node.result.startsWith("$L")){
                            lineList.add("sys writei $-" + node.result.substring(2));
                        } else {
                            lineList.add("sys writei " + node.result);
                        }
                        break;
                    case "WRITEF":
                        if( node.result.startsWith("$P")) {
                            lineList.add("sys writei $" + (5+global.getPCount(currentFunction)));
                        } else if( node.result.startsWith("$L")){
                            lineList.add("sys writer $-" + node.result.substring(2));
                        } else {
                            lineList.add("sys writer " + node.result);
                        }
                        break;
                    case "WRITES":
                        lineList.add("sys writes " + node.result);
                        break;
                    case "F*":
                        val1 = "";
                        val2 = "";
                        target = node.result;
                        temp = "r" + regCount;
                        if (node.operand1 != null) {
                            if (checkType(node.operand1).equals("INT") || checkType(node.operand1).equals("FLOAT")) {
                                if(node.operand1.startsWith("$P")) {
                                    lineList.add("move $" + (5+Integer.parseInt(node.operand1.substring(2))) + " r1");
                                } else {
                                    lineList.add("move " + node.operand1 + " r1");
                                }
                                val1 = "r1";
                                regCount++;
                                temp = "r" + regCount;

                            } else if (!node.operand1.startsWith("$T")) {
                                if(node.operand1.startsWith("$P")) {
                                    lineList.add("move $" + (5+Integer.parseInt(node.operand1.substring(2))) + " r1");
                                } else {
                                    lineList.add("move " + node.operand1 + " r1");
                                }
                                val1 = "r1";
                                regCount++;
                                temp = "r" + regCount;
                            } else if (node.operand1.startsWith("$T")) {
                                lineList.add("move " + node.operand1 + " r1");
                                val1 = "r1";// tempMap.get(node.operand1);
                            }
                        }
                        if (node.operand2 != null) {
                            if (checkType(node.operand2).equals("INT") || checkType(node.operand2).equals("FLOAT")) {
                                if(node.operand2.startsWith("$P")) {
                                    lineList.add("move $" + (5+Integer.parseInt(node.operand2.substring(2))) + " r2");
                                } else {
                                    lineList.add("move " + node.operand2 + " r2");
                                }
                                val2 = "r2";
                                regCount++;
                            } else if (!node.operand2.startsWith("$T")) {
                                if(node.operand2.startsWith("$P")) {
                                    lineList.add("move $" + (5+Integer.parseInt(node.operand2.substring(2))) + " r2");
                                } else {
                                    lineList.add("move " + node.operand2 + " r2");
                                }
                                val2 = "r2";
                                regCount++;
                            } else if (node.operand2.startsWith("$T")) {
                                lineList.add("move " + node.operand2 + " r2");
                                val2 = "r2";// tempMap.get(node.operand2);
                            }
                        }
                        //"POOP" REMEMBER TO CHANGE TEMP MAP AND TARGET
                        lineList.add("mulr " + val1 + " " + val2);
                        lineList.add("move " + val2 + " " + target);
                        break;
                    case "F/": //lineList.add("divr "+val1+" "+val2);
                        val1 = "";
                        val2 = "";
                        target = node.result;
                        temp = "r" + regCount;
                        if (node.operand1 != null) {
                            if (checkType(node.operand1).equals("INT") || checkType(node.operand1).equals("FLOAT")) {
                                if(node.operand1.startsWith("$P")) {
                                    lineList.add("move $" + (5+Integer.parseInt(node.operand1.substring(2))) + " r1");
                                } else {
                                    lineList.add("move " + node.operand1 + " r1");
                                }
                                val1 = "r1";
                                regCount++;
                                temp = "r" + regCount;

                            } else if (!node.operand1.startsWith("$T")) {
                                if(node.operand1.startsWith("$P")) {
                                    lineList.add("move $" + (5+Integer.parseInt(node.operand1.substring(2))) + " r1");
                                } else {
                                    lineList.add("move " + node.operand1 + " r1");
                                }
                                val1 = "r1";
                                regCount++;
                                temp = "r" + regCount;
                            } else if (node.operand1.startsWith("$T")) {
                                lineList.add("move " + node.operand1 + " r1");
                                val1 = "r1";
                                //val1 = tempMap.get(node.operand1);
                            }
                        }
                        if (node.operand2 != null) {
                            if (checkType(node.operand2).equals("INT") || checkType(node.operand2).equals("FLOAT")) {
                                if(node.operand2.startsWith("$P")) {
                                    lineList.add("move $" + (5+Integer.parseInt(node.operand2.substring(2))) + " r2");
                                } else {
                                    lineList.add("move " + node.operand2 + " r2");
                                }
                                val2 = "r2";
                                regCount++;
                            } else if (!node.operand2.startsWith("$T")) {
                                if(node.operand2.startsWith("$P")) {
                                    lineList.add("move $" + (5+Integer.parseInt(node.operand2.substring(2))) + " r2");
                                } else {
                                    lineList.add("move " + node.operand2 + " r2");
                                }
                                val2 = "r2";
                                regCount++;
                            } else if (node.operand2.startsWith("$T")) {
                                lineList.add("move " + node.operand2 + " r2");
                                val2 = "r2";
                            }
                        }
                        tempMap.put(target, val1);
                        lineList.add("divr " + val2 + " " + val1);
                        lineList.add("move " + val1 + " " + target);
                        break;
                    case "F+": //lineList.add("addr "+val1+" "+val2);
                        val1 = "";
                        val2 = "";
                        target = node.result;
                        temp = "r" + regCount;
                        if (node.operand1 != null) {
                            if (checkType(node.operand1).equals("INT") || checkType(node.operand1).equals("FLOAT")) {
                                if(node.operand1.startsWith("$P")) {
                                    lineList.add("move $" + (5+Integer.parseInt(node.operand1.substring(2))) + " r1");
                                } else {
                                    lineList.add("move " + node.operand1 + " r1");
                                }
                                val1 = "r1";
                                regCount++;
                                temp = "r" + regCount;

                            } else if (!node.operand1.startsWith("$T")) {
                                if(node.operand1.startsWith("$P")) {
                                    lineList.add("move $" + (5+Integer.parseInt(node.operand1.substring(2))) + " r1");
                                } else {
                                    lineList.add("move " + node.operand1 + " r1");
                                }
                                val1 = "r1";
                                regCount++;
                                temp = "r" + regCount;
                            } else if (node.operand1.startsWith("$T")) {
                                lineList.add("move " + node.operand1 + " r1");
                                val1 = "r1";// tempMap.get(node.operand1);
                            }
                        }
                        if (node.operand2 != null) {
                            if (checkType(node.operand2).equals("INT") || checkType(node.operand2).equals("FLOAT")) {
                                if(node.operand2.startsWith("$P")) {
                                    lineList.add("move $" + (5+Integer.parseInt(node.operand2.substring(2))) + " r2");
                                } else {
                                    lineList.add("move " + node.operand2 + " r2");
                                }
                                val2 = "r2";
                                regCount++;
                            } else if (!node.operand2.startsWith("$T")) {
                                if(node.operand2.startsWith("$P")) {
                                    lineList.add("move $" + (5+Integer.parseInt(node.operand2.substring(2))) + " r2");
                                } else {
                                    lineList.add("move " + node.operand2 + " r2");
                                }
                                val2 = "r2";
                                regCount++;
                            } else if (node.operand2.startsWith("$T")) {
                                lineList.add("move " + node.operand2 + " r2");
                                val2 = "r2";// tempMap.get(node.operand2);
                            }
                        }
                        lineList.add("addr " + val1 + " " + val2);
                        lineList.add("move " + val2 + " " + target);
                        break;
                    case "F-": //lineList.add("subr "+val1+" "+val2);
                        val1 = "";
                        val2 = "";
                        target = node.result;
                        temp = "r" + regCount;
                        if (node.operand1 != null) {
                            if (checkType(node.operand1).equals("INT") || checkType(node.operand1).equals("FLOAT")) {
                                if(node.operand1.startsWith("$P")) {
                                    lineList.add("move $" + (5+Integer.parseInt(node.operand1.substring(2))) + " r1");
                                } else {
                                    lineList.add("move " + node.operand1 + " r1");
                                }
                                val1 = "r1";
                                regCount++;
                                temp = "r" + regCount;

                            } else if (!node.operand1.startsWith("$T")) {
                                if(node.operand1.startsWith("$P")) {
                                    lineList.add("move $" + (5+Integer.parseInt(node.operand1.substring(2))) + " r1");
                                } else {
                                    lineList.add("move " + node.operand1 + " r1");
                                }
                                val1 = "r1";
                                regCount++;
                                temp = "r" + regCount;
                            } else if (node.operand1.startsWith("$T")) {
                                lineList.add("move " + node.operand1 + " r1");
                                val1 = "r1";
                                //val1 = tempMap.get(node.operand1);
                            }
                        }
                        if (node.operand2 != null) {
                            if (checkType(node.operand2).equals("INT") || checkType(node.operand2).equals("FLOAT")) {
                                if(node.operand2.startsWith("$P")) {
                                    lineList.add("move $" + (5+Integer.parseInt(node.operand2.substring(2))) + " r2");
                                } else {
                                    lineList.add("move " + node.operand2 + " r2");
                                }
                                val2 = "r2";
                                regCount++;
                            } else if (!node.operand2.startsWith("$T")) {
                                if(node.operand2.startsWith("$P")) {
                                    lineList.add("move $" + (5+Integer.parseInt(node.operand2.substring(2))) + " r2");
                                } else {
                                    lineList.add("move " + node.operand2 + " r2");
                                }
                                val2 = "r2";
                                regCount++;
                            } else if (node.operand2.startsWith("$T")) {
                                lineList.add("move " + node.operand2 + " r2");
                                val2 = "r2";
                            }
                        }
                        lineList.add("subr " + val2 + " " + val1);
                        lineList.add("move " + val1 + " " + target);
                        break;
                    case "I*":
                        val1 = "";
                        val2 = "";
                        target = node.result;
                        temp = "r" + regCount;
                        if (node.operand1 != null) {
                            if (checkType(node.operand1).equals("INT") || checkType(node.operand1).equals("FLOAT")) {
                                if(node.operand1.startsWith("$P")) {
                                    lineList.add("move $" + (5+Integer.parseInt(node.operand1.substring(2))) + " r1");
                                } else {
                                    lineList.add("move " + node.operand1 + " r1");
                                }
                                val1 = "r1";
                                regCount++;
                                temp = "r" + regCount;

                            } else if (!node.operand1.startsWith("$T")) {
                                if(node.operand1.startsWith("$P")) {
                                    lineList.add("move $" + (5+Integer.parseInt(node.operand1.substring(2))) + " r1");
                                } else {
                                    lineList.add("move " + node.operand1 + " r1");
                                }
                                val1 = "r1";
                                regCount++;
                                temp = "r" + regCount;
                            } else if (node.operand1.startsWith("$T")) {
                                lineList.add("move " + node.operand1 + " r1");
                                val1 = "r1";// tempMap.get(node.operand1);
                            }
                        }
                        if (node.operand2 != null) {
                            if (checkType(node.operand2).equals("INT") || checkType(node.operand2).equals("FLOAT")) {
                                if(node.operand2.startsWith("$P")) {
                                    lineList.add("move $" + (5+Integer.parseInt(node.operand2.substring(2))) + " r2");
                                } else {
                                    lineList.add("move " + node.operand2 + " r2");
                                }
                                val2 = "r2";
                                regCount++;
                            } else if (!node.operand2.startsWith("$T")) {
                                if(node.operand2.startsWith("$P")) {
                                    lineList.add("move $" + (5+Integer.parseInt(node.operand2.substring(2))) + " r2");
                                } else {
                                    lineList.add("move " + node.operand2 + " r2");
                                }
                                val2 = "r2";
                                regCount++;
                            } else if (node.operand2.startsWith("$T")) {
                                lineList.add("move " + node.operand2 + " r2");
                                val2 = "r2";// tempMap.get(node.operand2);
                            }
                        }
                        lineList.add("muli " + val1 + " " + val2);
                        lineList.add("move " + val2 + " " + target);
                        break;
                    case "I/":
                        val1 = "";
                        val2 = "";
                        target = node.result;
                        temp = "r" + regCount;
                        if (node.operand1 != null) {
                            if (checkType(node.operand1).equals("INT") || checkType(node.operand1).equals("FLOAT")) {
                                if(node.operand1.startsWith("$P")) {
                                    lineList.add("move $" + (5+Integer.parseInt(node.operand1.substring(2))) + " r1");
                                } else {
                                    lineList.add("move " + node.operand1 + " r1");
                                }
                                val1 = "r1";
                                regCount++;
                                temp = "r" + regCount;

                            } else if (!node.operand1.startsWith("$T")) {
                                if(node.operand1.startsWith("$P")) {
                                    lineList.add("move $" + (5+Integer.parseInt(node.operand1.substring(2))) + " r1");
                                } else {
                                    lineList.add("move " + node.operand1 + " r1");
                                }
                                val1 = "r1";
                                regCount++;
                                temp = "r" + regCount;
                            } else if (node.operand1.startsWith("$T")) {
                                lineList.add("move " + node.operand1 + " r1");
                                val1 = "r1";
                            }
                        }
                        if (node.operand2 != null) {
                            if (checkType(node.operand2).equals("INT") || checkType(node.operand2).equals("FLOAT")) {
                                if(node.operand2.startsWith("$P")) {
                                    lineList.add("move $" + (5+Integer.parseInt(node.operand2.substring(2))) + " r2");
                                } else {
                                    lineList.add("move " + node.operand2 + " r2");
                                }
                                val2 = "r2";
                                regCount++;
                            } else if (!node.operand2.startsWith("$T")) {
                                if(node.operand2.startsWith("$P")) {
                                    lineList.add("move $" + (5+Integer.parseInt(node.operand2.substring(2))) + " r2");
                                } else {
                                    lineList.add("move " + node.operand2 + " r2");
                                }
                                val2 = "r2";
                                regCount++;
                            } else if (node.operand2.startsWith("$T")) {
                                lineList.add("move " + node.operand2 + " r2");
                                val2 = "r2";
                            }
                        }
                        tempMap.put(target, val1);
                        lineList.add("divi " + val2 + " " + val1);
                        lineList.add("move " + val1 + " " + target);
                        break;
                    case "I+":
                        val1 = "";
                        val2 = "";
                        target = node.result;
                        temp = "r" + regCount;
                        if (node.operand1 != null) {
                            if (checkType(node.operand1).equals("INT") || checkType(node.operand1).equals("FLOAT")) {
                                if(node.operand1.startsWith("$P")) {
                                    lineList.add("move $" + (5+Integer.parseInt(node.operand1.substring(2))) + " r1");
                                } else {
                                    lineList.add("move " + node.operand1 + " r1");
                                }
                                val1 = "r1";
                                regCount++;
                                temp = "r" + regCount;

                            } else if (!node.operand1.startsWith("$T")) {
                                if(node.operand1.startsWith("$P")) {
                                    lineList.add("move $" + (5+Integer.parseInt(node.operand1.substring(2))) + " r1");
                                } else {
                                    lineList.add("move " + node.operand1 + " r1");
                                }
                                val1 = "r1";
                                regCount++;
                                temp = "r" + regCount;
                            } else if (node.operand1.startsWith("$T")) {
                                lineList.add("move " + node.operand1 + " r1");
                                val1 = "r1";// tempMap.get(node.operand1);
                            }
                        }
                        if (node.operand2 != null) {
                            if (checkType(node.operand2).equals("INT") || checkType(node.operand2).equals("FLOAT")) {
                                if(node.operand2.startsWith("$P")) {
                                    lineList.add("move $" + (5+Integer.parseInt(node.operand2.substring(2))) + " r2");
                                } else {
                                    lineList.add("move " + node.operand2 + " r2");
                                }
                                val2 = "r2";
                                regCount++;
                            } else if (!node.operand2.startsWith("$T")) {
                                if(node.operand2.startsWith("$P")) {
                                    lineList.add("move $" + (5+Integer.parseInt(node.operand2.substring(2))) + " r2");
                                } else {
                                    lineList.add("move " + node.operand2 + " r2");
                                }
                                val2 = "r2";
                                regCount++;
                            } else if (node.operand2.startsWith("$T")) {
                                lineList.add("move " + node.operand2 + " r2");
                                val2 = "r2";// tempMap.get(node.operand2);
                            }
                        }
                        //tempMap.put(target, val2);
                        lineList.add("addi " + val1 + " " + val2);
                        lineList.add("move " + val2 + " " + target);
                        break;
                    case "I-":
                        val1 = "";
                        val2 = "";
                        target = node.result;
                        temp = "r" + regCount;
                        if (node.operand1 != null) {
                            if (checkType(node.operand1).equals("INT") || checkType(node.operand1).equals("FLOAT")) {
                                if(node.operand1.startsWith("$P")) {
                                    lineList.add("move $" + (5+Integer.parseInt(node.operand1.substring(2))) + " r1");
                                } else {
                                    lineList.add("move " + node.operand1 + " r1");
                                }
                                val1 = "r1";
                                regCount++;
                                temp = "r" + regCount;

                            } else if (!node.operand1.startsWith("$T")) {
                                if(node.operand1.startsWith("$P")) {
                                    lineList.add("move $" + (5+Integer.parseInt(node.operand1.substring(2))) + " r1");
                                } else {
                                    lineList.add("move " + node.operand1 + " r1");
                                }
                                val1 = "r1";
                                regCount++;
                                temp = "r" + regCount;
                            } else if (node.operand1.startsWith("$T")) {
                                lineList.add("move " + node.operand1 + " r1");
                                val1 = "r1";
                                //val1 = tempMap.get(node.operand1);
                            }
                        }
                        if (node.operand2 != null) {
                            if (checkType(node.operand2).equals("INT") || checkType(node.operand2).equals("FLOAT")) {
                                if(node.operand2.startsWith("$P")) {
                                    lineList.add("move $" + (5+Integer.parseInt(node.operand2.substring(2))) + " r2");
                                } else {
                                    lineList.add("move " + node.operand2 + " r2");
                                }
                                val2 = "r2";
                                regCount++;
                            } else if (!node.operand2.startsWith("$T")) {
                                if(node.operand2.startsWith("$P")) {
                                    lineList.add("move $" + (5+Integer.parseInt(node.operand2.substring(2))) + " r2");
                                } else {
                                    lineList.add("move " + node.operand2 + " r2");
                                }
                                val2 = "r2";
                                regCount++;
                            } else if (node.operand2.startsWith("$T")) {
                                lineList.add("move " + node.operand2 + " r2");
                                val2 = "r2";
                            }
                        }
                        //tempMap.put(target, val1);
                        lineList.add("subi " + val2 + " " + val1);
                        lineList.add("move " + val1 + " " + target);
                        break;
                    case "JUMP":
                        lineList.add("jmp " + node.result);
                        break;
                    case "LABEL":
                        if ( global.functionList.contains(node.result)){
                            regCount = 0;
                            tempMap.clear();
                            currentFunction = node.result;
                        }
                        lineList.add("label " + node.result);
                        break;
                    case "GT":
                        if(node.operand1.startsWith("$P")) {
                            node.operand1 = "$" + (5 + Integer.parseInt(node.operand1.substring(2))) + "";
                        }
                        if(node.operand2.startsWith("$P")) {
                            node.operand2 = "$" + (5 + Integer.parseInt(node.operand2.substring(2))) + "";
                        }
                        lineList.add("move " + node.operand1 + " r1");
                        regCount++;

                        temp2 = "r" + regCount;
                        regCount++;
                        lineList.add("move " + node.operand2 + " r2");
                        isInt = true;
                        if(checkType(node.operand1).equals("INT")){
                            isInt = true;
                        } else if (checkType(node.operand1).equals("FLOAT")){
                            isInt = false;
                        } else if (global.checkTypeWScope(node.operand1, currentFunction) != null){
                            if (global.checkTypeWScope(node.operand1, currentFunction) .equals("INT")){
                                isInt = true;
                            } else if (global.checkTypeWScope(node.operand1, currentFunction).equals("FLOAT")){
                                isInt = false;
                            }
                        }
                        if(checkType(node.operand2).equals("INT")){
                            isInt = true;
                        } else if (checkType(node.operand2).equals("FLOAT")){
                            isInt = false;
                        } else if (global.checkTypeWScope(node.operand2, currentFunction) != null){
                            if (global.checkTypeWScope(node.operand2, currentFunction) .equals("INT")){
                                isInt = true;
                            } else if (global.checkTypeWScope(node.operand2, currentFunction).equals("FLOAT")){
                                isInt = false;
                            }
                        }
                        if(isInt){
                            lineList.add("cmpi r1 r2");
                        } else {
                            lineList.add("cmpr r1 r2");
                        }
                        lineList.add("jgt " + node.result);
                        break;
                    case "GE":
                        if(node.operand1.startsWith("$P")) {
                            node.operand1 = "$" + (5 + Integer.parseInt(node.operand1.substring(2)));
                        }
                        if(node.operand2.startsWith("$P")) {
                            node.operand2 = "$" + (5 + Integer.parseInt(node.operand2.substring(2)));
                        }
                        lineList.add("move " + node.operand1 + " r1");
                        regCount++;

                        temp2 = "r" + regCount;
                        regCount++;
                        lineList.add("move " + node.operand2 + " r2");
                        isInt = true;
                        if(checkType(node.operand1).equals("INT")){
                            isInt = true;
                        } else if (checkType(node.operand1).equals("FLOAT")){
                            isInt = false;
                        } else if (global.checkTypeWScope(node.operand1, currentFunction) != null){
                            if (global.checkTypeWScope(node.operand1, currentFunction) .equals("INT")){
                                isInt = true;
                            } else if (global.checkTypeWScope(node.operand1, currentFunction).equals("FLOAT")){
                                isInt = false;
                            }
                        }
                        if(checkType(node.operand2).equals("INT")){
                            isInt = true;
                        } else if (checkType(node.operand2).equals("FLOAT")){
                            isInt = false;
                        } else if (global.checkTypeWScope(node.operand2, currentFunction) != null){
                            if (global.checkTypeWScope(node.operand2, currentFunction) .equals("INT")){
                                isInt = true;
                            } else if (global.checkTypeWScope(node.operand2, currentFunction).equals("FLOAT")){
                                isInt = false;
                            }
                        }
                        if(isInt){
                            lineList.add("cmpi r1 r2");
                        } else {
                            lineList.add("cmpr r1 r2");
                        }
                        lineList.add("jge " + node.result);
                        break;
                    case "LT":
                        if(node.operand1.startsWith("$P")) {
                            node.operand1 = "$" + (5 + Integer.parseInt(node.operand1.substring(2)));
                        }
                        if(node.operand2.startsWith("$P")) {
                            node.operand2 = "$" + (5 + Integer.parseInt(node.operand2.substring(2)));
                        }
                        lineList.add("move " + node.operand1 + " r1");
                        regCount++;

                        temp2 = "r" + regCount;
                        regCount++;
                        lineList.add("move " + node.operand2 + " r2");

                        isInt = true;
                        if(checkType(node.operand1).equals("INT")){
                            isInt = true;
                        } else if (checkType(node.operand1).equals("FLOAT")){
                            isInt = false;
                        } else if (global.checkTypeWScope(node.operand1, currentFunction) != null){
                            if (global.checkTypeWScope(node.operand1, currentFunction) .equals("INT")){
                                isInt = true;
                            } else if (global.checkTypeWScope(node.operand1, currentFunction).equals("FLOAT")){
                                isInt = false;
                            }
                        }
                        if(checkType(node.operand2).equals("INT")){
                            isInt = true;
                        } else if (checkType(node.operand2).equals("FLOAT")){
                            isInt = false;
                        } else if (global.checkTypeWScope(node.operand2, currentFunction) != null){
                            if (global.checkTypeWScope(node.operand2, currentFunction) .equals("INT")){
                                isInt = true;
                            } else if (global.checkTypeWScope(node.operand2, currentFunction).equals("FLOAT")){
                                isInt = false;
                            }
                        }
                        if(isInt){
                            lineList.add("cmpi r1 r2");
                        } else {
                            lineList.add("cmpr r1 r2");
                        }
                        lineList.add("jlt " + node.result);
                        break;
                    case "LE":
                        if(node.operand1.startsWith("$P")) {
                            node.operand1 = "$" + (5 + Integer.parseInt(node.operand1.substring(2)));
                        }
                        if(node.operand2.startsWith("$P")) {
                            node.operand2 = "$" + (5 + Integer.parseInt(node.operand2.substring(2)));
                        }
                        lineList.add("move " + node.operand1 + " r1");
                        regCount++;
                        temp2 = "r" + regCount;
                        regCount++;
                        lineList.add("move " + node.operand2 + " r2");

                        isInt = true;
                        if(checkType(node.operand1).equals("INT")){
                            isInt = true;
                        } else if (checkType(node.operand1).equals("FLOAT")){
                            isInt = false;
                        } else if (global.checkTypeWScope(node.operand1, currentFunction) != null){
                            if (global.checkTypeWScope(node.operand1, currentFunction) .equals("INT")){
                                isInt = true;
                            } else if (global.checkTypeWScope(node.operand1, currentFunction).equals("FLOAT")){
                                isInt = false;
                            }
                        }
                        if(checkType(node.operand2).equals("INT")){
                            isInt = true;
                        } else if (checkType(node.operand2).equals("FLOAT")){
                            isInt = false;
                        } else if (global.checkTypeWScope(node.operand2, currentFunction) != null){
                            if (global.checkTypeWScope(node.operand2, currentFunction) .equals("INT")){
                                isInt = true;
                            } else if (global.checkTypeWScope(node.operand2, currentFunction).equals("FLOAT")){
                                isInt = false;
                            }
                        }
                        if(isInt){
                            lineList.add("cmpi r1 r2");
                        } else {
                            lineList.add("cmpr r1 r2");
                        }
                        lineList.add("jle " + node.result);
                        break;
                    case "NE":
                        if(node.operand1.startsWith("$P")) {
                            node.operand1 = "$" + (5 + Integer.parseInt(node.operand1.substring(2)));
                        }
                        if(node.operand2.startsWith("$P")) {
                            node.operand2 = "$" + (5 + Integer.parseInt(node.operand2.substring(2)));
                        }
                        lineList.add("move " + node.operand1 + " r1");
                        regCount++;

                        temp2 = "r" + regCount;
                        regCount++;
                        lineList.add("move " + node.operand2 + " r2");
                        isInt = true;
                        if(checkType(node.operand1).equals("INT")){
                            isInt = true;
                        } else if (checkType(node.operand1).equals("FLOAT")){
                            isInt = false;
                        } else if (global.checkTypeWScope(node.operand1, currentFunction) != null){
                            if (global.checkTypeWScope(node.operand1, currentFunction) .equals("INT")){
                                isInt = true;
                            } else if (global.checkTypeWScope(node.operand1, currentFunction).equals("FLOAT")){
                                isInt = false;
                            }
                        }
                        if(checkType(node.operand2).equals("INT")){
                            isInt = true;
                        } else if (checkType(node.operand2).equals("FLOAT")){
                            isInt = false;
                        } else if (global.checkTypeWScope(node.operand2, currentFunction) != null){
                            if (global.checkTypeWScope(node.operand2, currentFunction) .equals("INT")){
                                isInt = true;
                            } else if (global.checkTypeWScope(node.operand2, currentFunction).equals("FLOAT")){
                                isInt = false;
                            }
                        }
                        if(isInt){
                            lineList.add("cmpi r1 r2");
                        } else {
                            lineList.add("cmpr r1 r2");
                        }
                        lineList.add("jne " + node.result);
                        break;
                    case "EQ":
                        if(node.operand1.startsWith("$P")) {
                            node.operand1 = "$" + (5 + Integer.parseInt(node.operand1.substring(2)));
                        }
                        if(node.operand2.startsWith("$P")) {
                            node.operand2 = "$" + (5 + Integer.parseInt(node.operand2.substring(2)));
                        }
                        lineList.add("move " + node.operand1 + " r1");
                        regCount++;

                        temp2 = "r" + regCount;
                        regCount++;
                        lineList.add("move " + node.operand2 + " r2");
                        isInt = true;
                        if(checkType(node.operand1).equals("INT")){
                            isInt = true;
                        } else if (checkType(node.operand1).equals("FLOAT")){
                            isInt = false;
                        } else if (global.checkTypeWScope(node.operand1, currentFunction) != null){
                            if (global.checkTypeWScope(node.operand1, currentFunction) .equals("INT")){
                                isInt = true;
                            } else if (global.checkTypeWScope(node.operand1, currentFunction).equals("FLOAT")){
                                isInt = false;
                            }
                        }
                        if(checkType(node.operand2).equals("INT")){
                            isInt = true;
                        } else if (checkType(node.operand2).equals("FLOAT")){
                            isInt = false;
                        } else if (global.checkTypeWScope(node.operand2, currentFunction) != null){
                            if (global.checkTypeWScope(node.operand2, currentFunction) .equals("INT")){
                                isInt = true;
                            } else if (global.checkTypeWScope(node.operand2, currentFunction).equals("FLOAT")){
                                isInt = false;
                            }
                        }
                        if(isInt){
                            lineList.add("cmpi r1 r2");
                        } else {
                            lineList.add("cmpr r1 r2");
                        }
                        lineList.add("jeq " + node.result);
                        break;

                    default:
                        System.out.println(node.op);
                        break;
                }
            }
        }
        lineList.add("end");
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
