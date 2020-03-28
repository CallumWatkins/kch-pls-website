package test.java;

import main.java.com.projectBackEnd.Entities.News.Hibernate.News;
import main.java.com.projectBackEnd.Entities.News.Hibernate.NewsManager;
import main.java.com.projectBackEnd.Entities.News.Hibernate.NewsManagerInterface;
import main.java.com.projectBackEnd.HibernateUtility;

import javax.persistence.PersistenceException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;

/**
 * Test class to extensively unit test interactions between software and the News table in the database.
 */
public class NewsManagerTest {
    
    public static ConnectionLeakUtil connectionLeakUtil = null;
    public static NewsManagerInterface newsManager = null;

    /**
     * Prior to running, database information location is set and a singleton newsManager is acquired for testing on
     */
    @BeforeAll
    public static void setUpDatabase() {
        HibernateUtility.setResource("testhibernate.cfg.xml");
        newsManager = NewsManager.getNewsManager();
        connectionLeakUtil = new ConnectionLeakUtil();
    }

    /**
     * After the test, the factory is shut down and we can see if any connections were leaked with the Database.
     */
    @AfterAll
    public static void assertNoLeaks() {
        HibernateUtility.shutdown();
        connectionLeakUtil.assertNoLeaks();
    }

    /**
     * Prior to each test, we'll delete all the news tuples from the table.
     */
    @BeforeEach
    public void setUp() {
        newsManager.deleteAll();
    }

//======================================================================================================================
    //Testing the News Creation Constructors
    //public News(Date date, boolean pinned, String description, String title, boolean urgent, String content, String slug)

    /**
     * Testing that creating a news object correctly assigns all the fields with expected values
     */
    @Test
    public void testCreateValidNews() {
        News validNews = new News(new Date(12343212L), false,
                "description", "title", false, "content", "slug1");
        assertEquals("description", validNews.getDescription());
        assertEquals("title", validNews.getTitle());
        assertEquals("slug1", validNews.getSlug());
        assertEquals("content", validNews.getContent());
    }

    /**
     * Testing that News object with null values will still be created
     */
    @Test
    public void testCreateNullValuesNews() {
        News news1 = new News(null, false,
                null, null, false, null, null);
        assertNotNull(news1);
        assertNull(news1.getDate());
        assertNull(news1.getTitle());
    }

    /**
     * Testing that news objects with empty string values will still be created
     */
    @Test
    public void testCreateEmptyValuesNews() {
        News news1 = new News(new Date(), false,
                "", "", false, "", "");
        assertNotNull(news1);
        assertEquals("", news1.getContent());
        assertEquals("", news1.getTitle());
    }

    /**
     * Testing that two news objects created in the same way have the same property values
     */
    @Test
    public void testEqualNews() {
        News news1 = new News(new Date(12343212L), false,
                "desc213ription1", "ti321tle1", false, "con321tent1", "slug1");
        News news2 = new News(new Date(12343212L), false,
                "desc213ription1", "ti321tle1", false, "con321tent1", "slug1");
        assertThat(news1, samePropertyValuesAs(news2));
    }

    /**
     * Testing that once a news object copies another, they both have the same property values
     */
    @Test
    public void testNewsCopying() {
        News news1 = new News(new Date(12343212L), false,
                "description", "title", false, "content", "slug1");
        News news2 = new News(null, false,
                "desc213r321321iption1", "ti321tle1", true, "null", "slug4321");
        news1.copy(news2);
        assertThat(news1, samePropertyValuesAs(news2));
    }

    //Testing NewsManagerInterface: getAllNews

    /**
     * Test the fill database method below, and the getAllNews method to show that all are
     * successfully added.
     * Expected: All the medicines from the list are added successfully.
     */
    @Test
    public void testFillingAndGetting() {
        fillDatabase(getListOfNews());
        assertEquals(getListOfNews().size(), newsManager.getAllNews().size());
    }

    /**
     * Testing the order returned by the getAllNews
     */
    @Test
    public void testOrderOfNews() {
        fillDatabase(getListOfNews());
        List<News> allNews = newsManager.getAllNews();
        assertEquals("title6", allNews.get(0).getTitle());
        assertEquals("title4", allNews.get(1).getTitle());
    }

    //Testing NewsManagerInterface: deleteAll

    /**
     * Testing a database can have deleteAll run on it, even if it is empty
     * Expected: The number of entries in the database remains zero.
     */
    @Test
    public void testDeleteAllEmptyDatabase() {
        newsManager.deleteAll();
        assertEquals(0, newsManager.getAllNews().size());
        newsManager.deleteAll();
        assertEquals(0, newsManager.getAllNews().size());
    }

    /**
     * Testing a database will be flushed by the deleteAll method used between tests
     * Expected: The entries will disappear from the database.
     */
    @Test
    public void testDeleteAllFilledDatabase() {
        fillDatabase(getListOfNews());
        assertEquals(getListOfNews().size(), newsManager.getAllNews().size());
        newsManager.deleteAll();
        assertEquals(0, newsManager.getAllNews().size());
    }

    //Testing NewsManagerInterface: addNews

    /**
     * Test adding a regular News article to the database.
     * Expected: A new news article is added to the database, regardless of constructor used.
     */
    @Test
    public void testAddNews() {
        newsManager.addNews(new News(new Date(12343212L), false,
                "''##DROP TABLE';;'", "sameTitle", false, "con321tent1", "slug1"));
        newsManager.addNews(new News(231, new Date(123432124L), false,
                "desc213ription1", "sameTitle", false, "c2on321tent1", "slug2"));
        assertEquals(2, newsManager.getAllNews().size());
        assertEquals("sameTitle", newsManager.getAllNews().get(0).getTitle());

    }

    /**
     * Adding a news object with null values will not be added to the database.
     * Expected: The size remains unchanged.
     */
    @Test
    public void testAddNewsWithNullValues() {
        int sizeBefore = newsManager.getAllNews().size();
        newsManager.addNews(new News(null,true, null, null, false, null, null));
        assertEquals(sizeBefore, newsManager.getAllNews().size());
    }

    /**
     * Testing adding news articles with empty values
     * Expected: The article is added.
     */
    @Test
    public void testAddNewsWithEmptyStringValues() {
        int sizeBefore = newsManager.getAllNews().size();
        newsManager.addNews(new News(new Date(),true, "   ", "", false, "", ""));
        assertEquals(sizeBefore+1, newsManager.getAllNews().size());
    }
    /**
     * Testing adding news articles which share the same slug. This should not be possible.
     */
    @Test
    public void testDuplicateSlugAddition() {
        int sizeBefore = newsManager.getAllNews().size();
        newsManager.addNews(new News(new Date(12343212L), false,
                "desc213ription1", "ti321t      le1", false, "con321tent1", "slug1"));
        newsManager.addNews(new News(new Date(12343212L), false,
                "desc213ription1", "ti321tle1", false, "con321tent1", "slug1"));
        assertEquals(sizeBefore+1, newsManager.getAllNews().size());
    }

    //Testing NewsManagerInterface: getByPrimaryKey

    /**
     * Testing that news objects can be found and made from their primary key.
     * Expected: The news found shares the same values as the news in the database.
     */
    @Test
    public void testGetByPrimaryKey() {
        fillDatabase(getListOfNews());
        News foundNews = newsManager.getAllNews().get(0);
        int newsPK = foundNews.getPrimaryKey();
        News foundNewsFromDB = newsManager.getByPrimaryKey(newsPK);

        assertThat(foundNews, samePropertyValuesAs(foundNewsFromDB));
    }

    /**
     * Testing that attempting to obtain a news article with a primary key that doesn't exist returns null
     */
    @Test
    public void testGetIllegalPrimaryKey() {
        assertNull(newsManager.getByPrimaryKey(-1));
    }

    //Testing NewsManagerInterface: delete

    /**
     * Test that deleting a news article from the database reduces the number of news articles
     * in the database.
     */
    @Test
    public void testDelete() {
        fillDatabase(getListOfNews());
        newsManager.delete(newsManager.getAllNews().get(1).getPrimaryKey());
        assertEquals( getListOfNews().size()-1, newsManager.getAllNews().size());
        newsManager.delete(newsManager.getAllNews().get(1).getPrimaryKey());
        assertEquals(getListOfNews().size()-2, newsManager.getAllNews().size());
    }

    /**
     * Test deleting a primary key which is not in the database.
     * Expected: The database remains unchanged and an error is thrown.
     */
    @Test
    public void testWithDeleteUnfoundPrimaryKey() {
        int previousSize = newsManager.getAllNews().size();
        try {
            newsManager.delete(-1);
            fail();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            assertEquals(newsManager.getAllNews().size(), previousSize);
            // Check that nothing has been removed
        }
    }

    /**
     * Test the correct article was infact deleted when using delete
     */
    @Test
    public void testCorrectNewsDeletedUsingPrimaryKey() {
        News toBeDeleted = newsManager.addNews(new News(new Date(12343212L), false,
                "getting deleted", "soon won't exist", false, "f", "slug1"));
        News alsoAdded = newsManager.addNews(new News(new Date(12343212L), false,
                "description", "title", false, "content", "slug3"));
        assertEquals(2, newsManager.getAllNews().size());
        newsManager.delete(toBeDeleted.getPrimaryKey());
        assertEquals(1, newsManager.getAllNews().size());
        News leftoverArticle = newsManager.getAllNews().get(0);
        assertEquals(alsoAdded.getSlug(), leftoverArticle.getSlug());
        assertNull(newsManager.getByPrimaryKey(toBeDeleted.getPrimaryKey()));
    }

    //Testing NewsManagerInterface: update

    /**
     * Testing updating one of the existing news articles into another one
     */
    @Test
    public void testUpdateNews() {
        fillDatabase(getListOfNews());
        int id = newsManager.getAllNews().get(0).getPrimaryKey();
        News replacementNews = new News(id, new Date(12343212L), true,
                "changedDescription", "newTitle", false, "content1", "slug9");
        newsManager.update(replacementNews);

        News newsInDB = newsManager.getByPrimaryKey(id);
        assertEquals(replacementNews.getDescription(), newsInDB.getDescription());
        assertEquals(replacementNews.getTitle(), newsInDB.getTitle());
    }

    /**
     * Testing updating a news article so it violates the unique - it should throw an error!
     */
    @Test
    public void testUpdateNewsWithDupeSlug() {
        fillDatabase(getListOfNews());
        int previousSize = newsManager.getAllNews().size();
        int id = newsManager.getAllNews().get(0).getPrimaryKey();
        News replacementNews = new News(id ,new Date(12343212L), true, "changedDescrption",
                "newTitle", false, "content1", "slug1");
        try {
            newsManager.update(replacementNews);
            fail();
        } catch (PersistenceException e) {
            e.printStackTrace();
            assertEquals(newsManager.getAllNews().size(), previousSize);
        }
    }

    /**
     * Test update a news article with nulls
     */
    @Test
    public void testUpdateNewsWithNullValues() {
        fillDatabase(getListOfNews());
        int previousSize = newsManager.getAllNews().size();
        int id = newsManager.getAllNews().get(0).getPrimaryKey();
        News replacementNews = new News(id ,null, true, null,
                null, false, null, null);
        try {
            newsManager.update(replacementNews);
            fail();
        } catch (PersistenceException e) {
            e.printStackTrace();
            assertEquals(newsManager.getAllNews().size(), previousSize);
        }
    }

    /**
     * Test update a news article with empty string values
     */
    @Test
    public void testUpdateNewsWithEmptyStringValues() {
        fillDatabase(getListOfNews());
        int id = newsManager.getAllNews().get(0).getPrimaryKey();
        News replacementNews = new News(id, new Date(12343212L), true,
                "", "", false, "", "");
        newsManager.update(replacementNews);

        News newsInDB = newsManager.getByPrimaryKey(id);
        assertEquals(replacementNews.getDescription(), newsInDB.getDescription());
        assertEquals(replacementNews.getTitle(), newsInDB.getTitle());
    }

    //Testing NewsManagerInterface: getNewsBySlug

    /**
     * Test that unique slugs can be used to obtain the News article from the database.
     */
    @Test
    public void testGetNewsBySlug() {
        fillDatabase(getListOfNews());
        newsManager.addNews(new News(new Date(12343212L), false,
                "getting deleted", "soon won't exist", false, "f", "unique-slug"));
        News found = newsManager.getNewsBySlug("unique-slug");
        assertNotNull(found);
        assertEquals("getting deleted", found.getDescription());
    }

    /**
     * List to fill the database with example News objects
     * @return example objects in a list
     */
    private static ArrayList<News> getListOfNews() {
        ArrayList<News> listOfNews = new ArrayList<>();

        listOfNews.add(new News(new Date(12343212L), false, "description1", "title1",
                false, "content1", "slug1"));
        listOfNews.add(new News(new Date(12343214L), false, "description2", "title2",
                false, "content2", "slug2"));
        listOfNews.add(new News(new Date(12343215L), false, "description3", "title3",
                false, "content3", "slug3"));
        listOfNews.add(new News(new Date(12343218L), true, "description4", "title4",
                false, "content4", "slug4"));
        listOfNews.add(new News(new Date(12343400L), false, "description5", "title5",
                false, "content5", "slug5"));
        listOfNews.add(new News(new Date(12343900L), false, "description6", "title6",
                true, "content6", "slug6"));
        listOfNews.add(new News(new Date(15343212L), false, "description7", "title7",
                false, "content7", "slug7"));

        return listOfNews;
    }

    /**
     * Fill the databaes with a list of news articles
     * @param listOfNews The list of news articles to fill the database with.
     */
    private void fillDatabase(ArrayList<News> listOfNews) {
        for (int i = 0; i<listOfNews.size(); ++i) newsManager.addNews(listOfNews.get(i));
    }
}