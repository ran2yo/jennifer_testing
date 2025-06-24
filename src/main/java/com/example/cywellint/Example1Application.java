package com.example.cywellint;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
public class Example1Application extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(Example1Application.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(Example1Application.class, args);
	}

}

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
class TestController {
	private final TestService testService;

	@GetMapping("/simulate")
	public ResponseEntity<String> simulateTraffic(@RequestParam(name = "user", defaultValue = "user") String type) {
		for (int i = 0; i < 100000; i++) { 
			testService.processRequest(type);
		}
		return ResponseEntity.ok("Processed" + type);
	}

}

@Service
@Slf4j
class TestService {
	private final TestRepository testRepository;
	private final Random random = new Random();

	public TestService(TestRepository testRepository) {
		this.testRepository = testRepository;
	}

	public void processRequest(String type) {
		long start = System.currentTimeMillis();
		try {
			
			/*for (int j = 0; j < 5; j++) { // 내부에서도 5단계 정도 추가
*/				callSubMethod(random.nextInt(5));
			/* } */
			/*
			 * switch (type) { case "user" -> processUser(); case "order" -> processOrder();
			 * case "payment" -> processPayment();
			 * 
			 * default -> throw new IllegalArgumentException("Invalid type"); }
			 */

		} catch (Exception e) {
			// TODO: handle exception
			log.error("Error occurred while processing{}: {}", type, e.toString());
		} finally {
			long duration = System.currentTimeMillis() - start;
			log.info("Processed {} in {} ms", type, duration);
		}
	}

    private void callSubMethod(int step) {
            log.info("Executing step {}", step);

    }

//	private void processUser() {
//		log.debug("Creating user");
//		simulateDelay();
//		testRepository.save("User");
//	}
//
//	private void processOrder() {
//		log.debug("Creating order");
//		simulateDelay();
//		testRepository.save("Order");
//	}
//
//	private void processPayment() {
//		log.debug("Creating payment");
//		simulateDelay();
//		testRepository.save("Payment");                                                
//	}
//
//	private void simulateDelay() {
//	
//			Thread.currentThread().interrupt();
//		
//	}

}

@Repository
@Slf4j
class TestRepository {
	public void save(String entityName) {
		log.debug("Saving {} to database", entityName);
		simlateDbWork();
	}

	private void simlateDbWork() {
		try {
			Thread.sleep(ThreadLocalRandom.current().nextInt(5, 50));
		} catch (InterruptedException e) {
			// TODO: handle finally clause
			Thread.currentThread().interrupt();

		}
	}
}
