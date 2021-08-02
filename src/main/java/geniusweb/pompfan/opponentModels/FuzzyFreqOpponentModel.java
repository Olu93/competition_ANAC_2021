package geniusweb.pompfan.opponentModels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;

import geniusweb.actions.Action;
import geniusweb.actions.Offer;
import geniusweb.actions.PartyId;
import geniusweb.issuevalue.Bid;
import geniusweb.issuevalue.Value;
import geniusweb.opponentmodel.FrequencyOpponentModel;

// ??: Generic impl. of random generator
// ??: Baysian version
// ??: Time dependant
public class FuzzyFreqOpponentModel extends FrequencyOpponentModel implements IFuzzyModel {

    private static final int SLACK = 3;
    @JsonIgnore
    private Map<String, List<Value>> bidFreqAsList = null;
    @JsonIgnore
    private Integer initBidCount = 0;
    @JsonIgnore
    private Map<String, Map<Value, Long>> sumIssVals = new HashMap<String, Map<Value, Long>>();
    @JsonIgnore
    private Random random = new Random();
    @JsonIgnore
    private int numGenerations = 10;
    private PartyId partyId;
    private List<Action> newHistory;

    public FuzzyFreqOpponentModel() {
        super();
    }

    public FuzzyFreqOpponentModel(PartyId actor, List<Action> realHistoryActions) {
        super();
        this.setPartyId(actor);;
        this.setInitBidCount(realHistoryActions.size());
        this.setNumGenerations(Math.max(0, this.getInitBidCount() + (this.random.nextInt(SLACK) - (2 * SLACK))));;
        this.setNewHistory(this.generateHistory());
        this.setBidFrequencies(this.genNewFreqModel(this.getNewHistory()));;

    }

    public Map<String, List<Value>> initHistory(List<Action> realHistoryActions) {
        if (this.getDomain() == null) {
            throw new IllegalStateException("domain is not initialized");
        }
        Map<String, List<Value>> newFreqs = new HashMap<String, List<Value>>();
        for (Action action : realHistoryActions) {
            if (!(action instanceof Offer))
                continue;

            Offer offer = (Offer) action;
            Bid bid = offer.getBid();
            this.partyId = offer.getActor();
            for (String issue : this.getDomain().getIssues()) {
                Value value = bid.getValue(issue);
                newFreqs.get(issue).add(value);
            }
        }
        for (String issue : newFreqs.keySet()) {
            List<Value> tmp = newFreqs.get(issue);
            this.sumIssVals.put(issue, tmp.stream().collect(Collectors.groupingBy(e -> e, Collectors.counting())));
        }
        return newFreqs;
    }

    public Map<String, Map<Value, Integer>> genNewFreqModel(List<Action> realHistoryActions) {
        if (this.getDomain() == null) {
            throw new IllegalStateException("domain is not initialized");
        }

        Map<String, Map<Value, Integer>> newFreqs = new HashMap<String, Map<Value, Integer>>();
        for (Action action : realHistoryActions) {
            if (!(action instanceof Offer))
                continue;

            Bid bid = ((Offer) action).getBid();
            for (String issue : this.getDomain().getIssues()) {
                Map<Value, Integer> freqs = newFreqs.getOrDefault(issue, new HashMap<Value, Integer>());
                Value value = bid.getValue(issue);
                if (value != null) {
                    Integer oldfreq = freqs.getOrDefault(value, 0);
                    freqs.put(value, oldfreq + 1);
                }
            }
        }
        return newFreqs;
    }

    @Override
    public List<Action> generateHistory() {
        List<Action> newHist = new ArrayList<Action>();
        Set<String> allIssues = this.sumIssVals.keySet();
        for (int i = 0; i < this.numGenerations; i++) {
            Map<String, Value> tmpMap = new HashMap<String, Value>();
            Bid bid = null;
            for (String issue : allIssues) {
                List<Value> candidates = this.bidFreqAsList.get(issue);
                Integer selectedIndex = this.random.nextInt(this.initBidCount);
                Value selectedValue = candidates.get(selectedIndex);
                tmpMap.put(issue, selectedValue);
                bid = new Bid(tmpMap);
            }
            newHist.add(new Offer(this.partyId, bid));
        }
        return newHist;
    }

    public Integer getInitBidCount() {
        return initBidCount;
    }

    public void setInitBidCount(Integer initBidCount) {
        this.initBidCount = initBidCount;
    }

    public Map<String, List<Value>> getBidFrequencies() {
        return bidFreqAsList;
    }

    public Map<String, Map<Value, Long>> getSumIssVals() {
        return sumIssVals;
    }

    public void setSumIssVals(Map<String, Map<Value, Long>> sumIssVals) {
        this.sumIssVals = sumIssVals;
    }

    public Random getRandom() {
        return random;
    }

    public void setRandom(Random random) {
        this.random = random;
    }

    public int getNumGenerations() {
        return numGenerations;
    }

    public void setNumGenerations(int numGenerations) {
        this.numGenerations = numGenerations;
    }

    public PartyId getPartyId() {
        return partyId;
    }

    public void setPartyId(PartyId partyId) {
        this.partyId = partyId;
    }

    public void setBidFrequencies(Map<String, Map<Value, Integer>> bidFrequencies) {
    }

    public Map<String, List<Value>> getBidFreqAsList() {
        return bidFreqAsList;
    }

    public void setBidFreqAsList(Map<String, List<Value>> bidFreqAsList) {
        this.bidFreqAsList = bidFreqAsList;
    }

    public List<Action> getNewHistory() {
        return newHistory;
    }

    public void setNewHistory(List<Action> newHistory) {
        this.newHistory = newHistory;
    }
}
