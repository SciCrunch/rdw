grails.servlet.version = "2.5" // Change depending on target container compliance (2.5 or 3.0)
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.target.level = 1.6
grails.project.source.level = 1.6
//grails.project.war.file = "target/${appName}-${appVersion}.war"
// IBO
grails.project.war.file = "rdw.war"

// uncomment (and adjust settings) to fork the JVM to isolate classpaths
//grails.project.fork = [
//   run: [maxMemory:1024, minMemory:64, debug:false, maxPerm:256]
//]

grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // specify dependency exclusions here; for example, uncomment this to disable ehcache:
        // excludes 'ehcache'
    }
    log "error" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    checksums true // Whether to verify checksums on resolve
    legacyResolve false // whether to do a secondary resolve on plugin installation, not advised and here for backwards compatibility

    repositories {
        inherits true // Whether to inherit repository definitions from plugins

        grailsPlugins()
        grailsHome()
        grailsCentral()

        mavenLocal()
        mavenCentral() 

        // uncomment these (or add new ones) to enable remote dependency resolution from public Maven repositories
        //mavenRepo "http://snapshots.repository.codehaus.org"
        //mavenRepo "http://repository.codehaus.org"
        //mavenRepo "http://download.java.net/maven/2/"
        //mavenRepo "http://repository.jboss.com/maven2/"
    }

    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes e.g.

       //  runtime 'mysql:mysql-connector-java:5.1.22'
		runtime "postgresql:postgresql:9.1-901.jdbc4"
		
		test "org.spockframework:spock-grails-support:0.7-groovy-2.0"
		
		compile "org.apache.httpcomponents:httpmime:4.2.5"
		
		compile 'org.ccil.cowan.tagsoup:tagsoup:1.2'
		
		compile 'bnlpkit:bnlpkit:0.5.3'
    }

    plugins {
        runtime ":hibernate:3.6.10.14"
        runtime ":jquery:1.8.3"
       // runtime ":resources:1.1.6"
		runtime ":resources:1.2"
		

        // Uncomment these (or add new ones) to enable additional resources capabilities
        //runtime ":zipped-resources:1.0"
        //runtime ":cached-resources:1.0"
        //runtime ":yui-minify-resources:0.1.5"

      //   build ":tomcat:$grailsVersion"
		
		build ":tomcat:7.0.52.1"

        runtime ":database-migration:1.3.8"

        compile ':cache:1.0.1'
		compile ':cache-ehcache:1.0.0'
		compile ":quartz:1.0.1"
		
		compile ":db-reverse-engineer:0.5" // IBO
		test(":spock:0.7") { exclude "spock-grails-support" }
		compile ":jquery-ui:1.8.24"
		
		compile ":rest:0.7" // for HttpBuilder
    }
}
