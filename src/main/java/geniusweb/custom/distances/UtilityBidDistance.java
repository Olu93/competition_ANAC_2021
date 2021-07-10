package geniusweb.custom.distances;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import geniusweb.issuevalue.Bid;
import geniusweb.profile.utilityspace.UtilitySpace;

public class UtilityBidDistance extends AbstractBidDistance {

    public UtilityBidDistance(UtilitySpace utilitySpace) {
        super(utilitySpace);
    }

    @Override
    public Double computeDistance(Bid b1, Bid b2) {
        return Math.abs(this.getUtility(b1)-this.getUtility(b2));
    }


    
}
