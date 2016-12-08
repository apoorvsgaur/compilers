import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by brandonscheller on 10/20/16
 */
public class NodePackage {

    enum NodeType {
        EMPTY, ID, ID_OP
    }

    ArrayList<Node> nodes = new ArrayList<>();
    NodeType type;
    String id;
    String op;
    //bad practice
    ArrayList<String> targets = new ArrayList<>();

    NodePackage(){
        this.type = NodeType.EMPTY;
    }
    NodePackage(String ID){
        this.type = NodeType.ID;
        this.id = ID;
    }
    NodePackage(String ID, ArrayList<Node> nodes){
        this.type = NodeType.ID;
        this.id = ID;
        this.nodes.addAll(nodes);
    }
    NodePackage(String ID, String OP, ArrayList<Node> nodes){
        this.type = NodeType.ID_OP;
        this.id = ID;
        this.op = OP;
        this.nodes.addAll(nodes);
    }

    ArrayList<Node> getNodes(){
        return nodes;
    }
}
