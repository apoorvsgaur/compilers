/**
 * Created by brandonscheller on 10/20/16
 */
public class Node {
    String op;
    String operand1;
    String operand2;
    String result;

    Node( String op, String operand1, String operand2, String result){
        this.op = op;
        this.operand1 = operand1;
        this.operand2 = operand2;
        this.result = result;
    }

    void print(){
        if(op != null & operand1 == null & operand2 == null & result == null ){
            System.out.println(";"+op);
        }
        else if(op == null & operand1 == null & operand2 == null & result == null ){
            System.out.println();
        }
        else if(op != null & operand1 == null & operand2 == null & result != null ){
            System.out.println(";"+op+" "+result);
        }
        else if(op != null & operand1 != null & operand2 == null & result != null ){
            System.out.println(";"+op+" "+operand1+" "+result);
        } else
            System.out.println(";"+op+" "+operand1+" "+operand2+" "+result);
    }
}
