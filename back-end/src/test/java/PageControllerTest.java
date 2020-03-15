package test.java;


import io.micronaut.test.annotation.MicronautTest;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import main.java.com.projectBackEnd.*;

import main.java.com.projectBackEnd.Entities.Page.*;
import main.java.com.projectBackEnd.Entities.Site.*;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;

@MicronautTest
public class PageControllerTest {

    @Inject
    @Client("/")
    HttpClient client;

    static SiteManagerInterface siteManager;
    static PageManagerInterface pageManager;

    @BeforeAll
    public static void setUpDatabase() {
        HibernateUtility.setResource("testhibernate.cfg.xml");
        siteManager = SiteManager.getSiteManager();
        pageManager = PageManager.getPageManager();
    }

    @AfterAll
    public static void closeDatabase() {
        HibernateUtility.shutdown();
    }

    @BeforeEach
    public void setUp() {
        siteManager.deleteAll();
        //Automatically deletes all pages too due to cascade, but:
        pageManager.deleteAll();
    }

    @Test
    public void testAddingRegularPage() {
        addSite("testSiteA");
        HttpResponse response = addPage("testSiteA", "nutrition/slu!#g", 1, "Title", "nutri!tion/information");
        assertNotNull(pageManager.getPageBySiteAndSlug("testSiteA", "nutrition/slu!#g"));

        //String id =  getEUrl(response);
        Page testPage = getPage("testSiteA","nutrition/slu!#g");
        assertEquals("Title", testPage.getTitle());
    }

    @Test
    public void testAddingPageWithNulls() {
        assertThrows(NullPointerException.class, () -> {
            HttpResponse response = addPage(null, null, null, null, null);
        });
    }

    @Test
    public void testAddingNullSitePage() {
        addSite("TestSite");
        HttpRequest request = HttpRequest.POST(("/sites/"+ "TestSite" +"/pages"), new PageAddCommand(null, "slug", 3, "", ""));
        HttpClientResponseException thrown = assertThrows(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(request);
        });

    }

    @Test
    public void testAddingNullIndexPage() {
        addSite("testSiteA");
        HttpRequest request = HttpRequest.POST(("/sites/"+ "testSiteA" +"/pages"), new Page("testSiteA", "slug", null, "", ""));
        HttpClientResponseException thrown = assertThrows(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(request);
        }); //Shouldn't be allowed!
    }
    @Test
    public void testUpdatePageTitle() {
        addSite("testSiteA");
        HttpResponse response = addPage("testSiteA", "nutrition/slu!#g", 1, "Title", "nutri!tion/information");
        assertNotNull(pageManager.getAllPages().get(0));
        int idOfMadePage = pageManager.getPageBySiteAndSlug("testSiteA", "nutrition/slu!#g").getPrimaryKey();
        //protected HttpResponse putPage(int id, String siteName, String slug, int index, String title, String content) {
        response = putPage(idOfMadePage, "testSiteA", "nutrition/slu!#g", 1, "newTitle", "nutri!tion/information");
        assertEquals("newTitle",pageManager.getPageBySiteAndSlug("testSiteA", "nutrition/slu!#g").getTitle());
        Page testPage = getPage("testSiteA", "nutrition/slu!#g");
        assertEquals("newTitle", testPage.getTitle());
    }
    @Test
    public void testUpdatePageSiteToValid() {
        addSite("testSiteA");
        addSite("testSiteB");
        addPage("testSiteA", "nutrition/slu!#g", 1, "Title", "nutri!tion/information");
        //gets id of above page
        int idOfMadePage = pageManager.getPageBySiteAndSlug("testSiteA", "nutrition/slu!#g").getPrimaryKey();

        putPage(idOfMadePage, "testSiteA", "nutrition/slu!#g", 1, "newTitle", "nutri!tion/information");

        Page testPage = getPage("testSiteA", "nutrition/slu!#g");
        assertEquals("newTitle", testPage.getTitle());
    }
    @Test
    public void testUpdatePageSlugToValid() {
        addSite("testSiteA");
        addSite("testSiteB");
        addPage("testSiteA", "nutrition/slu!#g", 1, "Title", "nutri!tion/information");
        //gets id of above page
        int idOfMadePage = pageManager.getPageBySiteAndSlug("testSiteA", "nutrition/slu!#g").getPrimaryKey();

        putPage(idOfMadePage, "testSiteA", "nutrition/sl123u!#g", 1, "newTitle", "nutri!tion/information");

        Page testPage = getPage("testSiteA", "nutrition/sl123u!#g");
        assertEquals("newTitle", testPage.getTitle());
        HttpClientResponseException thrown = assertThrows(HttpClientResponseException.class, () -> {
            getPage("testSiteA", "nutrition/slu!#g");
        });
        assertNull(pageManager.getPageBySiteAndSlug("testSiteA", "nutrition/slu!#g"));
    }
    @Test
    public void testNonExistingPageReturns404() {
        HttpClientResponseException thrown = assertThrows(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(HttpRequest.GET("nothing"));
        });
        assertNotNull(thrown.getResponse());
        assertEquals(HttpStatus.NOT_FOUND, thrown.getStatus());
    }
    @Test
    public void testDeleteNonExistentPage() {
        HttpClientResponseException thrown = assertThrows(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(HttpRequest.DELETE("nothing"));
        });
    }
    @Test
    public void testUpdatePageToInvalid() {
        addSite("testSiteA");
        HttpResponse response = addPage("testSiteA", "nutrition/slu!#g", 1, "Title", "nutri!tion/information");
        int idOfMadePage = pageManager.getPageBySiteAndSlug("testSiteA", "nutrition/slu!#g").getPrimaryKey();
        HttpClientResponseException thrown = assertThrows(HttpClientResponseException.class, () -> {
            putPage(idOfMadePage, "notvalid", "nutrition/slu!#g", 1, "newTitle", "nutri!tion/information");
        });
        assertNotNull(pageManager.getPageBySiteAndSlug("testSiteA", "nutrition/slu!#g"));
    }

    @Test
    public void updateToDuplicateKeysPage() {
        addSite("testSiteA");
        HttpResponse response = addPage("testSiteA", "nutrition/slu!#g", 1, "Title", "nutri!tion/information");
        response = addPage("testSiteA", "sameKey", 1, "Title", "nutri!tion/information");
        int idOfMadePage = pageManager.getPageBySiteAndSlug("testSiteA", "nutrition/slu!#g").getPrimaryKey();
        HttpClientResponseException thrown = assertThrows(HttpClientResponseException.class, () -> {
            putPage(idOfMadePage, "notvalid", "sameKey", 1, "newTitle", "nutri!tion/information");
        });
        assertNotNull(pageManager.getPageBySiteAndSlug("testSiteA", "nutrition/slu!#g"));
    }

    @Test
    public void testDeletePage() {
        addSite("testSiteA");
        HttpResponse response = addPage("testSiteA", "nutrition/slu!#g", 1, "Title", "nutri!tion/information");
        URI pLoc = pageLocation("testSiteA", "nutrition/slu!#g");
        HttpRequest request = HttpRequest.DELETE(pLoc.toString());
        client.toBlocking().exchange(request);
        assertNull(pageManager.getPageBySiteAndSlug("testSiteA", "nutrition/slu!#g"));
    }

    @Test
    public void testPatchingPageIndex() {
        addSite("testSiteA");
        addPage("testSiteA", "nutrition/slu!#g", 9, "Title", "nutri!tion/information");
        addPage("testSiteA", "anotherPage", 12, "Title", "nutri!tion/information");
        addPage("testSiteA", "coolPage", 20, "Title", "nutri!tion/information");
        addPage("testSiteA", "Paaage", 13, "Title", "nutri!tion/information");
        //public PagePatchCommand(int id, String slug, int index) {
        List<Page> allPagesWithID = pageManager.getAllPages();
        List<PagePatchCommand> input = new ArrayList<>();
        for(int i = 0; i < allPagesWithID.size(); ++i) {
            Page currentPage = allPagesWithID.get(i);
            input.add(new PagePatchCommand(currentPage.getPrimaryKey(), currentPage.getSlug(), i));
        } //Will order all pages from 0-4;

        HttpRequest request = HttpRequest.PATCH("/sites/"+ "testSiteA" +"/page-indices", input);
        client.toBlocking().exchange(request);
        //TODO Add the correct parameter for this!
        //Updates all the pages to have a new index.
        //@Patch("/{name}/page-indices")
        //public HttpResponse<Page> patchPage(String name, @Body List<PagePatchCommand> patchCommandList){
        allPagesWithID = pageManager.getAllPages();
        for(int i = 0; i < allPagesWithID.size(); ++i) {
            assertEquals(i, allPagesWithID.get(i).getIndex());
        }

    }

    protected HttpResponse putPage(int id, String siteName, String slug, int index, String title, String content) {
        String pSlug = pageManager.getByPrimaryKey(id).getSlug();
        URI pLoc = pageLocation(siteName, pSlug);
        HttpRequest request = HttpRequest.PUT(pLoc, new PageUpdateCommand(id, siteName, slug, index, title, content));
        return client.toBlocking().exchange(request);
    }

    protected HttpResponse addPage(String siteName, String slug, Integer index, String title, String content) {
        URI sLoc = location(siteName);
        HttpRequest request = HttpRequest.POST((sLoc +"/pages"), new PageAddCommand(siteName, slug, index, title, content));
        HttpResponse response = client.toBlocking().exchange(request);
        return response;

    }
    protected Page getPage(String name, String pageName) {
        URI loc = pageLocation(name, pageName);

        HttpRequest request = HttpRequest.GET(loc);
        return client.toBlocking().retrieve(request, Page.class);
    }
    protected URI pageLocation(String siteName, String pageName) {
        String encodedSlug = null;
        String encodedPage = null;
        try {
            encodedSlug = URLEncoder.encode(siteName, java.nio.charset.StandardCharsets.UTF_8.toString());
            encodedPage = URLEncoder.encode(pageName, java.nio.charset.StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            return null;
        }
        return URI.create("/sites/" + encodedSlug + "/pages/" + encodedPage);
    }

    //Adding and locating Site methods
    protected HttpResponse addSite(String name) {
        HttpRequest request = HttpRequest.POST("/sites", new SiteAddCommand(name));
        HttpResponse response = client.toBlocking().exchange(request);
        return response;
    }
    protected URI location(String siteName) {
        String encodedSlug = null;
        try {
            encodedSlug = URLEncoder.encode(siteName, java.nio.charset.StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            return null;
        }
        return URI.create("/sites/" + encodedSlug);
    }


}