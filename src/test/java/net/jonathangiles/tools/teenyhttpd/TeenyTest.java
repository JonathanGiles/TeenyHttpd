package net.jonathangiles.tools.teenyhttpd;

import net.jonathangiles.tools.teenyhttpd.model.Method;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TeenyTest {

    private static final int TEST_PORT = 8080;

    TeenyApplicationTest.Response executeRequest(Method method, String url) {
        return executeRequest(method, url, null, null);
    }

    TeenyApplicationTest.Response executeRequest(String url, Map<String, String> headers) {
        return executeRequest(Method.GET, url, headers, null);
    }

    TeenyApplicationTest.Response executeRequest(Method method, String url, Map<String, String> headers, String requestBody) {
        CloseableHttpClient httpClient = HttpClients.createDefault();

        String basePath = "http://localhost:" + TEST_PORT;

        url = basePath + url;

        HttpUriRequest request;

        try {
            switch (method) {
                case GET:
                    request = new HttpGet(url);
                    break;
                case POST:
                    request = new HttpPost(url);
                    break;
                case PUT:
                    request = new HttpPut(url);
                    break;
                case DELETE:
                    request = new HttpDelete(url);
                    break;
                case HEAD:
                    request = new HttpHead(url);
                    break;
                case TRACE:
                    request = new HttpTrace(url);
                    break;
                case OPTIONS:
                    request = new HttpOptions(url);
                    break;
                case PATCH:
                    return new TeenyApplicationTest.Response(httpClient.execute(new HttpPatch(url)));
                default:
                    throw new RuntimeException("Unsupported method: " + method);
            }

            if (headers != null && !headers.isEmpty()) {
                headers.forEach(request::addHeader);
            }

            if (requestBody != null && request instanceof HttpEntityEnclosingRequestBase) {
                ((HttpEntityEnclosingRequestBase) request).setEntity(new StringEntity(requestBody));
            }

            return new TeenyApplicationTest.Response(httpClient.execute(request));

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                System.out.println("Ugh");
            }
        }
    }

    static class Response {
        private final String body;
        private final int statusCode;
        private final Map<String, String> headers = new HashMap<>();

        public int getStatusCode() {
            return statusCode;
        }

        public String getBody() {
            return body;
        }

        public String getFirstHeader(String key) {
            return headers.get(key);
        }

        public Response(HttpResponse response) {
            this.statusCode = response.getStatusLine().getStatusCode();

            for (org.apache.http.Header header : response.getAllHeaders()) {
                headers.put(header.getName(), header.getValue());
            }

            if (statusCode == 204) {
                this.body = "";
                return;
            }

            try {
                this.body = EntityUtils.toString(response.getEntity());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }


        }
    }

}
