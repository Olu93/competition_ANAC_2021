package geniusweb.pompfan.beliefs;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import geniusweb.actions.Offer;
import geniusweb.bidspace.AllBidsList;
import geniusweb.pompfan.distances.AbstractBidDistance;
import geniusweb.pompfan.opponents.AbstractPolicy;
import geniusweb.pompfan.state.AbstractState;

public class BayesianUpdateParticleFilterBelief extends ParticleFilterWithAcceptBelief {

    private double allBidsSize;

    public BayesianUpdateParticleFilterBelief(Map<AbstractPolicy, Double> opponentProbabilities,
            AbstractBidDistance distance) {
        super(opponentProbabilities, distance);
    }

    public BayesianUpdateParticleFilterBelief(List<AbstractPolicy> listOfOpponents, AbstractBidDistance distance) {
        super(listOfOpponents, distance);
        this.EPSILON = 1.0;
    }

    @Override
    public AbstractBelief updateBeliefs(Offer realObservation, Offer lastAgentAction, Offer lastOppAction,
            AbstractState<?> state) {
        Map<AbstractPolicy, Double> prior = this.getOpponentProbabilities();
        AbstractBelief oldBelief = super.updateBeliefs(realObservation, lastAgentAction, lastOppAction, state);
        Double sums = oldBelief.getProbabilities().parallelStream().collect(Collectors.summingDouble(x -> x));
        Map<AbstractPolicy, Double> newBelief = oldBelief.getOpponentProbabilities().entrySet().parallelStream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> updateFormula(prior, sums, e)));
        Double newSums = newBelief.entrySet().parallelStream().collect(Collectors.summingDouble(e -> e.getValue()));
        Map<AbstractPolicy, Double> newestBelief = newBelief.entrySet().parallelStream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue() / newSums));

        return new BayesianUpdateParticleFilterBelief(newestBelief, this.getDistance());
    }

    private Double updateFormula(Map<AbstractPolicy, Double> prior, Double sums, Entry<AbstractPolicy, Double> e) {
        return (e.getValue() / sums) * prior.get(e.getKey());
    }

}
