package xie;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * Hello world!
 *
 */

@RestController
@EnableAutoConfiguration
public class SpringBootClient1 {

	@RequestMapping("/")
	@ResponseBody
	public String home() {
		return "Hello world!";
	}

	public static void main(String[] args) {
		SpringApplication.run(SpringBootClient1.class, args);
	}
}
