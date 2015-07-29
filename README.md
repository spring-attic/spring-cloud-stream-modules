# spring-cloud-stream-modules
A source repository for OOTB Stream Modules

To deploy the build artifacts to the maven repo http://repo.spring.io/spring-cloud-stream-modules you need to authenticate. This is commonly done in `settings.xml` in ~/.m2/settings.xml or the location of settings.xml can be passed as a command line argument.

     $mvn deploy

or

    $mvn deploy -s some/settings.xml

Settings.xml contains:

    <settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                          http://maven.apache.org/xsd/settings-1.0.0.xsd">
      <servers>
        <server>
            <id>spring-cloud-stream-modules</id>
            <username>artifactory-username</username>
            <password>encrypted-artifactory-password</password>
          </server>
          <server>
            <id>spring-cloud-stream-modules-snapshot</id>
              <username>artifactory-username</username>
              <password>encrypted-artifactory-password</password>
          </server>
       </servers>
      </settings>
