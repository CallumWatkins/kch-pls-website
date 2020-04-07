package test.java;

import main.java.com.projectBackEnd.*;
import main.java.com.projectBackEnd.Services.Session.Session;
import main.java.com.projectBackEnd.Services.Session.SessionManager;
import main.java.com.projectBackEnd.Services.Session.SessionManagerInterface;
import main.java.com.projectBackEnd.Services.Session.NoSessionException;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

import static org.junit.jupiter.api.Assertions.assertThrows;
/**
 * Test class to extensively unit test interactions between the session entity manager and the Sessions table in the database.
 */
class SessionManagerTest {

    private static ConnectionLeakUtil connectionLeakUtil = null;
	private static SessionManagerInterface sessionManager = null;


	/**
	 * Prior to running, database information is set and a singleton manager is created for testing. Also initialises
	 * the connection leak util attribute
	 */
	@BeforeAll
    static void setUpDatabase() {
		HibernateUtility.setResource("testhibernate.cfg.xml");
		sessionManager = SessionManager.getSessionManager();
        connectionLeakUtil = new ConnectionLeakUtil();
    }

	/**
	 * After the test, the factory is shut down and the LeakUtil can tell us whether any connections leaked.
	 */
    @AfterAll
    static void assertNoLeaks() {
        HibernateUtility.shutdown();
        connectionLeakUtil.assertNoLeaks();
    }
	/**
	 * Prior to each test, we'll delete all the sessions in the sessions table.
	 */
    @BeforeEach
    void setUp() {
	((SessionManager)sessionManager).deleteAll();
    }


	/**
	 * Tests that the manager is able to create a new session with legal information, expects success
	 */
	@Test
	void testGetNewSession() {
		fillDatabase(getTestSessions());
		String token = sessionManager.getNewSession("1",100);
		List<Session> sessions = (List<Session>)((EntityManager) sessionManager).getAll();
		assertEquals(1,sessions.stream().filter(s->(s.getToken().equals(token))).count());
	}


	/**
	 * Tests that the manager is able to verify existing sessions depending on if the correct token is supplied,
	 * expects success
	 */
	@Test
	void testVerifySession() {
		fillDatabase(getTestSessions());
		String token = sessionManager.getNewSession("1",100);
		List<Session> sessions = (List<Session>)((EntityManager) sessionManager).getAll();
		assertEquals(1,sessions.stream().filter(s->(s.getToken().equals(token))).count());
		assertEquals(0,sessions.stream().filter(s->(s.getToken().equals(""))).count());
		assertEquals(0,sessions.stream().filter(s->(s.getToken().equals("random string"))).count());
		assertEquals(0,sessions.stream().filter(s->(s.getToken().equals(null))).count());
	}

	/**
	 * Tests that the manager correctly times out after the timeout threshold is reached, expects success
	 * @throws InterruptedException This exception is not expected but may be thrown
	 */
	@Test
	void testTimeout() throws InterruptedException {
		fillDatabase(getTestSessions());
		String token = sessionManager.getNewSession("1",2);
		List<Session> sessions = (List<Session>)((EntityManager) sessionManager).getAll();
		assertEquals(1,sessions.stream().filter(s->(s.getToken().equals(token))).count());
		Thread.sleep(2005);
		sessionManager.verifySession(token);
		sessions = (List<Session>)((EntityManager) sessionManager).getAll();
		assertEquals(0,sessions.stream().filter(s->(s.getToken().equals(token))).count());
	}


	/**
	 * Tests that the manager is able to terminate an existing session, expects success
	 */
	@Test
	void testTerminateSession() {
		String token = sessionManager.getNewSession("1",100);
		List<Session> sessions = (List<Session>)((EntityManager) sessionManager).getAll();
		assertEquals(1,sessions.size());
		assertEquals(token,sessions.get(0).getToken());
		sessionManager.terminateSession(token);
		assertEquals(0,((EntityManager) sessionManager).getAll().size());
	}

	/**
	 * Tests that the manager is able to add a valid email and retrieve it by it's token, expects success
	 */
	@Test
	void testAddGetEmail(){
		try{
			String token = sessionManager.getNewSession("email@email.com",100);
			assertEquals("email@email.com", sessionManager.getEmail(token));
			sessionManager.terminateSession(token);
		}
		catch(NoSessionException e){
			fail();
		}	
	}

	/**
	 * Attempts to retrieve an email via an empty token, expects an exception to be thrown
	 */
	@Test
	void testGetEmailNotExistEmpty()  {
		assertThrows(NoSessionException.class,() -> {sessionManager.getEmail("");});
	}

	/**
	 * Attempts to retrieve an email via an incorrect token, expects an exception to be thrown
	 */
	@Test
	void testGetEmailNotExistIncorrect() {

		assertThrows(NoSessionException.class,() -> {sessionManager.getEmail("very incorrect token that does not work");});
	}

	/**
	 * Attempts to retrieve an email via a null token, expects an exception to be thrown
	 */
	@Test
	void testGetEmailNotExistNull() {
		assertThrows(NoSessionException.class,() -> {sessionManager.getEmail(null);});
	}


	/**
	 * Quality of life method to create multiple sessions for testing purposes
	 * @return The list of sessions
	 */
	private ArrayList<Session> getTestSessions() {
		ArrayList<Session> sessions = new ArrayList<Session>();
		sessions.add(new Session("1",100));
		sessions.add(new Session("2",100));
		sessions.add(new Session("3",100));

		return sessions;
	}

	/**
	 * Fills the sessions table with a list of Sessions
	 * @param sessionsToAdd The sessions list to add.
	 */
	private void fillDatabase(ArrayList<Session> sessionsToAdd) {
		for(Session s : sessionsToAdd){
			((EntityManager) sessionManager).insertTuple(s);
		}
	}


}
