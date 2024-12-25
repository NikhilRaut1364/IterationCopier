package com.stellantis.team.utility.model;
import java.util.ArrayList;
import java.util.List;

public class Node {
	private IterationPair iteration;
    private List<Node> children;

    public Node(IterationPair iteration) {
        this.iteration = iteration;
        this.children = new ArrayList<>();
    }

    public IterationPair getIteration() {
        return iteration;
    }

    public List<Node> getChildren() {
        return children;
    }

    public void addChild(Node child) {
        children.add(child);
    }

    @Override
    public String toString() {
        return iteration.getDisplay();
    }
}
