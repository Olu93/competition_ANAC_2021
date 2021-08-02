package geniusweb.pompfan.agent; // TODO: change name

import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

import geniusweb.actions.Action;
import geniusweb.pompfan.beliefs.AbstractBelief;
import geniusweb.pompfan.components.BeliefNode;

/**
 * The class hold the negotiation data that is obtain during a negotiation
 * session. It will be saved to disk after the negotiation has finished. During
 * the learning phase, this negotiation data can be used to update the
 * persistent state of the agent. NOTE that Jackson can serialize many default
 * java classes, but not custom classes out-of-the-box.
 */
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class NegotiationData {

    private Double maxReceivedUtil = 0.0;
    private Double agreementUtil = 0.0;
    private String opponentName;
    private AbstractBelief belief;
    private BeliefNode rootNode;
    private HashMap<String, Object> configuration;
    private List<Action> realOppHistory;

    public HashMap<String, Object> getConfiguration() {
        return configuration;
    }

    public List<Action> getRealOppHistory() {
        return realOppHistory;
    }

    public NegotiationData setRealOppHistory(List<Action> realHistory) {
        this.realOppHistory = realHistory;
        return this;
    }

    public NegotiationData setConfiguration(HashMap<String, Object> configuration) {
        this.configuration = configuration;
        return this;
    }

    public void setRootNode(BeliefNode rootNode) {
        this.rootNode = rootNode;
    }

    public void addAgreementUtil(Double agreementUtil) {
        this.agreementUtil = agreementUtil;
        if (agreementUtil > maxReceivedUtil)
            this.maxReceivedUtil = agreementUtil;
    }

    public void addBidUtil(Double bidUtil) {
        if (bidUtil > maxReceivedUtil)
            this.maxReceivedUtil = bidUtil;
    }

    public void setOpponentName(String opponentName) {
        this.opponentName = opponentName;
    }

    public String getOpponentName() {
        return this.opponentName;
    }

    public Double getMaxReceivedUtil() {
        return this.maxReceivedUtil;
    }

    public Double getAgreementUtil() {
        return this.agreementUtil;
    }

    public NegotiationData setBelief(AbstractBelief belief) {
        this.belief = belief;
        return this;
    }

    public NegotiationData setRoot(BeliefNode rootNode) {
        this.rootNode = rootNode;
        return this;
    }

    public AbstractBelief getBelief() {
        return belief;
    }

    public BeliefNode getRootNode() {
        return rootNode;
    }
}
