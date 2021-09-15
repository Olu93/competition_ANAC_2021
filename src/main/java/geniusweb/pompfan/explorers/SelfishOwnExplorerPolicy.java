package geniusweb.pompfan.explorers;

import java.math.BigDecimal;

import geniusweb.actions.Accept;
import geniusweb.actions.Action;
import geniusweb.actions.Offer;
import geniusweb.actions.PartyId;
import geniusweb.issuevalue.Bid;
import geniusweb.pompfan.state.AbstractState;
import geniusweb.profile.utilityspace.UtilitySpace;

public class SelfishOwnExplorerPolicy extends RandomOwnExplorerPolicy {


    public SelfishOwnExplorerPolicy(UtilitySpace utilitySpace, PartyId id) {
        super(utilitySpace,  id);
    }

    @Override
    public Action chooseAction(Bid lastOpponentBid, Bid lastAgentBid, AbstractState<?> state) {
        Action action;
        Bid bid;
        long i = this.getRandom().nextInt(this.getPossibleBids().size().intValue());
        bid = this.getPossibleBids().get(i);
        action = isGood(bid) ? new Offer(this.getPartyId(), bid) : new Accept(this.getPartyId(), lastOpponentBid);
        return action;
    }

    private boolean isGood(Bid bid) {
        if (bid == null)
            return false;
        BigDecimal sample = this.getUtilitySpace().getUtility(bid);
        return (sample.compareTo(this.getSTUBBORNESS()) >= 0);
    }


}
