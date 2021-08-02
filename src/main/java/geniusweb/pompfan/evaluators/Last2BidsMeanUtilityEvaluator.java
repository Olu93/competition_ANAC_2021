package geniusweb.pompfan.evaluators;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonCreator;

import geniusweb.actions.Accept;
import geniusweb.actions.Action;
import geniusweb.actions.Offer;
import geniusweb.issuevalue.Bid;
import geniusweb.issuevalue.Domain;
import geniusweb.pompfan.helper.BidVector;
import geniusweb.pompfan.state.AbstractState;
import geniusweb.pompfan.state.HistoryState;
import geniusweb.profile.utilityspace.UtilitySpace;

/**
 * MeanUtilityEvaluator
 */
public class Last2BidsMeanUtilityEvaluator implements IEvalFunction<HistoryState> {
    @JsonBackReference
    private UtilitySpace utilitySpace;


    public Last2BidsMeanUtilityEvaluator() {
        super();
    }

    public UtilitySpace getUtilitySpace() {
        return utilitySpace;
    }

    public void setUtilitySpace(UtilitySpace utilitySpace) {
        this.utilitySpace = utilitySpace;
    }

    public Last2BidsMeanUtilityEvaluator(UtilitySpace utilitySpace) {
        super();
        this.utilitySpace = utilitySpace;
        if (this.getUtilitySpace() == null) {
            System.out.println("Something is wrong");
        }
    }

    @Override
    public Double evaluate(HistoryState state) {
        ArrayList<Action> currState = state.getRepresentation();
        Action action1 = currState.size() >= 1 ? currState.get(currState.size() - 1) : null;
        Action action2 = currState.size() >= 2 ? currState.get(currState.size() - 2) : null;
        Bid bid1 = action1 instanceof Offer ? ((Offer) action1).getBid() : ((Accept) action1).getBid();
        Bid bid2 = action2 instanceof Offer ? ((Offer) action2).getBid() : ((Accept) action2).getBid();
        BigDecimal utility1 = action1 != null ? this.utilitySpace.getUtility(bid1) : BigDecimal.ZERO;
        BigDecimal utility2 = action2 != null ? this.utilitySpace.getUtility(bid2) : BigDecimal.ZERO;
        BigDecimal mean = utility1.add(utility2).divide(new BigDecimal(2));
        return mean.doubleValue();
    }

}