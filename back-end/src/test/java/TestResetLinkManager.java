package test.java;

import main.java.com.projectBackEnd.*;
import main.java.com.projectBackEnd.Entities.ResetLinks.*;
import main.java.com.projectBackEnd.Entities.User.Hibernate.UserManager;
import main.java.com.projectBackEnd.Entities.User.Hibernate.UserManagerInterface;
import main.java.com.projectBackEnd.Entities.User.Hibernate.EmailExistsException;
import main.java.com.projectBackEnd.Entities.User.Hibernate.InvalidEmailException;
import main.java.com.projectBackEnd.Entities.User.Hibernate.IncorrectNameException;
import org.junit.jupiter.api.*;
import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestResetLinkManager{
        public static ConnectionLeakUtil connectionLeakUtil = null;
        public static ResetLinkManagerInterface linkManager = null;
        public static UserManagerInterface userManager = null;;
        @BeforeAll
        public static void setUpDatabase() {
                HibernateUtility.setResource("testhibernate.cfg.xml");
                linkManager = ResetLinkManager.getResetLinkManager();
		userManager = UserManager.getUserManager();
                //connectionLeakUtil = new ConnectionLeakUtil();

        }

        @AfterAll
        public static void assertNoLeaks() {
                HibernateUtility.shutdown();
                //connectionLeakUtil.assertNoLeaks();
        }

        @BeforeEach
        public void setUp() {
                ((EntityManager)linkManager).deleteAll();                
		((EntityManager)userManager).deleteAll();
        }
        @Test
        public void testCreate(){
                fill();
		try{
			String a = linkManager.create("test@test.com");
			assertNotEquals("",a);
			assertNotNull(a);
        	}
		catch(EmailNotExistException e){
			fail();
		}
	}
        @Test
        public void testCreateEmailNotExist() throws EmailNotExistException{
                fill();
		assertThrows(EmailNotExistException.class,() -> {linkManager.create("test2@test.com");});

        }
        @Test
        public void testGetEmail(){
                fill();
		try{
			String a = linkManager.create("test@test.com");
			assertEquals("test@test.com",linkManager.getEmail(a));
			assertNull(linkManager.getEmail(a+1));
			assertNull(linkManager.getEmail(a+"c"));
			assertNull(linkManager.getEmail(""));
			assertNull(linkManager.getEmail("123"));
			assertNull(linkManager.getEmail(null));
        	}
		catch(EmailNotExistException e){
			fail();
		}
        }
       @Test
        public void testExist(){
                fill();
		try{
			String a = linkManager.create("test@test.com");
			assertTrue(linkManager.exist(a));
			assertFalse(linkManager.exist(a+1));
			assertFalse(linkManager.exist(""));
			assertFalse(linkManager.exist(null));
			assertFalse(linkManager.exist("refwubhybuhwsfw"));
			assertFalse(linkManager.exist("fds"));
        	}
		catch(EmailNotExistException e){
			fail();
		}
        }
        public void fill(){
                try{
                        userManager.addUser("test@test.com","pass","name");
                        userManager.addUser("test1@test.com","pass","name");
                }
                catch(EmailExistsException|InvalidEmailException|IncorrectNameException e){
			System.out.println(e);                        
			fail();
                }
        }

}

		
