package id.ac.ui.cs.advprog.yomugateway;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api")
public class GatewayController {

    // Simple proxy for GET /api/achievements
    @GetMapping("/achievements")
    public ResponseEntity<String> getAchievements() {
        // Forward the request to the yomu-achievements service
        String achievementServiceUrl = "http://localhost:8083/api/achievements";
        RestTemplate restTemplate = new RestTemplate();
        
        try {
            return restTemplate.getForEntity(achievementServiceUrl, String.class);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error communicating with Achievement Service: " + e.getMessage());
        }
    }
}
