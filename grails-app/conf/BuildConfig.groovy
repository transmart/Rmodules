/*************************************************************************   
* Copyright 2008-2012 Janssen Research & Development, LLC.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
******************************************************************/

grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"

grails.project.dependency.resolver = "maven"
grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log "warn"

    repositories {
        grailsCentral()

        mavenCentral()
        mavenRepo([
                name: 'repo.transmartfoundation.org-public',
                url: 'https://repo.transmartfoundation.org/content/repositories/public/',
        ])
    }
    dependencies {
        compile 'net.sf.opencsv:opencsv:2.3'
        compile "org.rosuda:Rserve:1.7.3"
    }

    plugins {
        compile ":hibernate:3.6.10.1"
        compile ":quartz:1.0-RC2"
        runtime ":resources:1.2"

        build(":tomcat:7.0.41",
              ":release:3.0.0",
			  ":rest-client-builder:1.0.3"
              ) {
            exported: false
        }
    }
}
