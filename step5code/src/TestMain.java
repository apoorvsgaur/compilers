/**
 * Created by brandonscheller on 10/21/16
 */
public class TestMain {
    public static void main(String[] args){
        System.out.println(checkType("3.0"));
    }

    static String checkType(String value){
        try{
            int integer = Integer.parseInt(value);
            return "INT";
        }
        catch(NumberFormatException e){
        }
        try{
            float floatnum = Float.parseFloat(value);
            return "FLOAT";
        }
        catch(NumberFormatException e){
        }
        throw new RuntimeException("Incorrect integer");
    }
}
