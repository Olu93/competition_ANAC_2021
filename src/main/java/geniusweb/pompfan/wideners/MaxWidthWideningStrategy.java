package geniusweb.pompfan.wideners;

import java.util.HashMap;

import geniusweb.pompfan.components.ActionNode;
import geniusweb.pompfan.components.BeliefNode;
import geniusweb.pompfan.components.Node;
import geniusweb.pompfan.components.Tree;
import geniusweb.pompfan.explorers.AbstractOwnExplorationPolicy;
import geniusweb.pompfan.opponents.AbstractPolicy;
import geniusweb.pompfan.state.StateRepresentationException;
import geniusweb.progress.Progress;

public class MaxWidthWideningStrategy extends AbstractWidener {
    private Integer maxWidth;
    // !! AT THIS MOMENT THIS IS JUNK
    // TODO: This needs to be fixed

    public MaxWidthWideningStrategy(AbstractOwnExplorationPolicy ownExplorationStrategy,
            HashMap<String, Object> params) {
        super(ownExplorationStrategy);
        this.maxWidth = Double.valueOf((String) params.getOrDefault("maxWidth", "42.0")).intValue();
    }

    public Integer getMaxWidth() {
        return maxWidth;
    }

    public MaxWidthWideningStrategy setMaxWidth(Integer maxWidth) {
        this.maxWidth = maxWidth;
        return this;
    }

    @Override
    public void widen(Progress simulatedProgress, Long shiftSimTime, Node currRoot) throws StateRepresentationException {
        AbstractPolicy currOpp = currRoot.getState().getOpponent();
        while (currRoot.getChildren().size() == this.maxWidth) {
            // Going down the tree - Action Node Level
            currRoot = Tree.selectFavoriteChild(currRoot.getChildren());
            currRoot.getState().setOpponent(currOpp);
            if (currRoot.getChildren().size() < this.maxWidth) {
                // Widening the Belief level

                Double simulatedTimeOfObsReceival = simulatedProgress.get(System.currentTimeMillis());
                ActionNode currActionNode = (ActionNode) currRoot;
                BeliefNode receivedObservationNode = (BeliefNode) currActionNode
                        .receiveObservation(simulatedTimeOfObsReceival);
                currRoot = receivedObservationNode;
                if (currRoot == null) {
                    return;
                } // !!: finish dealing with exceptions
                currRoot.getState().setOpponent(currOpp);

                Double value = currRoot.getState().evaluate();
                Tree.backpropagate(currRoot, value);
                return;
            } else {
                // Going down the tree - Belief Node Level
                ActionNode oldRoot = (ActionNode) currRoot;
                oldRoot.getState().setOpponent(currOpp);
                currRoot = Tree.selectFavoriteChild(currRoot.getChildren());
                if (currRoot == null) {
                    Double simulatedTimeOfNewNode = simulatedProgress.get(System.currentTimeMillis());
                    BeliefNode newChild = (BeliefNode) oldRoot.receiveObservation(simulatedTimeOfNewNode);
                    currRoot = newChild;
                    currRoot.getState().setOpponent(currOpp);
                    Double value = currRoot.getState().evaluate();
                    Tree.backpropagate(currRoot, value);
                    return;
                }

            }
        }

        if (currRoot.getChildren().size() < this.maxWidth) {
            // Widening the Action level
            Double simulatedTimeOfActReceival = simulatedProgress.get(System.currentTimeMillis());
            BeliefNode currBeliefNode = (BeliefNode) currRoot;
            ActionNode receivedActionNode = (ActionNode) currBeliefNode.act(this.getOwnExplorationStrategy(),
                    simulatedTimeOfActReceival);
            ActionNode actionNode = receivedActionNode;
            Double simulatedTimeOfObsReceival = simulatedProgress.get(System.currentTimeMillis());
            BeliefNode beliefNode = (BeliefNode) actionNode.receiveObservation(simulatedTimeOfObsReceival);
            currRoot = beliefNode;
            if (currRoot == null) {
                return;
            }
            currRoot.getState().setOpponent(currOpp);
            Double value = currRoot.getState().evaluate();
            Tree.backpropagate(currRoot, value);
        }
    }
}
