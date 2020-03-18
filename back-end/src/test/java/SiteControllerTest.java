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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import main.java.com.projectBackEnd.Entities.User.UserManager;

@MicronautTest
public class SiteControllerTest {

    @Inject
    @Client("/")
    HttpClient client;

    static SiteManagerInterface siteManager;
    static PageManagerInterface pageManager;
    private static String token;
    @BeforeAll
    public static void setUpDatabase() {
        HibernateUtility.setResource("testhibernate.cfg.xml");
        siteManager = SiteManager.getSiteManager();
        pageManager = PageManager.getPageManager();
        try{
        	UserManager.getUserManager().addUser("test@test.com" , "123");
        	token = UserManager.getUserManager().verifyUser("test@test.com" , "123");
        }
        catch(Exception e){
        	fail();
        }  
    }

    @AfterAll
    public static void closeDatabase() {
        try{
        	UserManager.getUserManager().deleteUser("test@test.com" , "123");
        }
        catch(Exception e){
        	fail();
        }    
        HibernateUtility.shutdown();
    }

    @BeforeEach
    public void setUp() {
        siteManager.deleteAll();
        //Automatically deletes all pages too due to cascade, but:
        pageManager.deleteAll();
    }

    @Test
    public void testPutLegalSite(){
        HttpResponse response= addSite("legalSite");
        String url = getEUrl(response);
        int id =  getSitePKByName(url);
        response = putSite(id, "NewName");
        assertEquals(HttpStatus.NO_CONTENT, response.getStatus());
    }

    @Test
    public void testAddLegalSite(){
        HttpResponse response= addSite("legalSite");
        assertEquals(HttpStatus.CREATED, response.getStatus());
    }

    @Test
    public void testUpdateNullNameSite(){
        HttpResponse response = addSite("testSite");
        String url =  getEUrl(response);
        int id = getSitePKByName(url);
        HttpClientResponseException thrown = assertThrows(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(HttpRequest.PUT("/sites", new Site(id, "")).header("X-API-Key",token));
        });
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, thrown.getStatus());
    }

    @Test
    public void testAddNullNameSite(){
        HttpClientResponseException thrown = assertThrows(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(HttpRequest.POST("/sites", new SiteAddCommand("")).header("X-API-Key",token));
        });
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, thrown.getStatus());
    }

    @Test
    public void testDeleteAndGetSite(){
        HttpResponse response = addSite("testSite");
        String url =  getEUrl(response);
        int id = getSitePKByName(url);
        // Asserting that we've added a site
        assertEquals(HttpStatus.CREATED, response.getStatus());

        HttpRequest request = HttpRequest.DELETE("/sites/"+url).header("X-API-Key",token);
        response = client.toBlocking().exchange(request);
        HttpClientResponseException thrown = assertThrows(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(HttpRequest.GET("/sites/"+url));
        });
        assertEquals(HttpStatus.NOT_FOUND, thrown.getStatus());
    }

    @Test
    public void testAddAndGetSite(){
        HttpResponse response = addSite("testSite");
        String id =  getEUrl(response);

        Site testSite = getSite(id);

        assertEquals("testSite", testSite.getName());
    }

    @Test
    public void testNonExistingSiteReturns404() {
        HttpClientResponseException thrown = assertThrows(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(HttpRequest.GET("/sites/IpsumLoremSite"));
        });

        assertNotNull(thrown.getResponse());
        assertEquals(HttpStatus.NOT_FOUND, thrown.getStatus());
    }

    @Test
    public void testAddAndUpdateSite(){
        HttpResponse response = addSite("testSite");
        String url =  getEUrl(response);

        int id = getSitePKByName(url);

        putSite(id, "newName");
        Site m = getSite("newName");
        assertEquals("newName", m.getName());
    }

/* //Delete this code once confirmed that it has moved into PageControllerTest in cleanup.
        Page testPage = getPage("testSiteA", "nutrition/slu!#g");
        assertEquals("newTitle", testPage.getTitle());
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
        URI pLoc = location(siteName);
        HttpRequest request = HttpRequest.PUT(pLoc+"/pages", new Page(id, siteName, slug, index, title, content));
        return client.toBlocking().exchange(request);
    }
*/

    protected HttpResponse putSite(int id, String newName) {
        HttpRequest request = HttpRequest.PUT("/sites", new Site(id, newName)).header("X-API-Key",token);
        return client.toBlocking().exchange(request);
    }

    protected HttpResponse addSite(String name) {
        HttpRequest request = HttpRequest.POST("/sites", new SiteAddCommand(name)).header("X-API-Key",token);
        HttpResponse response = client.toBlocking().exchange(request);
        return response;
    }

    protected Site getSite(String name) {
        URI loc = location(name);
        HttpRequest request = HttpRequest.GET(loc);
        return client.toBlocking().retrieve(request, Site.class);
    }


    private String getEUrl(HttpResponse response) {
        String val = response.header(HttpHeaders.LOCATION);
        if (val != null) {
            int index = val.indexOf("/sites/");
            if (index != -1) {
                return (val.substring(index + "/sites/".length()));
            }
            return null;
        }
        return null;
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

    protected int getSitePKByName(String name){
        return getSite(name).getPrimaryKey();
    }
}
