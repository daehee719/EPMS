package egovframework.com.config;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
@ConditionalOnProperty(name = "Globals.Auth", havingValue = "security", matchIfMissing = false)
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests(auth -> auth
				.requestMatchers("/css/**", "/html/**", "/images/**", "/js/**", "/resource/**").permitAll()
				.requestMatchers("/adm/**").hasRole("ADMIN")
				.requestMatchers("/edu/**").hasAnyRole("USER", "ADMIN")
				.anyRequest().authenticated());

		http.formLogin(form -> form
				.loginPage("/uat/uia/egovLoginUsr.do")
				.loginProcessingUrl("/uat/uia/actionLogin.do")
				.failureUrl("/uat/uia/egovLoginUsr.do?login_error=1")
				.defaultSuccessUrl("/EgovContent.do", true)
				.permitAll());

		http.logout(logout -> logout
				.logoutUrl("/uat/uia/actionLogout.do")
				.logoutSuccessUrl("/EgovContent.do"));

		http.exceptionHandling(ex -> ex.accessDeniedPage("/sec/ram/accessDenied.do"));
		http.csrf(csrf -> csrf.disable());

		http.headers(headers -> headers
				.frameOptions(frame -> frame.sameOrigin())
				.contentTypeOptions(Customizer.withDefaults())
				.cacheControl(cache -> cache.disable()));

		http.sessionManagement(session -> session.maximumSessions(1).expiredUrl("/EgovContent.do"));

		return http.build();
	}

	@Bean
	public UserDetailsService userDetailsService(DataSource dataSource) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		return username -> {
			String password = jdbcTemplate.queryForObject(
					"SELECT ESNTL_ID AS PASSWORD " +
					"FROM COMVNUSERMASTER m WHERE CONCAT(USER_SE, USER_ID) = ?",
					String.class,
					username);

			List<SimpleGrantedAuthority> authorities = jdbcTemplate.query(
					"SELECT A.AUTHOR_CODE AUTHORITY " +
					"FROM COMTNEMPLYRSCRTYESTBS A, COMVNUSERMASTER B " +
					"WHERE A.SCRTY_DTRMN_TRGET_ID = B.ESNTL_ID AND B.USER_ID = ?",
					(rs, rowNum) -> new SimpleGrantedAuthority(rs.getString("AUTHORITY")),
					username);

			return User.withUsername(username)
					.password(password)
					.authorities(authorities)
					.build();
		};
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return NoOpPasswordEncoder.getInstance();
	}

	@Bean
	public HttpSessionEventPublisher httpSessionEventPublisher() {
		return new HttpSessionEventPublisher();
	}
}
