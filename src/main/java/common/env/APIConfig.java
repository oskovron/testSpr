package common.env;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.Sources;
import org.aeonbits.owner.Config.LoadPolicy;
import org.aeonbits.owner.Config.LoadType;

@Config.LoadPolicy(Config.LoadType.MERGE)
@Sources({"classpath:${env}/config.properties", "classpath:prod/config.properties"})
public interface APIConfig extends Config {

    @Key("base.url")
    String baseUrl();

    @Key("default.supervisor.login")
    @DefaultValue("supervisor")
    String defaultSupervisorLogin();

    @Key("default.admin.login")
    @DefaultValue("admin")
    String defaultAdminLogin();
    
    @Key("endpoint.player.create")
    @DefaultValue("/player/create/{editor}")
    String endpointPlayerCreate();

    @Key("endpoint.player.get")
    @DefaultValue("/player/get")
    String endpointPlayerGet();

    @Key("endpoint.player.get.all")
    @DefaultValue("/player/get/all")
    String endpointPlayerGetAll();

    @Key("endpoint.player.update")
    @DefaultValue("/player/update/{editor}/{id}")
    String endpointPlayerUpdate();

    @Key("endpoint.player.delete")
    @DefaultValue("/player/delete/{editor}")
    String endpointPlayerDelete();
}


