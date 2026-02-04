package egovframework.com.config;

import java.util.EnumSet;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import jakarta.servlet.ServletContextListener;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.multipart.support.MultipartFilter;

import egovframework.com.cmm.context.EgovWebServletContextListener;
import egovframework.com.cmm.filter.HTMLTagFilter;
import egovframework.com.cmm.filter.SessionTimeoutCookieFilter;
import egovframework.com.uat.uap.filter.EgovLoginPolicyFilter;
import egovframework.com.utl.wed.filter.CkFilter;

@Configuration
public class ServletConfig {

	@Bean
	public ServletListenerRegistrationBean<ServletContextListener> egovWebServletContextListener() {
		return new ServletListenerRegistrationBean<>(new EgovWebServletContextListener());
	}

	@Bean
	public ServletListenerRegistrationBean<RequestContextListener> requestContextListener() {
		return new ServletListenerRegistrationBean<>(new RequestContextListener());
	}

	@Bean
	public FilterRegistrationBean<CharacterEncodingFilter> characterEncodingFilter() {
		CharacterEncodingFilter filter = new CharacterEncodingFilter();
		filter.setEncoding("UTF-8");
		filter.setForceEncoding(true);

		FilterRegistrationBean<CharacterEncodingFilter> registration = new FilterRegistrationBean<>(filter);
		registration.addUrlPatterns("*.do");
		registration.setDispatcherTypes(EnumSet.of(DispatcherType.REQUEST));
		registration.setOrder(1);
		return registration;
	}

	@Bean
	public FilterRegistrationBean<MultipartFilter> multipartFilter() {
		MultipartFilter filter = new MultipartFilter();
		filter.setMultipartResolverBeanName("multipartResolver");

		FilterRegistrationBean<MultipartFilter> registration = new FilterRegistrationBean<>(filter);
		registration.addUrlPatterns("*.do");
		registration.setOrder(2);
		return registration;
	}

	@Bean
	public FilterRegistrationBean<HTMLTagFilter> htmlTagFilter() {
		return buildSimpleFilter(new HTMLTagFilter(), 3);
	}

	@Bean
	public FilterRegistrationBean<SessionTimeoutCookieFilter> sessionTimeoutCookieFilter() {
		return buildSimpleFilter(new SessionTimeoutCookieFilter(), 4);
	}

	@Bean
	public FilterRegistrationBean<CkFilter> ckFilter() {
		CkFilter filter = new CkFilter();
		FilterRegistrationBean<CkFilter> registration = new FilterRegistrationBean<>(filter);
		registration.addUrlPatterns("/ckUploadImage");
		registration.addInitParameter("properties", "egovframework/egovProps/ck.properties");
		registration.setOrder(5);
		return registration;
	}

	@Bean
	@ConditionalOnProperty(name = "Globals.Auth", havingValue = "session")
	public FilterRegistrationBean<EgovLoginPolicyFilter> loginPolicyFilter() {
		FilterRegistrationBean<EgovLoginPolicyFilter> registration =
				new FilterRegistrationBean<>(new EgovLoginPolicyFilter());
		registration.addUrlPatterns("/uat/uia/actionLogin.do");
		registration.setOrder(6);
		return registration;
	}

	private <T extends Filter> FilterRegistrationBean<T> buildSimpleFilter(T filter, int order) {
		FilterRegistrationBean<T> registration = new FilterRegistrationBean<>(filter);
		registration.addUrlPatterns("*.do");
		registration.setOrder(order);
		return registration;
	}
}
