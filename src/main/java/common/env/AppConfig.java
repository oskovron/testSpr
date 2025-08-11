package common.env;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.Sources;
import org.aeonbits.owner.Config.LoadPolicy;
import org.aeonbits.owner.Config.LoadType;

@Sources({"classpath:${env}/config.properties", "classpath:prod/config.properties"})
public interface AppConfig extends Config {

    @Key("test.thread.count")
    @DefaultValue("1")
    Integer threadCount();

    @Key("test.parallel.mode")
    @DefaultValue("methods")
    String parallelMode();

    @Key("allure.results.directory")
    @DefaultValue("target/allure-results")
    String allureResultsDir();

    @Key("allure.report.directory")
    @DefaultValue("target/allure-report")
    String allureReportDir();

}


