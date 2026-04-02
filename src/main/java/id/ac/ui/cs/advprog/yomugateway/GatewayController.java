package id.ac.ui.cs.advprog.yomugateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api")
public class GatewayController {

    // Mengambil URL dari Environment Variable, defaultnya localhost jika tidak ada
    @Value("${ACHIEVEMENTS_SERVICE_URL:http://localhost:8083/api/achievements}")
    private String achievementServiceUrl;

    @GetMapping("/achievements")
    public ResponseEntity<String> getAchievements() {
        RestTemplate restTemplate = new RestTemplate();
        
        try {
            // Sekarang menggunakan variabel achievementServiceUrl yang dinamis
            return restTemplate.getForEntity(achievementServiceUrl, String.class);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error communicating with Achievement Service: " + e.getMessage());
        }
    }
}