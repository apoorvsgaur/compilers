/**
 * Created by brandonscheller on 10/20/16
 */
class Symbol {
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

    String getType(){
        return type;
    }
}
