package buky.example.reservationsservice;

import buky.example.reservationsservice.util.RestUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RequiredArgsConstructor
public class ReservationsServiceApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(ReservationsServiceApplication.class, args);
	}

	@Override
	public void run(String... args) {
		System.out.println("ReservationsServiceApplication UP");
	}
}
