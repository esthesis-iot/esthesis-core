package esthesis.dataflows.pingupdater.config;

import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;

@StaticInitSafe
@ConfigMapping(prefix = "esthesis.dfl")
public interface AppConfig {

}
