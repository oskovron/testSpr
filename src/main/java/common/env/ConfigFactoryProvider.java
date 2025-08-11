package common.env;

import org.aeonbits.owner.ConfigFactory;

public final class ConfigFactoryProvider {
    private static volatile AppConfig cached;
    private static volatile APIConfig apiConfig;

    private ConfigFactoryProvider() {}

    public static AppConfig appConfig() {
        if (cached == null) {
            synchronized (ConfigFactoryProvider.class) {
                if (cached == null) {
                    cached = ConfigFactory.create(AppConfig.class, System.getProperties(), System.getenv());
                }
            }
        }
        return cached;
    }

    public static APIConfig apiConfig() {
        if (apiConfig == null) {
            synchronized (ConfigFactoryProvider.class) {
                if (apiConfig == null) {
                    apiConfig = ConfigFactory.create(APIConfig.class, System.getProperties(), System.getenv());
                }
            }
        }
        return apiConfig;
    }
}


