package id.ac.ui.cs.advprog.yomugateway;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;

@RestController
public class GatewayController {

    @Value("${ACHIEVEMENTS_SERVICE_URL:http://localhost:8083}")
    private String achievementServiceUrl;

    @Value("${FORUM_SERVICE_URL:http://localhost:8084}")
    private String forumServiceUrl;

    @Value("${AUTH_SERVICE_URL:http://localhost:8081}")
    private String authServiceUrl;


    @RequestMapping(value = {
            "/api/achievements/**",
            "/api/admin/master/**",
            "/api/internal/**"
    })
    public ResponseEntity<byte[]> proxyRequest(HttpServletRequest request, @RequestBody(required = false) byte[] body) throws URISyntaxException {
        RestTemplate restTemplate = new RestTemplate();
        
        String path = request.getRequestURI();
        String query = request.getQueryString();
        String url = achievementServiceUrl + path + (query != null ? "?" + query : "");

        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (!headerName.equalsIgnoreCase("host")) {
                headers.add(headerName, request.getHeader(headerName));
            }
        }

        HttpEntity<byte[]> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<byte[]> response = restTemplate.exchange(new URI(url), HttpMethod.valueOf(request.getMethod()), entity, byte[].class);
            HttpHeaders responseHeaders = new HttpHeaders();
            response.getHeaders().forEach((key, values) -> {
                String lowerKey = key.toLowerCase();
                if (!lowerKey.startsWith("access-control-") &&
                    !lowerKey.equals("transfer-encoding") &&
                    !lowerKey.equals("connection") &&
                    !lowerKey.equals("keep-alive") &&
                    !lowerKey.equals("server") &&
                    !lowerKey.equals("date") &&
                    !lowerKey.equals("vary")) {
                    responseHeaders.addAll(key, values);
                }
            });
            return new ResponseEntity<>(response.getBody(), responseHeaders, response.getStatusCode());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(("Error communicating with Achievement Service: " + e.getMessage()).getBytes());
        }
    }

    @RequestMapping(value = {
            "/api/forum/**",
            "/api/diskusi/**"
    })
    public ResponseEntity<byte[]> proxyForumRequest(HttpServletRequest request, @RequestBody(required = false) byte[] body) throws URISyntaxException {
        RestTemplate restTemplate = new RestTemplate();
        
        String path = request.getRequestURI();

        if (path.startsWith("/api/forum")) {
            path = path.replaceFirst("/api/forum", "/api");
        } else if (path.startsWith("/api/diskusi")) {
            path = path.replaceFirst("/api/diskusi", "/api");
        }
        
        String query = request.getQueryString();
        String url = forumServiceUrl + path + (query != null ? "?" + query : "");

        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (!headerName.equalsIgnoreCase("host")) {
                headers.add(headerName, request.getHeader(headerName));
            }
        }

        HttpEntity<byte[]> entity = new HttpEntity<>(body, headers);

        try {
            return restTemplate.exchange(new URI(url), HttpMethod.valueOf(request.getMethod()), entity, byte[].class);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(("Error communicating with Forum Service: " + e.getMessage()).getBytes());
        }
    }

    @RequestMapping(value = {
            "/api/auth/**",
            "/api/user/**"
    })
    public ResponseEntity<byte[]> proxyAuthRequest(HttpServletRequest request, @RequestBody(required = false) byte[] body) throws URISyntaxException {
        RestTemplate restTemplate = new RestTemplate();
        
        String path = request.getRequestURI();
        if (path.startsWith("/api/")) {
            path = path.substring(4);
        }
        String query = request.getQueryString();
        String url = authServiceUrl + path + (query != null ? "?" + query : "");

        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (!headerName.equalsIgnoreCase("host") && !headerName.equalsIgnoreCase("transfer-encoding")) {
                headers.add(headerName, request.getHeader(headerName));
            }
        }

        HttpEntity<byte[]> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<byte[]> response = restTemplate.exchange(new URI(url), HttpMethod.valueOf(request.getMethod()), entity, byte[].class);
            HttpHeaders resHeaders = new HttpHeaders();
            resHeaders.putAll(response.getHeaders());
            resHeaders.remove(HttpHeaders.TRANSFER_ENCODING);
            return ResponseEntity.status(response.getStatusCode()).headers(resHeaders).body(response.getBody());
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            HttpHeaders resHeaders = new HttpHeaders();
            if (e.getResponseHeaders() != null) {
                resHeaders.putAll(e.getResponseHeaders());
                resHeaders.remove(HttpHeaders.TRANSFER_ENCODING);
            }
            return ResponseEntity.status(e.getStatusCode()).headers(resHeaders).body(e.getResponseBodyAsByteArray());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(("Error communicating with Auth Service: " + e.getMessage()).getBytes());
        }
    }
}
