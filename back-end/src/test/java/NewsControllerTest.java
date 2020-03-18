package test.java;

import io.micronaut.test.annotation.MicronautTest;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import main.java.com.projectBackEnd.*;
import main.java.com.projectBackEnd.Entities.Medicine.*;

import javax.inject.Inject;

import main.java.com.projectBackEnd.Entities.News.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@MicronautTest
public class NewsControllerTest {


    @Inject
    @Client("/")
    HttpClient client;

    static NewsManagerInterface newsManager;

    @BeforeAll
    public static void setUpDatabase() {
        HibernateUtility.setResource("testhibernate.cfg.xml");
        newsManager = NewsManager.getNewsManager();
    }

    @AfterAll
    public static void closeDatabase() {
        HibernateUtility.shutdown();
    }

    @BeforeEach
    public void setUp() {
        newsManager.deleteAll();
    }

    @Test
    public void testAddAndGetNews(){
        HttpResponse response = addNews(new Date(34189213L) , true, "Health Alert", "Corona virus pandemics",
                true, "COVID-19 originated from Wuhan, China", "slug");
        int id =  getEId(response).intValue();

        News testNews = getNews(id);

        assertEquals("Corona virus pandemics", testNews.getTitle());
    }


    protected HttpResponse putNews(Integer primaryKey, Date date, boolean pinned, String description, String title,
                                   boolean urgent, String content, String slug) {
        HttpRequest request = HttpRequest.PUT("/news", new NewsUpdateCommand(primaryKey, date, pinned, description,
                title, urgent, content,slug));
        return client.toBlocking().exchange(request);
    }

    protected HttpResponse addNews(Date date, boolean pinned, String description, String title, boolean urgent,
                                   String content, String slug){
        HttpRequest request = HttpRequest.POST("/news", new NewsAddCommand(date, pinned, description, title,
                urgent, content, slug));
        HttpResponse response = client.toBlocking().exchange(request);
        return response;
    }

    protected News getNews(Integer id){
        HttpRequest request = HttpRequest.GET("/news/" + id);
        return client.toBlocking().retrieve(request, News.class);

    }

    protected Long getEId(HttpResponse response) {
        String val = response.header(HttpHeaders.LOCATION);
        if (val != null) {
            int index = val.indexOf("/news/");
            if (index != -1) return Long.valueOf(val.substring(index + "/news/".length()));
            else return null;
        }
        else return null;
    }


}