package org.javasimon.jdbc4;

import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class Url represents Simon JDBC url. It parses given url and than provides getters for
 * driver's propreties if provided or default values.
 *
 * @author Radovan Sninsky
 * @since 2.4
 */
public final class DriverUrl {
    /**
     * Name for the property holding the real driver class value.
     */
    public static final String REAL_DRIVER = "simon_real_drv";
    /**
     * Name for the driver property holding the hierarchy prefix given to JDBC Simons.
     */
    public static final String PREFIX = "simon_prefix";

    private static final Pattern DRIVER_FROM_URL_PATTERN = Pattern.compile("(.*):(.*)");
    private final String simonUrl;
    private final String realUrl;
    private String driverId;
    private final Properties properties;

    /**
     * Class constructor, parses given URL and recognizes driver's properties.
     *
     * @param url given JDBC URL
     */
    public DriverUrl(String prefixUrl, String url, Properties properties) {
        this.simonUrl = url;
        this.properties = new Properties(properties);
        // Remove URL prefix
        if (url.startsWith(prefixUrl)) {
            url = url.substring(prefixUrl.length());
        }
        // Get Driver Id
        Matcher m = DRIVER_FROM_URL_PATTERN.matcher(url);
        if (m.matches()) {
            driverId = m.group(1);
            String end = m.group(2);
            StringBuilder realUrlBuilder = new StringBuilder("jdbc:").append(driverId).append(':');
            // Extract Simon properties
            StringTokenizer st = new StringTokenizer(end, ";");
            boolean first=true;
            while (st.hasMoreTokens()) {
                String tokenPairStr = st.nextToken();
                String[] tokenPair = tokenPairStr.trim().split("=", 2);
                boolean keep=true;
                if (tokenPair.length == 2) {
                    String token = tokenPair[0].trim();
                    String tokenValue = tokenPair[1].trim();

                    if (isSimonProperty(token)
                            && !properties.containsKey(token)) {
                        properties.setProperty(token, tokenValue);
                        keep=false;
                    }
                }
                // Not a Simon property
                if (keep) {
                    if (first) {
                        first=false;
                    } else {
                        realUrlBuilder.append(';');
                    }
                    realUrlBuilder.append(tokenPairStr);
                }
            }
            realUrl = realUrlBuilder.toString();
        } else {
            driverId = null;
            realUrl = null;
        }
    }

    private boolean isSimonProperty(String propertyName) {
        return propertyName.startsWith("simon_");
    }

    /**
     * Returns orignal JDBC URL without any Simon stuff.
     *
     * @return original JDBC URL
     */
    public String getRealUrl() {
        return realUrl;
    }

    /**
     * Returns driver identifier (eg. oracle, postgres, mysql, h2, etc.).
     *
     * @return driver identifier
     */
    public String getDriverId() {
        return driverId;
    }

    /**
     * Returns fully qualified class name of the real driver.
     *
     * @return driver class FQN
     */
    public String getRealDriver() {
        return properties.getProperty(REAL_DRIVER);
    }

    /**
     * Returns prefix for hierarchy of JDBC related Simons.
     *
     * @return prefix for JDBC Simons
     */
    public String getPrefix() {
        return properties.getProperty(PREFIX, Driver.DEFAULT_PREFIX);
    }

    /**
     * Get properties of the real driver
     *
     * @return Real properties
     */
    public Properties getRealProperties() {
        Properties realProperties = new Properties();
        // Remove Simon specific properties
        for (Map.Entry<Object, Object> property : properties.entrySet()) {
            if (!isSimonProperty((String) property.getKey())) {
                realProperties.put(property.getKey(), property.getValue());
            }
        }
        return realProperties;
    }

    public String getProperty(String propertyName) {
        return properties.getProperty(propertyName);
    }
}
