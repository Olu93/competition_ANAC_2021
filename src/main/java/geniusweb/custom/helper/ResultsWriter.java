package geniusweb.custom.helper;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.websocket.DeploymentException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import geniusweb.actions.PartyId;
import geniusweb.inform.Agreements;
import geniusweb.issuevalue.Bid;
import geniusweb.issuevalue.Value;
import geniusweb.profile.utilityspace.UtilitySpace;
import geniusweb.profileconnection.ProfileConnectionFactory;
import geniusweb.profileconnection.ProfileInterface;
import geniusweb.protocol.NegoState;
import geniusweb.protocol.session.SessionResult;
import geniusweb.protocol.session.SessionState;
import geniusweb.protocol.tournament.allpermutations.AllPermutationsState;
import geniusweb.references.Parameters;
import geniusweb.references.PartyWithProfile;
import geniusweb.references.ProfileRef;
import tudelft.utilities.logging.ReportToLogger;

public class ResultsWriter {
    private String startTime;
    private NegoState state = null;
    private FileWriter collectorFile;
    private final static ObjectMapper jacksonReader = new ObjectMapper();
    private String settingRef;

    public ResultsWriter(NegoState state, String startTime, String settingRef, FileWriter collectorFile) {
        super();
        this.state = state;
        this.setSettingRef(settingRef);
        this.setStartTime(startTime);
        this.setCollectorFile(collectorFile);
    }

    public String getSettingRef() {
        return settingRef;
    }

    public void setSettingRef(String settingRef) {
        this.settingRef = settingRef;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public FileWriter getCollectorFile() {
        return collectorFile;
    }

    public void setCollectorFile(FileWriter collectorFile) {
        this.collectorFile = collectorFile;
    }

    public void writeSession(Integer sessionNum, SessionResult sess, String sessionStart) {
        Agreements agreements = sess.getAgreements();
        Map<PartyId, PartyWithProfile> allParties = sess.getParticipants();
        Map<PartyId, Bid> allAgreements = agreements.getMap();
        List<Result> collectedResults = allParties.keySet().stream()
                .map(k -> new Result(sessionNum, k, allParties.get(k), allAgreements.get(k))
                        .setSessionStart(sessionStart).setTournamentStart(this.getStartTime())
                        .setSettingFile(this.getSettingRef()))
                .collect(Collectors.toList());
        List<String> jsonLines = collectedResults.stream().map(t -> {
            try {
                return jacksonReader.writeValueAsString(t) + "\n";
            } catch (JsonProcessingException e) {
                System.out.println(t);
                e.printStackTrace();
                return "";
            }
        }).collect(Collectors.toList());
        jsonLines.stream().filter(t -> t.isEmpty() == false).forEach(t -> {
            try {
                this.collectorFile.append(t).flush();
            } catch (IOException e) {
                System.out.println(t);
                e.printStackTrace();
            }
        });
    }

    public NegoState getState() {
        return state;
    }

    public void setState(NegoState state) {
        this.state = state;
    }

    public class Result {

        private String party;
        private Double utility;
        private PartyWithProfile pwp;
        private Map<String, Value> bid;
        private Integer session;
        private String tournamentStart;
        private String sessionStart;
        private String settingFile;

        private String version;
        private Parameters params;

        public Result(Integer sessionNum, PartyId partyId, PartyWithProfile pwp, Bid aggreeBid) {
            this.setSession(sessionNum);
            this.setVersion(partyId.getName());
            this.pwp = pwp;
            String[] rawPartyString = pwp.getParty().getPartyRef().getURI().toString().split("\\.");
            this.setParty(rawPartyString[rawPartyString.length - 1]);
            this.setBid(aggreeBid != null ? aggreeBid.getIssueValues() : new HashMap<String, Value>());
            this.setParams(pwp.getParty().getParameters());

            try {
                // PartyWithParameters party = pwp.getParty();
                ProfileRef profile = pwp.getProfile();
                ProfileInterface profileint = ProfileConnectionFactory.create(profile.getURI(),
                        new ReportToLogger(profile.toString()));
                UtilitySpace utilitySpace = ((UtilitySpace) profileint.getProfile());
                this.utility = aggreeBid != null ? utilitySpace.getUtility(aggreeBid).doubleValue() : 0.0;
            } catch (IOException e) {
                throw new IllegalStateException(e);
            } catch (DeploymentException e) {
                this.utility = -1.0;
                e.printStackTrace();
            }
        }

        public String getSettingFile() {
            return settingFile;
        }

        public Result setSettingFile(String settingFile) {
            this.settingFile = settingFile;
            return this;
        }

        public String getTournamentStart() {
            return tournamentStart;
        }

        public Result setTournamentStart(String tournamentStart) {
            this.tournamentStart = tournamentStart;
            return this;
        }

        public String getSessionStart() {
            return sessionStart;
        }

        public Result setSessionStart(String sessionStart) {
            this.sessionStart = sessionStart;
            return this;
        }

        public Integer getSession() {
            return session;
        }

        public void setSession(Integer session) {
            this.session = session;
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

        public Double getUtility() {
            return utility;
        }

        public void setUtility(Double util) {
            this.utility = util;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public Map<String, Value> getBid() {
            return bid;
        }

        public void setBid(Map<String, Value> map) {
            this.bid = map;
        }

    }
}
