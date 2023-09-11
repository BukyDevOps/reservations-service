package buky.example.reservationsservice.config;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.io.IOException;


@Component
@Slf4j
@RequiredArgsConstructor
@WebFilter("/*")
public class RequestLoggingFilter  implements Filter {

    private final HttpRequestCounter counter;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        counter.increment();
        chain.doFilter(request, response);


        int statusCode = ((HttpServletResponse) response).getStatus();
        log.info("Request received: " + ((HttpServletRequest) request).getMethod() + " " + ((HttpServletRequest) request).getRequestURI() + " Status CODE: "+ statusCode);

        if (statusCode >= 200 && statusCode < 400) {
            counter.incrementSuccessful();
        } else if (statusCode >= 400 && statusCode < 600) {
            counter.incrementFailed();
        }



    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization code if needed
    }

    @Override
    public void destroy() {
        // Cleanup code if needed
    }


}
