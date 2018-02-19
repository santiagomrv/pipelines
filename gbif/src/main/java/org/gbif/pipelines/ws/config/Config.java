package org.gbif.pipelines.ws.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

/**
 * Models the ws configuration.
 */
public class Config {

  // property names
  private static final String WS_BASE_PATH_PROP = "ws.basePath";
  private static final String WS_TIMEOUT_PROP = "ws.timeoutSeconds";

  // defaults
  private static final String DEFAULT_TIMEOUT = "60";
  private static final String DEFAULT_CACHE_SIZE = "100";

  private final String basePath;
  private final long timeout;
  private final CacheConfig cacheConfig;

  public Config(String propertiesFilePath) {
    Properties props = new Properties();
    try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(propertiesFilePath)) {
      props.load(in);
    } catch (IOException e) {
      throw new IllegalArgumentException("Could not load properties file " + propertiesFilePath, e);
    }

    basePath = Optional.ofNullable(props.getProperty(WS_BASE_PATH_PROP))
      .filter(path -> !path.isEmpty())
      .orElseThrow(() -> new IllegalArgumentException("WS base path is required"));
    timeout = Long.parseLong(props.getProperty(WS_TIMEOUT_PROP, DEFAULT_TIMEOUT));

    // cache config
    cacheConfig = new CacheConfig(props);
  }

  public String getBasePath() {
    return basePath;
  }

  public long getTimeout() {
    return timeout;
  }

  public CacheConfig getCacheConfig() {
    return cacheConfig;
  }

  /**
   * Models the cache configuration.
   */
  public class CacheConfig {

    private static final String CACHE_NAME_PROP = "cache.name";
    private static final String CACHE_SIZE_PROP = "cache.sizeInMb";

    private final String name;
    // size in bytes
    private final long size;

    private CacheConfig(Properties props) {
      name = Optional.ofNullable(props.getProperty(CacheConfig.CACHE_NAME_PROP))
        .filter(prop -> !prop.isEmpty())
        .orElseThrow(() -> new IllegalArgumentException("WS Cache name is required"));
      size = Long.parseLong(props.getProperty(CACHE_SIZE_PROP, DEFAULT_CACHE_SIZE)) * 1024 * 1024;
    }

    public String getName() {
      return name;
    }

    public long getSize() {
      return size;
    }

  }

}
