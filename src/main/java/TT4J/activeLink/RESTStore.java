package TT4J.activeLink;


import TT4J.interfaces.Store;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by stokowiec on 2015-07-10.
 */
public class RESTStore implements Store{

    private static final String GET_PATH = "TT4J";
    private static final String POST_PATH = "TT4J/add";

    private HttpClient httpClient;
    private String hostName;
    private int port;

    public RESTStore(String hostName, int port) {
        this.hostName = hostName;
        this.port = port;
        this.httpClient = HttpClientBuilder.create().build();;
    }

    public HttpResponse getLink(String id) throws IOException {
        String request = String.format("%s:%s/%s/%s", hostName, port, GET_PATH, id);
        HttpGet getRequest = new HttpGet(request);
        return httpClient.execute(getRequest);
    }

    public HttpResponse postLink(String link) throws IOException {

        String request = String.format("%s:%s/%s", hostName, port, POST_PATH);

        HttpPost postRequest = new HttpPost(request);
        postRequest.addHeader(HTTP.CONTENT_TYPE, "text/plain;");
        postRequest.setEntity(new StringEntity(link));

        return httpClient.execute(postRequest);
    }

    public String getResourcePath(){
        return String.format("%s:%s/%s", hostName, port, GET_PATH);
    }

    public static List<String> handleResponse(HttpResponse response) throws IOException {
        // Check for HTTP response code: 200 = success
        if (response.getStatusLine().getStatusCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
        }

        // Get-Capture Complete application/xml body response
        BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
        String line;
        List<String> output = new ArrayList<>();

        // Simply iterate through XML response and show on console.
        while ((line = br.readLine()) != null) {
            output.add(line);
        }

        return output;
    }

    @Override
    public String storeLink(String activeLink) throws IOException {

        String uuid = handleResponse(postLink(activeLink)).get(0);
        String resourcePath = getResourcePath();

        return String.format("%s/%s", resourcePath, uuid);
    }
}


