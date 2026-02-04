package egovframework.com.config;

import javax.sql.DataSource;

import org.egovframe.rte.fdl.crypto.EgovEnvCryptoService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
public class DataSourceConfig {

	@Bean(name = "dataSource")
	public DataSource dataSource(Environment environment,
			@Qualifier("egovEnvCryptoService") EgovEnvCryptoService cryptoService) {
		String driverClassName = environment.getProperty("Globals.maria.DriverClassName");
		String url = environment.getProperty("Globals.maria.Url");
		String username = environment.getProperty("Globals.maria.UserName");

		HikariConfig config = new HikariConfig();
		config.setDriverClassName(driverClassName);
		config.setJdbcUrl(url);
		config.setUsername(username);
		config.setPassword(cryptoService.getPassword());

		return new HikariDataSource(config);
	}
}
