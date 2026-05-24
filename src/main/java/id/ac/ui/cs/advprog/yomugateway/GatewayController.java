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

    @Value("${BACAAN_KUIS_SERVICE_URL:http://localhost:8080}")
    private String bacaanKuisServiceUrl;

    @Value("${LIGA_SERVICE_URL:http://localhost:8084}")
    private String ligaServiceUrl;


    @RequestMapping(value = {
            "/api/achievements/**",
            "/api/admin/master/**",
            "/api/internal/**"
    })
    public ResponseEntity<byte[]> proxyRequest(HttpServletRequest request, @RequestBody(required = false) byte[] body) throws URISyntaxException {
        String path = request.getRequestURI();
        return forwardRequest(request, body, achievementServiceUrl, path, "Achievement");
    }

    @RequestMapping(value = {
            "/api/forum/**",
            "/api/diskusi/**",
            "/api/diskusi-forum/**"
    })
    public ResponseEntity<byte[]> proxyForumRequest(HttpServletRequest request, @RequestBody(required = false) byte[] body) throws URISyntaxException {
        String path = request.getRequestURI();

        if (path.startsWith("/api/forum")) {
            path = path.replaceFirst("/api/forum", "/api");
        } else if (path.startsWith("/api/diskusi")) {
            path = path.replaceFirst("/api/diskusi", "/api");
        } else if (path.startsWith("/api/diskusi-forum")) {
            path = path.replaceFirst("/api/diskusi-forum", "/api");
        }

        return forwardRequest(request, body, forumServiceUrl, path, "Forum");
    }

    @RequestMapping(value = {
            "/api/auth/**",
            "/api/user/**"
    })
    public ResponseEntity<byte[]> proxyAuthRequest(HttpServletRequest request, @RequestBody(required = false) byte[] body) throws URISyntaxException {
        String path = request.getRequestURI();
        if (path.startsWith("/api/")) {
            path = path.substring(4);
        }

        return forwardRequest(request, body, authServiceUrl, path, "Auth");
    }

    @RequestMapping(value = {
            "/api/bacaan-kuis/**"
    })
    public ResponseEntity<byte[]> proxyBacaanKuisRequest(HttpServletRequest request, @RequestBody(required = false) byte[] body) throws URISyntaxException {
        String path = request.getRequestURI().replaceFirst("/api/bacaan-kuis", "/api");
        return forwardRequest(request, body, bacaanKuisServiceUrl, path, "Bacaan-Kuis");
    }

    @RequestMapping(value = {
            "/api/liga/**"
    })
    public ResponseEntity<byte[]> proxyLigaRequest(HttpServletRequest request, @RequestBody(required = false) byte[] body) throws URISyntaxException {
        String path = request.getRequestURI().replaceFirst("/api/liga", "/liga");
        return forwardRequest(request, body, ligaServiceUrl, path, "Liga");
    }

    private ResponseEntity<byte[]> forwardRequest(
            HttpServletRequest request,
            byte[] body,
            String serviceUrl,
            String targetPath,
            String serviceName
    ) throws URISyntaxException {
        RestTemplate restTemplate = new RestTemplate(new org.springframework.http.client.JdkClientHttpRequestFactory());
        String query = request.getQueryString();
        String url = serviceUrl + targetPath + (query != null ? "?" + query : "");
        HttpEntity<byte[]> entity = new HttpEntity<>(body, copyHeaders(request));

        try {
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    new URI(url),
                    HttpMethod.valueOf(request.getMethod()),
                    entity,
                    byte[].class
            );
            return ResponseEntity.status(response.getStatusCode())
                    .headers(filterResponseHeaders(response.getHeaders()))
                    .body(response.getBody());
        } catch (org.springframework.web.client.HttpStatusCodeException exception) {
            return ResponseEntity.status(exception.getStatusCode())
                    .headers(filterResponseHeaders(exception.getResponseHeaders()))
                    .body(exception.getResponseBodyAsByteArray());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(("Error communicating with " + serviceName + " Service: " + e.getMessage()).getBytes());
        }
    }

    private HttpHeaders copyHeaders(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (!headerName.equalsIgnoreCase("host")
                    && !headerName.equalsIgnoreCase("transfer-encoding")
                    && !headerName.equalsIgnoreCase("content-length")
                    && !headerName.equalsIgnoreCase("accept-encoding")) {
                headers.add(headerName, request.getHeader(headerName));
            }
        }
        return headers;
    }

    private HttpHeaders filterResponseHeaders(HttpHeaders sourceHeaders) {
        HttpHeaders responseHeaders = new HttpHeaders();
        if (sourceHeaders == null) {
            return responseHeaders;
        }

        sourceHeaders.forEach((key, values) -> {
            String lowerKey = key.toLowerCase();
            // Strict whitelist of safe headers to forward from the downstream service
            if (lowerKey.equals("content-type")
                    || lowerKey.equals("content-disposition")
                    || lowerKey.equals("cache-control")) {
                responseHeaders.addAll(key, values);
            }
        });
        return responseHeaders;
    }
}
