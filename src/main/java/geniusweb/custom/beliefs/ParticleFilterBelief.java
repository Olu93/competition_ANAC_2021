package geniusweb.custom.beliefs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import geniusweb.actions.Action;
import geniusweb.actions.Offer;
import geniusweb.custom.distances.AbstractBidDistance;
import geniusweb.custom.opponents.AbstractPolicy;
import geniusweb.custom.state.AbstractState;
import geniusweb.issuevalue.Bid;

public class ParticleFilterBelief extends AbstractBelief{

    public static final int NUMBER_SAMPLES = 100;
    public static final Double SAMENESS_THRESHOLD = 0.1;
    List<AbstractPolicy> particles = new ArrayList<>();

    public ParticleFilterBelief(List<AbstractPolicy> listOfOpponents,  AbstractBidDistance distance) {
        super(listOfOpponents, distance); // particles
        
    }
    
    @Override
    public AbstractBelief updateBeliefs(Offer realObservation, Offer lastAction, AbstractState<?> state) {


        for (AbstractPolicy abstractPolicy : this.getOpponentProbabilities().keySet()) {
            List<Bid> candidateObservations = new ArrayList<>();
            for (int i = 0; i < ParticleFilterBelief.NUMBER_SAMPLES; i++) {
                // Monte Carlo Sampling
                // This should be in a loop -- We need to try multiple actions to get an understanding of whether the opponent could generate the real obs.
                Action chosenAction = abstractPolicy.chooseAction(lastAction.getBid(), state);
                if (chosenAction instanceof Offer) {
                    candidateObservations.add(((Offer) chosenAction).getBid());
                }
            }
            boolean hasAnyMatch = candidateObservations.parallelStream().anyMatch(obs -> this.getDistance().computeDistance(obs, realObservation.getBid()) < SAMENESS_THRESHOLD);
            if (hasAnyMatch) {
                particles.add(abstractPolicy);
            }
        }
        
        return new ParticleFilterBelief(particles, this.getDistance());
    }


}
