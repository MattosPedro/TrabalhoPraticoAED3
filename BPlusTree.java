import java.io.Serializable;
import java.util.*;

public class BPlusTree<K extends Comparable<K>, V> implements Serializable {
    private int ordem;
    private BPlusNode<K, V> root;

    public BPlusTree(int ordem) {
        this.ordem = ordem;
        this.root = new BPlusNode<>(true);
    }

    public V search(K key) {
        BPlusNode<K, V> node = root;
        while (!node.isLeaf) {
            int i = Collections.binarySearch(node.keys, key);
            int childIndex = i >= 0 ? i + 1 : -i - 1;
            node = node.children.get(childIndex);
        }

        int pos = Collections.binarySearch(node.keys, key);
        if (pos >= 0) {
            return node.values.get(pos);
        }
        return null;
    }

    public void insert(K key, V value) {
        BPlusNode<K, V> r = root;
        if (r.keys.size() == 2 * ordem - 1) {
            BPlusNode<K, V> s = new BPlusNode<>(false);
            s.children.add(r);
            splitChild(s, 0, r);
            root = s;
        }
        insertNonFull(root, key, value);
    }

    private void insertNonFull(BPlusNode<K, V> node, K key, V value) {
        if (node.isLeaf) {
            int i = Collections.binarySearch(node.keys, key);
            int insertPos = i >= 0 ? i : -i - 1;
            node.keys.add(insertPos, key);
            node.values.add(insertPos, value);
        } else {
            int i = Collections.binarySearch(node.keys, key);
            int childIndex = i >= 0 ? i + 1 : -i - 1;
            BPlusNode<K, V> child = node.children.get(childIndex);
            if (child.keys.size() == 2 * ordem - 1) {
                splitChild(node, childIndex, child);
                if (key.compareTo(node.keys.get(childIndex)) > 0) {
                    childIndex++;
                }
            }
            insertNonFull(node.children.get(childIndex), key, value);
        }
    }

    private void splitChild(BPlusNode<K, V> parent, int index, BPlusNode<K, V> nodeToSplit) {
        BPlusNode<K, V> newNode = new BPlusNode<>(nodeToSplit.isLeaf);
        // Calcular corretamente a posição do meio
        int mid = nodeToSplit.keys.size() / 2;

        // Dividir as chaves
        newNode.keys.addAll(nodeToSplit.keys.subList(mid + 1, nodeToSplit.keys.size())); // Pega a segunda metade
        nodeToSplit.keys = new ArrayList<>(nodeToSplit.keys.subList(0, mid + 1)); // Pega a primeira metade

        if (nodeToSplit.isLeaf) {
            // Para nós folha, dividimos também os valores
            newNode.values = new ArrayList<>(nodeToSplit.values.subList(mid + 1, nodeToSplit.values.size()));
            nodeToSplit.values = new ArrayList<>(nodeToSplit.values.subList(0, mid + 1));

            // Ajusta o ponteiro para o próximo nó
            newNode.next = nodeToSplit.next;
            nodeToSplit.next = newNode;

            // Atualiza as chaves e filhos do nó pai
            parent.keys.add(index, newNode.keys.get(0));
            parent.children.add(index + 1, newNode);
        } else {
            // Para nós internos, dividimos os filhos
            newNode.children = new ArrayList<>(nodeToSplit.children.subList(mid + 1, nodeToSplit.children.size()));
            nodeToSplit.children = new ArrayList<>(nodeToSplit.children.subList(0, mid + 1));

            // O nó pai recebe a chave que separa as duas metades
            parent.keys.add(index, nodeToSplit.keys.remove(mid));
            parent.children.add(index + 1, newNode);
        }
    }

    public boolean delete(K key) {
        // Implementação de remoção não obrigatória se você apenas marcar como deletado
        // no arquivo
        return false;
    }

    public void printTree() {
        printNode(root, 0);
    }

    private void printNode(BPlusNode<K, V> node, int nivel) {
        System.out.println("Nível " + nivel + ": " + node.keys);
        if (!node.isLeaf) {
            for (BPlusNode<K, V> child : node.children) {
                printNode(child, nivel + 1);
            }
        }
    }

    public void exibirArvore() {
        if (root == null) {
            System.out.println("A árvore está vazia.");
        } else {
            System.out.println("Exibindo árvore:");
            exibirNo(root, 0); // Começa pela raiz, com nível 0
        }
    }

    private void exibirNo(BPlusNode<K, V> node, int nivel) {
        if (node == null)
            return;

        // Imprimir o nível atual e as chaves do nó
        String indent = " ".repeat(nivel * 4);
        System.out.println(indent + "Nível " + nivel + ": " + node.keys);

        // Se for um nó interno, percorre os filhos
        if (!node.isLeaf) {
            for (BPlusNode<K, V> child : node.children) {
                exibirNo(child, nivel + 1); // Vai para o próximo nível
            }
        }
    }

}
