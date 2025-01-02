package org.example;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class HuffmanCode {
    private final Map<String, String> huffmanCode = new HashMap<>();

    public Map<String, String> generateCodeword(Map<String, Integer> frequencies) {
        Node root = generateTree(frequencies);

        generateCodeWord(root, new StringBuilder());
        return huffmanCode;
    }

    private void generateCodeWord(Node root, StringBuilder code) {
        if (root == null) {
            return;
        }

        if (root.left == null && root.right == null) {
            huffmanCode.put(root.data, String.valueOf(code));
            return;
        }

        code.append("0");
        generateCodeWord(root.left, code);
        code.deleteCharAt(code.length() - 1);

        code.append("1");
        generateCodeWord(root.right, code);
        code.deleteCharAt(code.length() - 1);
    }

    private Node generateTree(Map<String, Integer> frequencies) {
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(o -> o.freq));

        for (Map.Entry<String, Integer> entry : frequencies.entrySet()) {
            Node node = new Node(entry.getKey(), entry.getValue());
            pq.add(node);
        }

        while (pq.size() != 1) {
            Node node = new Node();

            Node leftNode = pq.poll();
            Node rightNode = pq.poll();

            node.left = leftNode;
            node.right = rightNode;

            node.freq = leftNode.freq + rightNode.freq;

            pq.add(node);
        }

        return pq.peek();
    }

    private static class Node {
        Node left;
        Node right;
        String data;
        int freq;

        public Node(String data, int freq) {
            this.data = data;
            this.freq = freq;
        }

        public Node() {
        }
    }
}
