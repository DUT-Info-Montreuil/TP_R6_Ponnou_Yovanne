package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.jaas;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import java.util.HashMap;
import java.util.Map;

public class JaasConfig extends Configuration {

    private static final Map<String, AppConfigurationEntry[]> CONFIGS = new HashMap<>();

    static {
        CONFIGS.put("MasterAnnonceLogin", new AppConfigurationEntry[]{
                new AppConfigurationEntry(
                        DbLoginModule.class.getName(),
                        AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
                        Map.of()
                )
        });

        CONFIGS.put("MasterAnnonceToken", new AppConfigurationEntry[]{
                new AppConfigurationEntry(
                        TokenLoginModule.class.getName(),
                        AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
                        Map.of()
                )
        });
    }

    @Override
    public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
        return CONFIGS.get(name);
    }

    public static void install() {
        Configuration.setConfiguration(new JaasConfig());
    }
}
