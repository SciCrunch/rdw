Resource Disambiguator Web (RDW)
================================

Prerequisites
-------------
 * Java 1.6+
 * Grails 2.3.8 
 * Postgres 8+
 * resource_disambiguator (https://github.com/SciCrunch/resource_disambiguator.git)


Getting the code
----------------

    cd $HOME
    git clone https://github.com/SciCrunch/rdw.git
    cd $HOME/rdw

Building
--------

First edit the `$HOME/rdw/grails-app/conf/DataSource.groovy` file to setup your database connection at least for the production environment. You need to set the development and test environments also for development. They can all point to the same database.

```groovy
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
...
    }
  }
```
    
    cd $HOME/rdw
    grails clean
    grails war


You need to create at least one user called admin with role 'admin' in the `rd_user` database table. 
All curator users will have the role 'curator' in the `rd_user` table. i
After that, you need to copy `$HOME/rdw/rdw.war` to the `webapps` directory of your servlet container (e.g. Tomcat) and start it. i
To login, point your browser to `http://localhost:8080/rdw/`.

