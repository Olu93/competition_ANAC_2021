package geniusweb.custom.components;

import java.util.ArrayList;
import java.util.UUID;

import geniusweb.actions.Accept;
import geniusweb.actions.Action;
import geniusweb.custom.opponents.AbstractPolicy;
import geniusweb.custom.state.AbstractState;

/**
 * Node
 */
public class Node {
    public static enum NODE_TYPE {
        NORMAL, BELIEF, ACTION
    };

    private String id;
    private NODE_TYPE type;
    private Node parent;
    private ArrayList<Node> children;
    private Integer visits = 0;
    private Double value = 0d;
    private AbstractState<?> state;
    private Boolean isTerminal = false;

    public Node(Node parent, AbstractState<?> state) {
        super();
        this.id = UUID.randomUUID().toString();
        this.parent = parent;
        this.state = state;
        this.children = new ArrayList<Node>();

    }

    public NODE_TYPE getType() {
        return type;
    }

    public void setType(NODE_TYPE type) {
        this.type = type;
    }

    public Node addChild(Node childNode) {
        this.children.add(childNode);
        return this;
    }

    public Node updateVisits() {
        this.visits++;
        return this;
    }

    public Node setValue(Double valDouble) {
        this.value = valDouble;
        return this;
    }

    public String getId() {
        return id;
    }

    public Node setId(String id) {
        this.id = id;
        return this;
    }

    public Node getParent() {
        return parent;
    }

    public Node setParent(Node parent) {
        this.parent = parent;
        return this;
    }

    public ArrayList<Node> getChildren() {
        return children;
    }

    public Node setChildren(ArrayList<Node> children) {
        this.children = children;
        return this;
    }

    public Integer getVisits() {
        return visits;
    }

    public Node setVisits(Integer visits) {
        this.visits = visits;
        return this;
    }

    public Double getValue() {
        return value;
    }

    public AbstractState<?> getState() {
        return state;
    }

    public Node setState(AbstractState<?> state) {
        this.state = state;
        return this;
    }

    public Boolean getIsTerminal() {
        return isTerminal;
    }

    public Node setIsTerminal(Boolean isTerminal) {
        this.isTerminal = isTerminal;
        return this;
    }

    @Override
    public String toString() {
        Action a = this instanceof BeliefNode ? ((BeliefNode) this).getObservation() : ((ActionNode) this).getAction();
        return new StringBuilder().append(this instanceof BeliefNode ? "BN" : "AN").append(" : ")
                .append(this.id.substring(24)).append("[").append("children:").append(this.children.size()).append(",")
                .append("visits:").append(this.visits).append(",").append("value:").append(this.value).append(",")
                .append("isTerminal:").append(this.getIsTerminal()).append("]").append("-").append(a).toString();
    }

    public static Node buildNode(NODE_TYPE type, Node parent, AbstractState<?> newState, AbstractPolicy opponent,
            Action lastAction) {

        Node child;

        switch (type) {
        case BELIEF:
            child = new BeliefNode(parent, newState, lastAction);
            break;
        case ACTION:
            child = new ActionNode(parent, newState, lastAction);
            break;
        default:
            child = new Node(parent, newState);
            break;
        }

        child = child.setIsTerminal(lastAction instanceof Accept);
        return child;
    }

}