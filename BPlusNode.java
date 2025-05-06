import java.io.Serializable;
import java.util.*;

public class BPlusNode<K extends Comparable<K>, V> implements Serializable {
    boolean isLeaf;
    List<K> keys;
    List<BPlusNode<K, V>> children; // apenas para nós internos
    List<V> values; // apenas para folhas
    BPlusNode<K, V> next; // ligação entre folhas

    public BPlusNode(boolean isLeaf) {
        this.isLeaf = isLeaf;
        this.keys = new ArrayList<>();
        if (isLeaf) {
            this.values = new ArrayList<>();
            this.next = null;
        } else {
            this.children = new ArrayList<>();
        }
    }
}
