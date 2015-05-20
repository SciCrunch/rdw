dataSource {
    pooled = true
}
hibernate {
    cache.use_second_level_cache = true
    cache.use_query_cache = false
    cache.region.factory_class = 'net.sf.ehcache.hibernate.EhCacheRegionFactory'
}
// environment specific settings
environments {
    development {
		
		dataSource {
			pooled = true
			dbCreate = "validate"
			url = "jdbc:postgresql://localhost:5432/rd_dev"
			dialect = org.hibernate.dialect.PostgreSQLDialect
			driverClassName = "org.postgresql.Driver"
			username = "rd_dev"
			password = ""
		}
    }
    test {
		
		dataSource {
			pooled = true
			dbCreate = "validate"
			url = "jdbc:postgresql://localhost:5432/rd_prod"
			dialect = org.hibernate.dialect.PostgreSQLDialect
			driverClassName = "org.postgresql.Driver"
			username = "rd_test"
			password = ""
		}
    }
    production {
		
		dataSource {
			pooled = true
			dbCreate = "validate"
			logSql=true
			url = "jdbc:postgresql://localhost:5432/rd_prod"
			dialect = org.hibernate.dialect.PostgreSQLDialect
			driverClassName = "org.postgresql.Driver"
			username = "rd_prod"
			password = ""
			properties {
				validationQuery = "select 1 as con_test"
				testOnBorrow = true
				maxActive = 10
				maxIdle = 5
				minIdle = 2
				initialSize = 5
				minEvictableIdleTimeMillis = 60000
				timeBetweenEvictionRunsMillis = 60000
			}
		}
    }
}
