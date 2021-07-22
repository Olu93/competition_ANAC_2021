package geniusweb.custom.evaluators;

import java.math.BigDecimal;
import java.util.ArrayList;

import geniusweb.actions.Accept;
import geniusweb.actions.Action;
import geniusweb.actions.Offer;
import geniusweb.custom.state.HistoryState;
import geniusweb.issuevalue.Bid;
import geniusweb.profile.utilityspace.UtilitySpace;

/**
 * MeanUtilityEvaluator
 */
public class Last2BidsProductUtilityEvaluator implements EvaluationFunctionInterface<HistoryState> {

    private UtilitySpace utilitySpace;

    public Last2BidsProductUtilityEvaluator(UtilitySpace utilitySpace) {
        super();
        this.utilitySpace = utilitySpace;
    }

    @Override
    public Double evaluate(HistoryState state) {
        ArrayList<Action> currState = state.getRepresentation();
        Action action1 = currState.size() >= 1 ? currState.get(currState.size() - 1) : null;
        Action action2 = currState.size() >= 2 ? currState.get(currState.size() - 2) : null;
        Bid bid1 = action1 instanceof Offer ? ((Offer) action1).getBid() : ((Accept) action1).getBid();
        Bid bid2 = action2 instanceof Offer ? ((Offer) action2).getBid() : ((Accept) action2).getBid();
        if (action1 instanceof Accept)
            return state.getUtilitySpace().getUtility(bid1).doubleValue();
        BigDecimal utility1 = action1 != null ? this.utilitySpace.getUtility(bid1) : BigDecimal.ONE;
        BigDecimal utility2 = action2 != null ? this.utilitySpace.getUtility(bid2) : BigDecimal.ONE;
        BigDecimal mean = utility1.multiply(utility2);
        return mean.doubleValue();
    }

}