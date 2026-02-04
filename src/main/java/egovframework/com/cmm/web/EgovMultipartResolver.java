package egovframework.com.cmm.web;

import org.springframework.web.multipart.support.StandardServletMultipartResolver;

/**
 * Spring 6 대응 MultipartResolver.
 * 기존 CommonsMultipartResolver는 Spring 6에서 제거되어 StandardServletMultipartResolver로 대체한다.
 * 파일 확장자 화이트리스트 검증은 이후 필터/서비스 단계로 분리한다.
 */
public class EgovMultipartResolver extends StandardServletMultipartResolver {

	public EgovMultipartResolver() {
		super();
	}
}
