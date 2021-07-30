package geniusweb.custom.opponents;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Random;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import geniusweb.actions.Action;
import geniusweb.bidspace.AllBidsList;
import geniusweb.custom.state.AbstractState;
import geniusweb.exampleparties.boulware.Boulware;
import geniusweb.issuevalue.Bid;
import geniusweb.issuevalue.Domain;
import geniusweb.profile.utilityspace.UtilitySpace;
import tudelft.utilities.immutablelist.ImmutableList;

public class BoulwareOpponentPolicy extends TimeDependentOpponentPolicy {

    private static final String BOULWARE = "Boulware";

    @JsonCreator
    public BoulwareOpponentPolicy(@JsonProperty("utilitySpace") UtilitySpace utilitySpace,
            @JsonProperty("name") String name, @JsonProperty("e") double e) {
        super(utilitySpace, name, e);
    }

    public BoulwareOpponentPolicy(Domain domain) {
        super(domain);
        this.setName(BOULWARE);
    }

    @Override
    public double getE() {
        return 0.2;
    }

}
