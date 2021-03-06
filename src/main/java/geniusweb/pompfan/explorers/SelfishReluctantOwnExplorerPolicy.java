package geniusweb.pompfan.explorers;

import java.math.BigDecimal;

import geniusweb.actions.Accept;
import geniusweb.actions.Action;
import geniusweb.actions.Offer;
import geniusweb.actions.PartyId;
import geniusweb.issuevalue.Bid;
import geniusweb.pompfan.state.AbstractState;
import geniusweb.profile.utilityspace.UtilitySpace;

public class SelfishReluctantOwnExplorerPolicy extends AbstractOwnExplorationPolicy {

    public SelfishReluctantOwnExplorerPolicy(UtilitySpace utilitySpace, PartyId id) {
        super(utilitySpace, id);
    }

    @Override
    public Action chooseAction(Bid lastOpponentBid, Bid lastAgentBid, AbstractState<?> state) {
        Action action;
        Bid bid;
        
        long i = this.getRandom().nextInt(this.getPossibleBids().size().intValue());
        Bid ownBidCandidate = this.getPossibleBids().get(i);
        bid = shouldAccept(ownBidCandidate, lastOpponentBid) ? ownBidCandidate : lastOpponentBid;

        // 2 pcent of the time we do accepts
        if (this.getRandom().nextInt(100) > 98) {
            return new Accept(this.getPartyId(), lastOpponentBid);
        }

        action = new Offer(this.getPartyId(), bid);
        return action;
    }

    private boolean shouldAccept(Bid bid, Bid oppBid) {
        if (bid == null)
            return false;
        BigDecimal sample = this.getUtilitySpace().getUtility(bid);
        BigDecimal target = this.getUtilitySpace().getUtility(oppBid);
        return (sample.doubleValue() <= target.doubleValue());
    }

    @Override
    protected void init() {assert true;}

}
