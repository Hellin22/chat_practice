package practice.chatstomp.stomp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")      // 모든 Url 패턴에서 적용
                .allowedOrigins("http://localhost:5173")               // 모든 도메인에서의 접근 허용
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 허용할 HTTP메소드 (+OPTIONS는 프리플라이트 요청에 사용된다.)
                .allowedHeaders("*");               // 모든 HTTP 헤더 허용
    }
}
