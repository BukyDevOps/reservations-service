package buky.example.reservationsservice.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor, HandlerMethodArgumentResolver {


    private final RestTemplate restTemplate;

    @Value(value= "${user.BaseURL}")
    private String userURL;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod handlerMethod) {
            HasRole requiresRole = handlerMethod.getMethodAnnotation(HasRole.class);

            if (requiresRole != null) {
                String authorizationHeader = request.getHeader("Authorization");
                if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    return false;
                }

                String jwtToken = authorizationHeader.substring(7);

                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(jwtToken);
                headers.add("X-User-Role", requiresRole.value());

                HttpEntity<?> entity = new HttpEntity<>(headers);

                try {
                    String AUTH_SERVICE_URL = userURL + "/api/auth/authenticate";
                    ResponseEntity<Long> responseEntity = restTemplate.exchange(AUTH_SERVICE_URL, HttpMethod.GET, entity,
                            Long.class);
                    if (responseEntity.getStatusCode() == HttpStatus.OK) {
                        request.setAttribute("userId", responseEntity.getBody());
                        return true;
                    }
                } catch (RestClientException e) {
                    // User authentication failed or user doesn't have the required role
                }

                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(Long.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);

        assert request != null;
        return request.getAttribute("userId");
    }
}

