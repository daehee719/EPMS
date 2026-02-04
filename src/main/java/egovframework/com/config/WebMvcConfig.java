package egovframework.com.config;

import java.util.List;

import org.egovframe.rte.ptl.mvc.tags.ui.pagination.DefaultPaginationManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.view.BeanNameViewResolver;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import org.springframework.web.servlet.view.JstlView;

import egovframework.com.cmm.ImagePaginationRenderer;
import egovframework.com.cmm.resolver.EgovSecurityArgumentResolver;
import egovframework.com.cmm.web.EgovBindingInitializer;
import egovframework.com.cmm.web.EgovMultipartResolver;
import egovframework.com.sym.log.wlg.web.EgovWebLogInterceptor;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

	@Bean
	public ImagePaginationRenderer imageRenderer() {
		return new ImagePaginationRenderer();
	}

	@Bean
	public DefaultPaginationManager paginationManager(ImagePaginationRenderer imageRenderer) {
		DefaultPaginationManager manager = new DefaultPaginationManager();
		java.util.Map<String, org.egovframe.rte.ptl.mvc.tags.ui.pagination.PaginationRenderer> renderers =
				new java.util.HashMap<>();
		renderers.put("image", imageRenderer);
		manager.setRendererType(renderers);
		return manager;
	}

	@Bean
	public RequestMappingHandlerAdapter requestMappingHandlerAdapter() {
		RequestMappingHandlerAdapter adapter = new RequestMappingHandlerAdapter();
		adapter.setWebBindingInitializer(new EgovBindingInitializer());
		adapter.setMessageConverters(List.of(new MappingJackson2HttpMessageConverter()));
		adapter.setCustomArgumentResolvers(List.of(new EgovSecurityArgumentResolver()));
		return adapter;
	}

	@Bean
	public LocaleChangeInterceptor localeChangeInterceptor() {
		LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
		interceptor.setParamName("lang");
		return interceptor;
	}

	@Bean
	public LocaleResolver localeResolver() {
		return new SessionLocaleResolver();
	}

	@Bean
	public EgovWebLogInterceptor egovWebLogInterceptor() {
		return new EgovWebLogInterceptor();
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(localeChangeInterceptor());
		registry.addInterceptor(egovWebLogInterceptor());
	}

	@Bean
	public SimpleMappingExceptionResolver simpleMappingExceptionResolver() {
		SimpleMappingExceptionResolver resolver = new SimpleMappingExceptionResolver();
		resolver.setDefaultErrorView("egovframework/com/cmm/error/egovError");
		resolver.setExceptionMappings(new java.util.Properties() {{
			setProperty("org.springframework.dao.DataAccessException", "egovframework/com/cmm/error/dataAccessFailure");
			setProperty("org.springframework.transaction.TransactionException", "egovframework/com/cmm/error/dataAccessFailure");
			setProperty("org.egovframe.rte.fdl.cmmn.exception.EgovBizException", "egovframework/com/cmm/error/egovBizException");
			setProperty("org.springframework.web.HttpSessionRequiredException", "egovframework/com/uat/uia/EgovLoginUsr");
			setProperty("egovframework.com.cmm.exception.EgovXssException", "egovframework/com/cmm/error/egovXssException");
			setProperty("egovframework.com.cmm.exception.EgovFileExtensionException", "egovframework/com/cmm/error/egovFileExtensionException");
		}});
		return resolver;
	}

	@Bean
	public BeanNameViewResolver beanNameViewResolver() {
		BeanNameViewResolver resolver = new BeanNameViewResolver();
		resolver.setOrder(0);
		return resolver;
	}

	@Bean
	public UrlBasedViewResolver urlBasedViewResolver() {
		UrlBasedViewResolver resolver = new UrlBasedViewResolver();
		resolver.setOrder(1);
		resolver.setViewClass(JstlView.class);
		resolver.setPrefix("/WEB-INF/jsp/");
		resolver.setSuffix(".jsp");
		return resolver;
	}

	@Bean
	public MappingJackson2JsonView jsonView() {
		MappingJackson2JsonView view = new MappingJackson2JsonView();
		view.setContentType("application/json;charset=UTF-8");
		return view;
	}

	@Bean(name = "multipartResolver")
	public EgovMultipartResolver multipartResolver() {
		return new EgovMultipartResolver();
	}

	@Override
	public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
		configurer.enable();
	}
}
