package com.example.vidal;

import com.example.vidal.model.GenerateResponse;
import com.example.vidal.model.SolutionEntity;
import com.example.vidal.repo.SolutionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

@Component
public class WebhookRunner {
    private final RestTemplate restTemplate;
    private final SolutionRepository repo;
    private final Logger log = LoggerFactory.getLogger(WebhookRunner.class);

    @Value("${app.name:John Doe}")
    private String name;
    @Value("${app.regno:REG12347}")
    private String regNo;
    @Value("${app.email:john@example.com}")
    private String email;

    public WebhookRunner(RestTemplate restTemplate, SolutionRepository repo) {
        this.restTemplate = restTemplate;
        this.repo = repo;
    }

    public void runOnStartup() {
        try {
            String url = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String body = String.format("{\"name\": \"%s\",\"regNo\": \"%s\",\"email\": \"%s\"}", name, regNo, email);
            HttpEntity<String> req = new HttpEntity<>(body, headers);

            log.info("Posting generateWebhook request to {}", url);
            ResponseEntity<GenerateResponse> resp = restTemplate.exchange(url, HttpMethod.POST, req, GenerateResponse.class);
            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                GenerateResponse gr = resp.getBody();
                log.info("Received webhook: {}", gr.getWebhook());
                log.info("Received accessToken: [REDACTED]");


                String finalSql = chooseSql(regNo);

            
                SolutionEntity s = new SolutionEntity();
                s.setRegNo(regNo);
                s.setSqlQuery(finalSql);
                s.setSubmittedAt(Instant.now());
                repo.save(s);

    
                sendSolution(gr.getWebhook(), gr.getAccessToken(), finalSql);
            } else {
                log.error("generateWebhook responded with status {}", resp.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error during startup webhook flow", e);
        }
    }

    private String chooseSql(String regNo) {
                String digits = regNo.replaceAll(".*?(\\d{2})$", "$1");
                int lastTwo;
                try {
                        lastTwo = Integer.parseInt(digits);
                } catch (Exception e) {
                        lastTwo = 0;
                }
                
                String finalSql =
                        "WITH high_paid AS (\n" +
                        "  SELECT DISTINCT emp_id\n" +
                        "  FROM payments\n" +
                        "  WHERE amount > 70000\n" +
                        "),\n" +
                        "emps AS (\n" +
                        "  SELECT e.emp_id,\n" +
                        "         e.first_name,\n" +
                        "         e.last_name,\n" +
                        "         e.department,\n" +
                        "         date_part('year', age(current_date, e.dob))::int AS age\n" +
                        "  FROM employee e\n" +
                        "  JOIN high_paid h ON e.emp_id = h.emp_id\n" +
                        "),\n" +
                        "ranked AS (\n" +
                        "  SELECT emps.*,\n" +
                        "         row_number() OVER (PARTITION BY emps.department ORDER BY emps.emp_id) AS rn,\n" +
                        "         d.department_name\n" +
                        "  FROM emps\n" +
                        "  JOIN department d ON emps.department = d.department_id\n" +
                        ")\n" +
                        "SELECT\n" +
                        "  r.department_name AS department_name,\n" +
                        "  ROUND(AVG(r.age)::numeric, 2) AS average_age,\n" +
                        "  STRING_AGG(r.first_name || ' ' || r.last_name, ', ' ORDER BY r.rn) FILTER (WHERE r.rn <= 10) AS employee_list\n" +
                        "FROM ranked r\n" +
                        "GROUP BY r.department, r.department_name\n" +
                        "ORDER BY r.department DESC;";
                return finalSql;
    }

    private void sendSolution(String webhookUrl, String accessToken, String finalSql) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (accessToken != null) {
                headers.setBearerAuth(accessToken);
            }
            String payload = String.format("{\"regNo\": \"%s\", \"solution\": \"%s\"}", regNo, finalSql.replace("\"", "\\\""));
            HttpEntity<String> req = new HttpEntity<>(payload, headers);
            log.info("Posting solution to webhook {}");
            ResponseEntity<String> resp = restTemplate.exchange(webhookUrl, HttpMethod.POST, req, String.class);
            log.info("Webhook response: {}", resp.getStatusCode());
        } catch (Exception e) {
            log.error("Failed to send solution to webhook", e);
        }
    }
}
