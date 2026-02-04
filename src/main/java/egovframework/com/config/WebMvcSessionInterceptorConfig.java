package egovframework.com.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import egovframework.com.cmm.interceptor.IpObtainInterceptor;

@Configuration
@ConditionalOnProperty(name = "Globals.Auth", havingValue = "session")
public class WebMvcSessionInterceptorConfig implements WebMvcConfigurer {

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new IpObtainInterceptor());
	}
}
