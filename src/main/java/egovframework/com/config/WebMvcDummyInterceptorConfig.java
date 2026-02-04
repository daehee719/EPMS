package egovframework.com.config;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import egovframework.com.cmm.interceptor.AuthenticInterceptor;

@Configuration
@ConditionalOnProperty(name = "Globals.Auth", havingValue = "dummy")
public class WebMvcDummyInterceptorConfig implements WebMvcConfigurer {

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		AuthenticInterceptor interceptor = new AuthenticInterceptor();
		interceptor.setAdminAuthPatternList(List.of("/sec/ram/*.do"));

		registry.addInterceptor(interceptor)
				.addPathPatterns("/**/*.do")
				.excludePathPatterns(
						"/uat/uia/**",
						"/index.do",
						"/cmm/fms/getImage.do",
						"/uss/ion/bnr/getBannerImage.do",
						"/EgovLeft.do",
						"/EgovContent.do",
						"/EgovTop.do",
						"/EgovBottom.do",
						"/EgovModal.do");
	}
}
