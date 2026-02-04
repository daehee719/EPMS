package egovframework.com.config;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

@Configuration
public class MyBatisConfig {

	@Bean(name = "egov.sqlSessionFactory")
	public SqlSessionFactory sqlSessionFactory(DataSource dataSource, Environment environment) throws Exception {
		String dbType = environment.getProperty("Globals.DbType", "maria");

		SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
		factoryBean.setDataSource(dataSource);
		factoryBean.setConfigLocation(new PathMatchingResourcePatternResolver()
				.getResource("classpath:/egovframework/mapper/config/mapper-config.xml"));
		factoryBean.setMapperLocations(new PathMatchingResourcePatternResolver()
				.getResources("classpath:/egovframework/mapper/com/**/*_" + dbType + ".xml"));

		return factoryBean.getObject();
	}

	@Bean(name = "egov.sqlSessionTemplate")
	public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
		return new SqlSessionTemplate(sqlSessionFactory);
	}
}
