package geniusweb.custom.helper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.websocket.DeploymentException;

import geniusweb.actions.PartyId;
import geniusweb.inform.Agreements;
import geniusweb.issuevalue.Bid;
import geniusweb.profile.Profile;
import geniusweb.profile.utilityspace.LinearAdditive;
import geniusweb.profile.utilityspace.LinearAdditiveUtilitySpace;
import geniusweb.profile.utilityspace.UtilitySpace;
import geniusweb.profileconnection.ProfileConnectionFactory;
import geniusweb.profileconnection.ProfileInterface;
import geniusweb.protocol.NegoSettings;
import geniusweb.protocol.NegoState;
import geniusweb.protocol.session.SessionResult;
import geniusweb.protocol.session.SessionSettings;
import geniusweb.protocol.session.SessionState;
import geniusweb.protocol.tournament.allpermutations.AllPermutationsSettings;
import geniusweb.protocol.tournament.allpermutations.AllPermutationsState;
import geniusweb.protocol.tournament.allpermutationslearn.AllPermutationsLearnSettings;
import geniusweb.references.Parameters;
import geniusweb.references.PartyWithParameters;
import geniusweb.references.PartyWithProfile;
import geniusweb.references.ProfileRef;
import tudelft.utilities.immutablelist.ImmutableList;
import tudelft.utilities.logging.ReportToLogger;

public class ResultsLogger {
    private String timestamp;
    private List<Result> results = new ArrayList<Result>();
    private NegoState state = null;

    public ResultsLogger(NegoState state, String temporalState) {
        super();
        this.state = state;
        this.setTimestamp(temporalState);
        this.generateResults(this.state);
    }

    private void generateResults(NegoState state) {
        List<SessionResult> allSessions = new ArrayList<SessionResult>();
        if (state instanceof SessionState) {
            SessionState sessionState = (SessionState) state;
            allSessions.add(sessionState.getResult());
        }

        if (state instanceof AllPermutationsState) {
            AllPermutationsState tournamentStates = (AllPermutationsState) state;
            allSessions.addAll(tournamentStates.getResults());
        }

        for (SessionResult sess : allSessions) {
            Agreements agreements = sess.getAgreements();
            Map<PartyId, PartyWithProfile> allParties = sess.getParticipants();
            Map<PartyId, Bid> allAgreements = agreements.getMap();
            List<Result> collectedResults = allParties.keySet().stream()
                    .map(k -> new Result(k, allParties.get(k), allAgreements.get(k))).collect(Collectors.toList());
            results.addAll(collectedResults);
        }

    }

    public NegoState getState() {
        return state;
    }

    public void setState(NegoState state) {
        this.state = state;
    }

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String temporalState) {
        this.timestamp = temporalState;
    }

    public class Result {

        private String party;
        private Double util;
        private PartyWithProfile pwp;
        private Bid bid;

        private String version;
        private Parameters params;

        public Result(PartyId partyId, PartyWithProfile pwp, Bid aggreeBid) {
            this.setVersion(partyId.getName());
            this.pwp = pwp;
            String[] rawPartyString = pwp.getParty().getPartyRef().getURI().toString().split("\\.");
            this.setParty(rawPartyString[rawPartyString.length - 1]);
            ;
            this.setBid(aggreeBid);
            ;
            this.setParams(pwp.getParty().getParameters());

            try {
                // PartyWithParameters party = pwp.getParty();
                ProfileRef profile = pwp.getProfile();
                ProfileInterface profileint = ProfileConnectionFactory.create(profile.getURI(),
                        new ReportToLogger(profile.toString()));
                UtilitySpace utilitySpace = ((UtilitySpace) profileint.getProfile());
                this.util = this.bid != null ? utilitySpace.getUtility(this.bid).doubleValue() : 0.0;
            } catch (IOException e) {
                throw new IllegalStateException(e);
            } catch (DeploymentException e) {
                this.util = -1.0;
                e.printStackTrace();
            }
        }

        public Parameters getParams() {
            return params;
        }

        public void setParams(Parameters params) {
            this.params = params;
        }

        public String getParty() {
            return party;
        }

        public void setParty(String party) {
            this.party = party;
        }

        public Double getUtil() {
            return util;
        }

        public void setUtil(Double util) {
            this.util = util;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public Bid getBid() {
            return bid;
        }

        public void setBid(Bid bid) {
            this.bid = bid;
        }

    }
}
