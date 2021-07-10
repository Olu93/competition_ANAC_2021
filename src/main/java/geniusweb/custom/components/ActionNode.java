package geniusweb.custom.components;

import geniusweb.actions.Action;
import geniusweb.custom.state.AbstractState;
import geniusweb.custom.state.StateRepresentationException;

public class ActionNode extends Node {
    private Action action;

    public ActionNode(Node parent, AbstractState<?> state, Action action) {
        super(parent, state);
        this.setType(Node.NODE_TYPE.ACTION);
        this.setAction(action);
    }

    public Node receiveObservation() throws StateRepresentationException {
        AbstractState<?> state = this.getState();
        Action observation = state.getOpponent().chooseAction();
        AbstractState<?> newState = state.updateState(observation);
        // System.out.println("================== " + this.getChildren().size() + "
        // ==================");
        // System.out.println("Parent: " + this);
        BeliefNode child = (BeliefNode) this.getChildren().stream()
                // .peek(e -> System.out.println("Action: " + e))
                // .peek(e -> System.out.println("State: " + ((BeliefNode) e).getObservation()))
                .filter(childNode -> childNode.getState().equals(newState))
                // .peek(action -> System.out.println("Filtered value: " + action))
                .findFirst().orElse(null);
        if (child == null) {
            child = (BeliefNode) Node.buildNode(Node.NODE_TYPE.BELIEF, this, newState, state.getOpponent(),
                    observation);
            this.addChild(child);
        }
        return child;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

}
