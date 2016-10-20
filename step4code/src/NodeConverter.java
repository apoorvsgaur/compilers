import java.util.ArrayList;

/**
 * Created by brandonscheller on 10/20/16.
 */
public class NodeConverter {
    private ArrayList<Node> nodeList = new ArrayList<>();

    void addNode( Node node ){
        nodeList.add(node);
    }

    void print(){
        nodeList.forEach(Node::print);
    }
}
