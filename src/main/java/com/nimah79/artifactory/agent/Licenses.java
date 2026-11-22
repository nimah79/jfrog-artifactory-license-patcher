package com.nimah79.artifactory.agent;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * License values used by the patched license classes.
 * <p>
 * Every value can be overridden via environment variables (preferred) or JVM
 * system properties; when neither is set, the hardcoded defaults below apply.
 * <p>
 * Environment variables (system property names in parentheses):
 * <ul>
 *   <li>{@code LICENSE_OWNER} ({@code license.owner}) — owner/company shown on the licenses page</li>
 *   <li>{@code LICENSE_VALID_YEARS} ({@code license.validYears}) — license duration in years</li>
 *   <li>{@code LICENSE_PRODUCT_IDS} ({@code license.productIds}) — comma-separated product IDs</li>
 *   <li>{@code LICENSE_VALID_FROM} ({@code license.validFrom}) — start date, ISO {@code yyyy-MM-dd} or {@code yyyy-MM-dd'T'HH:mm:ss}; default: now</li>
 *   <li>{@code LICENSE_EXPIRES} ({@code license.expires}) — expiry date, same formats; default: validFrom + validYears</li>
 *   <li>{@code LICENSE_PRODUCT_TYPE} ({@code license.productType}) — multiplatform product license type; default: ENTERPRISE_PLUS</li>
 *   <li>{@code LICENSE_LEGACY_TYPE} ({@code license.legacyType}) — legacy license type; default: HA</li>
 *   <li>{@code LICENSE_QUANTITY} ({@code license.quantity}) — legacy license quantity; default: 1000</li>
 * </ul>
 */
public final class Licenses {
    private static final String[] DATE_FORMATS = {"yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd"};

    // Owner shown on the licenses page
    public static final String OWNER = getString("LICENSE_OWNER", "license.owner", "nimah79");
    // License duration in years
    public static final int VALID_YEARS = getInt("LICENSE_VALID_YEARS", "license.validYears", 30);
    // Product entries that are placed in the hardcoded (multiplatform) license.
    // "artifactory" must always be present (every license check in artifactory and
    // access looks it up); the rest are needed by the different products of the
    // platform (xray: /artifactory/api/xray/license, distribution, ...).
    public static final String[] PRODUCT_IDS = getList(
            "LICENSE_PRODUCT_IDS",
            "license.productIds",
            new String[]{
                    "artifactory",
                    "xray",
                    "distribution",
                    "jfconnect",
                    "pipelines",
                    "mission-control",
                    "metadata",
                    "evidence"
            });
    // License type of the multiplatform products (org.jfrog.license.api.Product.Type)
    public static final String PRODUCT_TYPE = getEnumName("LICENSE_PRODUCT_TYPE", "license.productType", "ENTERPRISE_PLUS");
    // License type of the legacy license (org.jfrog.license.legacy.License.Type)
    public static final String LEGACY_TYPE = getEnumName("LICENSE_LEGACY_TYPE", "license.legacyType", "HA");
    // Legacy license quantity
    public static final int QUANTITY = getInt("LICENSE_QUANTITY", "license.quantity", 1000);
    // Configured start date, or null to always use "now" at license creation time
    public static final Date VALID_FROM = getDate("LICENSE_VALID_FROM", "license.validFrom");
    // Configured expiry date, or null to use validFrom + VALID_YEARS
    public static final Date EXPIRES = getDate("LICENSE_EXPIRES", "license.expires");

    /**
     * Start date of the licenses: the configured {@link #VALID_FROM}, or "now".
     */
    public static Date validFrom() {
        return VALID_FROM != null ? (Date) VALID_FROM.clone() : new Date();
    }

    /**
     * Expiry date of the licenses: the configured {@link #EXPIRES},
     * or validFrom + {@link #VALID_YEARS} years.
     */
    public static Date expires() {
        if (EXPIRES != null) {
            return (Date) EXPIRES.clone();
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(validFrom());
        cal.add(Calendar.YEAR, VALID_YEARS);
        return cal.getTime();
    }

    private static String getString(String envKey, String sysProp, String defaultValue) {
        String value = rawValue(envKey, sysProp);
        if (value == null) {
            return defaultValue;
        }
        log("owner=" + value + " (from " + source(envKey, sysProp) + ")");
        return value;
    }

    private static int getInt(String envKey, String sysProp, int defaultValue) {
        String value = rawValue(envKey, sysProp);
        if (value == null) {
            return defaultValue;
        }
        try {
            int parsed = Integer.parseInt(value);
            log(envKey + "=" + value + " (from " + source(envKey, sysProp) + ")");
            return parsed;
        } catch (NumberFormatException e) {
            log("WARNING: invalid number '" + value + "' for " + envKey + ", using default " + defaultValue);
            return defaultValue;
        }
    }

    private static String[] getList(String envKey, String sysProp, String[] defaults) {
        String value = rawValue(envKey, sysProp);
        if (value == null) {
            return defaults;
        }
        List<String> ids = new ArrayList<>();
        for (String part : value.split(",")) {
            String id = part.trim();
            if (!id.isEmpty()) {
                ids.add(id);
            }
        }
        if (ids.isEmpty()) {
            log("WARNING: empty " + envKey + ", using default products " + Arrays.toString(defaults));
            return defaults;
        }
        log(envKey + "=" + ids + " (from " + source(envKey, sysProp) + ")");
        return ids.toArray(new String[0]);
    }

    private static String getEnumName(String envKey, String sysProp, String defaultValue) {
        String value = rawValue(envKey, sysProp);
        if (value == null) {
            return defaultValue;
        }
        if (value.matches("[A-Za-z][A-Za-z0-9_]*")) {
            log(envKey + "=" + value + " (from " + source(envKey, sysProp) + ")");
            return value;
        }
        log("WARNING: invalid " + envKey + " '" + value + "', using default " + defaultValue);
        return defaultValue;
    }

    private static Date getDate(String envKey, String sysProp) {
        String value = rawValue(envKey, sysProp);
        if (value == null) {
            return null;
        }
        for (String format : DATE_FORMATS) {
            try {
                SimpleDateFormat parser = new SimpleDateFormat(format);
                parser.setLenient(false);
                Date parsed = parser.parse(value);
                log(envKey + "=" + value + " (from " + source(envKey, sysProp) + ")");
                return parsed;
            } catch (ParseException ignored) {
                // try next format
            }
        }
        log("WARNING: invalid date '" + value + "' for " + envKey
                + " (expected " + String.join(" or ", DATE_FORMATS) + "), ignoring");
        return null;
    }

    private static String rawValue(String envKey, String sysProp) {
        String value = System.getenv(envKey);
        if (value == null || value.trim().isEmpty()) {
            value = System.getProperty(sysProp);
        }
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private static String source(String envKey, String sysProp) {
        return System.getenv(envKey) != null ? "env " + envKey : "system property " + sysProp;
    }

    /**
     * Logs the effective license configuration (after env/system property resolution).
     */
    public static void logEffectiveConfig() {
        StringBuilder products = new StringBuilder();
        for (String id : PRODUCT_IDS) {
            if (products.length() > 0) {
                products.append(",");
            }
            products.append(id);
        }
        log("Effective license config: owner=" + OWNER
                + ", validYears=" + VALID_YEARS
                + ", products=[" + products + "]"
                + ", productType=" + PRODUCT_TYPE
                + ", legacyType=" + LEGACY_TYPE
                + ", quantity=" + QUANTITY
                + ", validFrom=" + (VALID_FROM != null ? VALID_FROM : "<now>")
                + ", expires=" + (EXPIRES != null ? EXPIRES : "<validFrom+" + VALID_YEARS + "y>"));
    }

    private static void log(String message) {
        System.out.println("[License Patcher] " + message);
    }

    private Licenses() {
    }
}
