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

    @Value("${achievements.service.url}")
    private String achievementServiceUrl;

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
            return restTemplate.exchange(new URI(url), HttpMethod.valueOf(request.getMethod()), entity, byte[].class);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(("Error communicating with Achievement Service: " + e.getMessage()).getBytes());
        }
    }
}