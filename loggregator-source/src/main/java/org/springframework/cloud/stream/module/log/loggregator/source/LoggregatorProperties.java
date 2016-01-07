package org.springframework.cloud.stream.module.log.loggregator.source;


import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author <a href="josh@joshlong.com">Josh Long</a>
 */
@ConfigurationProperties
public class LoggregatorProperties {


    private String cloudFoundryApi, cloudFoundryUser, cloudFoundryPassword;

    private String applicationName;

    public String getCloudFoundryApi() {
        return cloudFoundryApi;
    }

    public void setCloudFoundryApi(String cloudFoundryApi) {
        this.cloudFoundryApi = cloudFoundryApi;
    }

    public String getCloudFoundryUser() {
        return cloudFoundryUser;
    }

    public void setCloudFoundryUser(String cloudFoundryUser) {
        this.cloudFoundryUser = cloudFoundryUser;
    }

    public String getCloudFoundryPassword() {
        return cloudFoundryPassword;
    }

    public void setCloudFoundryPassword(String cloudFoundryPassword) {
        this.cloudFoundryPassword = cloudFoundryPassword;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }
}
