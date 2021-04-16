package geniusweb.exampleparties.learningagent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import geniusweb.actions.Action;
import geniusweb.actions.EndNegotiation;
import geniusweb.actions.Offer;
import geniusweb.actions.PartyId;
import geniusweb.bidspace.AllBidsList;
import geniusweb.connection.ConnectionEnd;
import geniusweb.inform.ActionDone;
import geniusweb.inform.Agreements;
import geniusweb.inform.Finished;
import geniusweb.inform.Inform;
import geniusweb.inform.Settings;
import geniusweb.inform.YourTurn;
import geniusweb.issuevalue.Bid;
import geniusweb.party.Capabilities;
import geniusweb.profile.Profile;
import geniusweb.profile.utilityspace.LinearAdditive;
import geniusweb.progress.ProgressTime;
import geniusweb.references.Parameters;
import geniusweb.references.ProfileRef;
import geniusweb.references.ProtocolRef;
import geniusweb.references.Reference;
import tudelft.utilities.listener.DefaultListenable;
import tudelft.utilities.logging.Reporter;

public class LearningAgentTest {

	private static final PartyId PARTY1 = new PartyId("party1");
	private static final String SAOP = "SAOP";
	private static final String LEARN = "Learn";
	private static final PartyId otherparty = new PartyId("opponentAgent");
	private static final String PROFILE = "src/test/resources/testprofile.json";
	private final static ObjectMapper jackson = new ObjectMapper();

	private LearningAgent party;
	private final TestConnection connection = new TestConnection();
	private final ProgressTime progress = mock(ProgressTime.class);
	private Settings settingsSAOP;
	private Settings settingsLearn;
	private LinearAdditive profile;
	private Parameters parameters = new Parameters();

	@Before
	public void before() throws JsonParseException, JsonMappingException, IOException, URISyntaxException {
		party = new LearningAgent();
		parameters = parameters.with("persistentstate", "6bb5f909-0079-43ac-a8ac-a31794391074");
		parameters = parameters.with("negotiationdata", Arrays.asList(("12b5f909-0079-43ac-a8ac-a31794391012")));
		settingsSAOP = new Settings(PARTY1, new ProfileRef(new URI("file:" + PROFILE)), new ProtocolRef(SAOP), progress,
				parameters);
		settingsLearn = new Settings(PARTY1, new ProfileRef(new URI("file:" + PROFILE)), new ProtocolRef(LEARN),
				progress, parameters);

		String serialized = new String(Files.readAllBytes(Paths.get(PROFILE)), StandardCharsets.UTF_8);
		profile = (LinearAdditive) jackson.readValue(serialized, Profile.class);

		Path tmpDirectory = Paths.get(System.getProperty("java.io.tmpdir"), "geniusweb");
		if (Files.isDirectory(tmpDirectory)) {
			deleteDirectoryRecursion(tmpDirectory);
			tmpDirectory.toFile().mkdir();
		} else {
			tmpDirectory.toFile().mkdir();
		}
	}

	@Test
	public void smokeTest() {
	}

	@Test
	public void getDescriptionTest() {
		assertNotNull(party.getDescription());
	}

	@Test
	public void getCapabilitiesTest() {
		Capabilities capabilities = party.getCapabilities();
		assertFalse("party does not define protocols", capabilities.getBehaviours().isEmpty());
	}

	@Test
	public void testInformConnection() {
		party.connect(connection);
		// agent should not start acting just after an inform
		assertEquals(0, connection.getActions().size());
	}

	@Test
	public void testInformSettings() {
		party.connect(connection);
		connection.notifyListeners(settingsSAOP);
		assertEquals(0, connection.getActions().size());
	}

	@Test
	public void testInformAndConnection() {
		party.connect(connection);
		party.notifyChange(settingsSAOP);
		assertEquals(0, connection.getActions().size());
	}

	@Test
	public void testReceiveSingleBid() {
		party.connect(connection);
		party.notifyChange(settingsSAOP);
		Bid bidOpponent = findBadBid();

		party.notifyChange(new ActionDone(new Offer(otherparty, bidOpponent)));

		// party should not act at this point
		assertEquals(0, connection.getActions().size());
	}

	@Test
	public void testOtherWalksAway() {
		party.connect(connection);
		party.notifyChange(settingsSAOP);

		party.notifyChange(new ActionDone(new EndNegotiation(otherparty)));

		// party should not act at this point
		assertEquals(0, connection.getActions().size());
	}

	@Test
	public void testAgentHasFirstTurn() {
		party.connect(connection);
		party.notifyChange(settingsSAOP);
		party.notifyChange(new YourTurn());
		assertEquals(1, connection.getActions().size());
		assertTrue(connection.getActions().get(0) instanceof Offer);
	}

	@Test
	public void testAgentFinishedLogs() {
		// this log output is optional, this is to show how to check log
		Reporter reporter = mock(Reporter.class);
		party = new LearningAgent(reporter);
		party.connect(connection);
		party.notifyChange(settingsSAOP);
		Agreements agreements = mock(Agreements.class);
		when(agreements.toString()).thenReturn("agree");
		party.notifyChange(new Finished(agreements));

		verify(reporter).log(eq(Level.INFO), eq("Final outcome:Finished[agree]"));
	}

	@Test
	public void testGetCapabilities() {
		assertTrue(party.getCapabilities().getBehaviours().contains(SAOP));
		assertTrue(party.getCapabilities().getBehaviours().contains(LEARN));
	}

	@Test
	public void testLearningFirstBidAccepted() {
		party.connect(connection);
		party.notifyChange(settingsSAOP);
		party.notifyChange(new YourTurn());
		Bid bid = ((Offer) connection.getActions().get(0)).getBid();
		Agreements agreements = new Agreements(new HashMap<PartyId, Bid>() {
			{
				put(otherparty, bid);
			}
		});
		party.notifyChange(new Finished(agreements));
		party.terminate();

		party.connect(connection);
		party.notifyChange(settingsLearn);
	}

	@Test
	public void testMockNegotiation() {
		party.connect(connection);
		party.notifyChange(settingsSAOP);
		Bid bidOpponent = findBadBid();
		party.notifyChange(new ActionDone(new Offer(otherparty, bidOpponent)));
		party.notifyChange(new YourTurn());
		Bid bid = ((Offer) connection.getActions().get(0)).getBid();
		Agreements agreements = new Agreements(new HashMap<PartyId, Bid>() {
			{
				put(otherparty, bid);
			}
		});
		party.notifyChange(new Finished(agreements));
	}

	@Test
	public void testMockTournament() {
		testMockNegotiation();
		party.terminate();

		party = new LearningAgent();
		party.connect(connection);
		party.notifyChange(settingsLearn);
		party.terminate();

		party = new LearningAgent();
		testMockNegotiation();
		party.terminate();
	}

	private Bid findBadBid() {
		for (Bid bid : new AllBidsList(profile.getDomain())) {
			if (profile.getUtility(bid).compareTo(BigDecimal.valueOf(0.3)) < 0) {
				return bid;
			}
		}
		throw new IllegalStateException("Test can not be done: there is no bad bid with utility<0.2");
	}

	private void deleteDirectoryRecursion(Path path) throws IOException {
		if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
			try (DirectoryStream<Path> entries = Files.newDirectoryStream(path)) {
				for (Path entry : entries) {
					deleteDirectoryRecursion(entry);
				}
			}
		}
		Files.delete(path);
	}
}

/**
 * A "real" connection object, because the party is going to subscribe etc, and
 * without a real connection we would have to do a lot of mocks that would make
 * the test very hard to read.
 *
 */
class TestConnection extends DefaultListenable<Inform> implements ConnectionEnd<Inform, Action> {
	private List<Action> actions = new LinkedList<>();

	@Override
	public void send(Action action) throws IOException {
		actions.add(action);
	}

	@Override
	public Reference getReference() {
		return null;
	}

	@Override
	public URI getRemoteURI() {
		return null;
	}

	@Override
	public void close() {

	}

	@Override
	public Error getError() {
		return null;
	}

	public List<Action> getActions() {
		return actions;
	}

}
