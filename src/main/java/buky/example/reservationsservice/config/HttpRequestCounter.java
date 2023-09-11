package buky.example.reservationsservice.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class HttpRequestCounter {

    private final Counter totalCnt;
    private final Counter successfulHttpRequestsCounter;
    private final Counter unsuccessfulHttpRequestsCounter;

    public HttpRequestCounter(MeterRegistry meterRegistry) {
        totalCnt = Counter.builder("http.requests.total")
                .description("Total number of HTTP requests")
                .register(meterRegistry);

        successfulHttpRequestsCounter = Counter.builder("http.requests.successful")
                .description("Total number of successful HTTP requests")
                .register(meterRegistry);

        unsuccessfulHttpRequestsCounter = Counter.builder("http.requests.failed")
                .description("Total number of failed HTTP requests")
                .register(meterRegistry);

    }

    public void increment() {

        System.out.println("incrementing Total: " +  totalCnt.count());
        totalCnt.increment();
    }

    public void incrementSuccessful() {

        System.out.println("incrementing Successfull: " +  successfulHttpRequestsCounter.count());
        successfulHttpRequestsCounter.increment();
    }

    public void incrementFailed() {

        System.out.println("incrementing Failed: " +  unsuccessfulHttpRequestsCounter.count());
        unsuccessfulHttpRequestsCounter.increment();
    }

    public double getTotalRequests() {
        return totalCnt.count();
    }


}
