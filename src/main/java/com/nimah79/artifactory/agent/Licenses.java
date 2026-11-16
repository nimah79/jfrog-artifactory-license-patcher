package com.nimah79.artifactory.agent;

public final class Licenses {
    // Owner shown on the licenses page
    public static final String OWNER = "nimah79";
    // Product entries that are placed in the hardcoded (multiplatform) license.
    // "artifactory" must always be present (every license check in artifactory and
    // access looks it up); the rest are needed by the different products of the
    // platform (xray: /artifactory/api/xray/license, distribution, ...).
    public static final String[] PRODUCT_IDS = {
            "artifactory",
            "xray",
            "distribution",
            "jfconnect",
            "pipelines",
            "mission-control",
            "metadata",
            "evidence"
    };
    // License duration in years
    public static final int VALID_YEARS = 30;

    private Licenses() {
    }
}
