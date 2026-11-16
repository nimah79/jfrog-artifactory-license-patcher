package com.nimah79.artifactory.agent.patches;

import com.nimah79.artifactory.agent.Licenses;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LicensePatcher extends ClassPatch {

    public LicensePatcher() {
        super(
                "org.jfrog.license.legacy.LegacyLicenseManager",
                "org.jfrog.license.legacy.SignedLicense",
                "org.jfrog.license.api.LicenseManager",
                "org.jfrog.license.multiplatform.JFrogLicenseManager",
                "org.jfrog.license.multiplatform.SignedLicense",
                "org.jfrog.license.legacy.b",
                "org.jfrog.license.multiplatform.SignedProduct",
                "org.jfrog.license.api.License",
                "org.jfrog.license.multiplatform.a",
                "org.jfrog.license.api.a",
                "org.jfrog.license.api.Product"
        );
    }

    @Override
    byte[] onTransform(ClassLoader loader, String className, CtClass clazz, byte[] classBuf) throws Throwable {
        try {
            if (className.equals("org.jfrog.license.legacy.LegacyLicenseManager")) {
                return patchLegacyLicenseManager(loader, clazz);
            } else if (className.equals("org.jfrog.license.legacy.SignedLicense")) {
                return patchSignedLicense(clazz);
            } else if (className.equals("org.jfrog.license.api.LicenseManager")) {
                return patchLicenseManager(loader, clazz);
            } else if (className.equals("org.jfrog.license.multiplatform.JFrogLicenseManager")) {
                return patchJFrogLicenseManager(loader, clazz);
            } else if (className.equals("org.jfrog.license.multiplatform.SignedLicense")) {
                return patchMultiplatformSignedLicense(clazz);
            } else if (className.equals("org.jfrog.license.legacy.b")) {
                return patchLegacyB(loader, clazz);
            } else if (className.equals("org.jfrog.license.multiplatform.SignedProduct")) {
                return patchSignedProduct(loader, clazz);
            } else if (className.equals("org.jfrog.license.api.License")) {
                return patchApiLicense(clazz);
            } else if (className.equals("org.jfrog.license.multiplatform.a")) {
                return patchMultiplatformA(loader, clazz);
            } else if (className.equals("org.jfrog.license.api.a")) {
                return patchApiA(loader, clazz);
            } else if (className.equals("org.jfrog.license.api.Product")) {
                return patchApiProduct(loader, clazz);
            }
        } catch (Exception e) {
            System.out.println("[License Patcher] Error patching class " + className + ": " + e.getMessage());
            e.printStackTrace();
        }
        return clazz.toBytecode();
    }

    private String makeProductBody(String productId) {
        return "{"
                + "    org.jfrog.license.api.Product p = new org.jfrog.license.api.Product();"
                + "    p.setId(\"" + productId + "\");"
                + "    p.setOwner(com.nimah79.artifactory.agent.Licenses.OWNER);"
                + "    java.util.Calendar cal = java.util.Calendar.getInstance();"
                + "    p.setValidFrom(cal.getTime());"
                + "    cal.add(java.util.Calendar.YEAR, com.nimah79.artifactory.agent.Licenses.VALID_YEARS);"
                + "    p.setExpires(cal.getTime());"
                + "    p.setType(org.jfrog.license.api.Product.Type.ENTERPRISE_PLUS);"
                + "    p.setTrial(false);"
                + "    return p;"
                + "}";
    }

    private String makeSignedProductBody(String productId) {
        return "{"
                + "    org.jfrog.license.api.Product p = new org.jfrog.license.api.Product();"
                + "    p.setId(\"" + productId + "\");"
                + "    p.setOwner(com.nimah79.artifactory.agent.Licenses.OWNER);"
                + "    java.util.Calendar cal = java.util.Calendar.getInstance();"
                + "    p.setValidFrom(cal.getTime());"
                + "    cal.add(java.util.Calendar.YEAR, com.nimah79.artifactory.agent.Licenses.VALID_YEARS);"
                + "    p.setExpires(cal.getTime());"
                + "    p.setType(org.jfrog.license.api.Product.Type.ENTERPRISE_PLUS);"
                + "    p.setTrial(false);"
                + "    org.jfrog.license.multiplatform.SignedProduct sp = new org.jfrog.license.multiplatform.SignedProduct(p, null, null);"
                + "    return sp;"
                + "}";
    }

    private String hardcodedApiLicenseBody() {
        return "{"
                + "    System.out.println(\"[License Patcher] Creating hardcoded api.License\");"
                + "    org.jfrog.license.api.License license = new org.jfrog.license.api.License();"
                + "    license.setVersion(2);"
                + "    license.setValidateOnline(java.lang.Boolean.FALSE);"
                + "    java.util.Map products = new java.util.LinkedHashMap();"
                + "    String[] ids = com.nimah79.artifactory.agent.Licenses.PRODUCT_IDS;"
                + "    for (int i = 0; i < ids.length; i++) {"
                + "        org.jfrog.license.api.Product p = new org.jfrog.license.api.Product();"
                + "        p.setId(ids[i]);"
                + "        p.setOwner(com.nimah79.artifactory.agent.Licenses.OWNER);"
                + "        java.util.Calendar cal = java.util.Calendar.getInstance();"
                + "        p.setValidFrom(cal.getTime());"
                + "        cal.add(java.util.Calendar.YEAR, com.nimah79.artifactory.agent.Licenses.VALID_YEARS);"
                + "        p.setExpires(cal.getTime());"
                + "        p.setType(org.jfrog.license.api.Product.Type.ENTERPRISE_PLUS);"
                + "        p.setTrial(false);"
                + "        products.put(ids[i], p);"
                + "    }"
                + "    license.setProducts(products);"
                + "    return license;"
                + "}";
    }

    private String hardcodedApiProductsBody() {
        return "{"
                + "    System.out.println(\"[License Patcher] Returning hardcoded products map\");"
                + "    java.util.Map products = new java.util.LinkedHashMap();"
                + "    String[] ids = com.nimah79.artifactory.agent.Licenses.PRODUCT_IDS;"
                + "    for (int i = 0; i < ids.length; i++) {"
                + "        org.jfrog.license.api.Product p = new org.jfrog.license.api.Product();"
                + "        p.setId(ids[i]);"
                + "        p.setOwner(com.nimah79.artifactory.agent.Licenses.OWNER);"
                + "        java.util.Calendar cal = java.util.Calendar.getInstance();"
                + "        p.setValidFrom(cal.getTime());"
                + "        cal.add(java.util.Calendar.YEAR, com.nimah79.artifactory.agent.Licenses.VALID_YEARS);"
                + "        p.setExpires(cal.getTime());"
                + "        p.setType(org.jfrog.license.api.Product.Type.ENTERPRISE_PLUS);"
                + "        p.setTrial(false);"
                + "        products.put(ids[i], p);"
                + "    }"
                + "    return products;"
                + "}";
    }

    private byte[] patchLicenseManager(ClassLoader loader, CtClass clazz) throws Throwable {
        String licenseBody = hardcodedApiLicenseBody();
        String productsBody = hardcodedApiProductsBody();

        String[] loadLicenseNames = {"loadLicense"};
        String[] loadNames = {"load"};
        String[] otherNames = {"loadTestKey", "loadJFrogLicense", "loadLegacyLicense"};
        String[] networkNames = {"checkInternalNetworkConnectivity", "assertInternalNetwork"};

        patchMethodsByName(clazz, loadLicenseNames, licenseBody, 1, true);
        patchMethodsByName(clazz, loadLicenseNames, licenseBody, 2, true);
        patchMethodsByName(clazz, loadNames, productsBody, 1, true);
        patchMethodsByName(clazz, loadNames, productsBody, 2, true);
        patchMethodsByName(clazz, otherNames, licenseBody, 2, true);
        patchMethodsByName(clazz, networkNames, "{}", 0, true);

        clazz.detach();
        return clazz.toBytecode();
    }

    private byte[] patchApiA(ClassLoader loader, CtClass clazz) throws Throwable {
        String licenseBody = hardcodedApiLicenseBody();
        String productsBody = hardcodedApiProductsBody();
        String identityBody = "{ return $1; }";

        // Patch by exact parameter signatures to avoid hitting static utility methods
        // a(String) -> loadLicense: 1 param java.lang.String
        // a(String, String...) -> loadLicense varargs: 2 params [String, String[]]
        // a(String, LicenseException) -> loadTestKey: 2 params [String, LicenseException]
        // a(String, String) -> loadJFrogLicense: 2 params [String, String]
        // b(String) -> load: 1 param java.lang.String
        // b(String, String...) -> load varargs: 2 params [String, String[]]
        // b(String, String) -> loadLegacyLicense: 2 params [String, String]
        // c() -> assertInternalNetwork: 0 params, throws
        // Skip: a() (assertSystemTestProperty), b() (checkInternalNetworkConnectivity),
        //       a(byte[]) (encodeBase64StringChunked), c(String) (licenseKeyHash),
        //       d(String) (formatLicenseKey), e(String) (parsePublicKey)

        for (CtBehavior m : clazz.getDeclaredBehaviors()) {
            String name = m.getMethodInfo().getName();
            CtClass[] params = m.getParameterTypes();
            
            if ("a".equals(name)) {
                if (params.length == 1 && "java.lang.String".equals(params[0].getName())) {
                    m.setBody(licenseBody); // loadLicense(String)
                } else if (params.length == 2 
                           && "java.lang.String".equals(params[0].getName())
                           && "java.lang.String[]".equals(params[1].getName())) {
                    m.setBody(licenseBody); // loadLicense(String, String...)
                } else if (params.length == 2
                           && "java.lang.String".equals(params[0].getName())
                           && "org.jfrog.license.exception.LicenseException".equals(params[1].getName())) {
                    m.setBody(licenseBody); // loadTestKey(String, LicenseException)
                } else if (params.length == 2
                           && "java.lang.String".equals(params[0].getName())
                           && "java.lang.String".equals(params[1].getName())) {
                    m.setBody(licenseBody); // loadJFrogLicense(String, String)
                }
            } else if ("b".equals(name)) {
                if (params.length == 1 && "java.lang.String".equals(params[0].getName())) {
                    m.setBody(productsBody); // load(String)
                } else if (params.length == 2 
                           && "java.lang.String".equals(params[0].getName())
                           && "java.lang.String[]".equals(params[1].getName())) {
                    m.setBody(productsBody); // load(String, String...)
                } else if (params.length == 2
                           && "java.lang.String".equals(params[0].getName())
                           && "java.lang.String".equals(params[1].getName())) {
                    m.setBody(licenseBody); // loadLegacyLicense(String, String)
                }
            } else if ("c".equals(name) && params.length == 0) {
                m.setBody("{}"); // assertInternalNetwork (throws)
            }
        }

        clazz.detach();
        return clazz.toBytecode();
    }

    private void patchMethodsByName(CtClass clazz, String[] names, String body, int paramCount, boolean exactParamCount) throws Throwable {
        for (String name : names) {
            List<CtBehavior> methods = new ArrayList<>();
            for (CtBehavior it : clazz.getDeclaredBehaviors()) {
                if (name.equals(it.getMethodInfo().getName())) {
                    if ((exactParamCount && it.getParameterTypes().length == paramCount) ||
                        (!exactParamCount && it.getParameterTypes().length == paramCount)) {
                        methods.add(it);
                    }
                }
            }
            for (CtBehavior m : methods) {
                m.setBody(body);
            }
        }
    }

    private byte[] patchLegacyLicenseManager(ClassLoader loader, CtClass clazz) throws Throwable {
        String legacyBody = "{"
                + "    org.jfrog.license.legacy.License lic = new org.jfrog.license.legacy.License();"
                + "    lic.setProductId(1);"
                + "    lic.setProductName(\"Artifactory\");"
                + "    lic.setSubject(com.nimah79.artifactory.agent.Licenses.OWNER);"
                + "    lic.setCompany(com.nimah79.artifactory.agent.Licenses.OWNER);"
                + "    java.util.Calendar cal = java.util.Calendar.getInstance();"
                + "    lic.setIssued(cal.getTime());"
                + "    lic.setValidFrom(cal.getTime());"
                + "    cal.add(java.util.Calendar.YEAR, com.nimah79.artifactory.agent.Licenses.VALID_YEARS);"
                + "    lic.setValidUntil(cal.getTime());"
                + "    lic.setQuantity(1000);"
                + "    lic.setType(org.jfrog.license.legacy.License.Type.HA);"
                + "    org.jfrog.license.legacy.SignedLicense sl = new org.jfrog.license.legacy.SignedLicense(lic, null, null);"
                + "    return sl;"
                + "}";

        String identityBody = "{ return $1; }";

        List<CtBehavior> loadMethods = new ArrayList<>();
        for (CtBehavior it : clazz.getDeclaredBehaviors()) {
            if ("load".equals(it.getMethodInfo().getName()) && it.getParameterTypes().length == 2) {
                loadMethods.add(it);
            }
        }
        for (CtBehavior m : loadMethods) {
            m.setBody(legacyBody);
        }

        List<CtBehavior> decryptMethods = new ArrayList<>();
        for (CtBehavior it : clazz.getDeclaredBehaviors()) {
            if ("decrypt".equals(it.getMethodInfo().getName()) && it.getParameterTypes().length == 1) {
                decryptMethods.add(it);
            }
        }
        for (CtBehavior m : decryptMethods) {
            m.setBody(identityBody);
        }

        List<CtBehavior> encryptMethods = new ArrayList<>();
        for (CtBehavior it : clazz.getDeclaredBehaviors()) {
            if ("encrypt".equals(it.getMethodInfo().getName()) && it.getParameterTypes().length == 1) {
                encryptMethods.add(it);
            }
        }
        for (CtBehavior m : encryptMethods) {
            m.setBody(identityBody);
        }

        clazz.detach();
        return clazz.toBytecode();
    }

    private byte[] patchLegacyB(ClassLoader loader, CtClass clazz) throws Throwable {
        String legacyBody = "{"
                + "    org.jfrog.license.legacy.License lic = new org.jfrog.license.legacy.License();"
                + "    lic.setProductId(1);"
                + "    lic.setProductName(\"Artifactory\");"
                + "    lic.setSubject(com.nimah79.artifactory.agent.Licenses.OWNER);"
                + "    lic.setCompany(com.nimah79.artifactory.agent.Licenses.OWNER);"
                + "    java.util.Calendar cal = java.util.Calendar.getInstance();"
                + "    lic.setIssued(cal.getTime());"
                + "    lic.setValidFrom(cal.getTime());"
                + "    cal.add(java.util.Calendar.YEAR, com.nimah79.artifactory.agent.Licenses.VALID_YEARS);"
                + "    lic.setValidUntil(cal.getTime());"
                + "    lic.setQuantity(1000);"
                + "    lic.setType(org.jfrog.license.legacy.License.Type.HA);"
                + "    org.jfrog.license.legacy.SignedLicense sl = new org.jfrog.license.legacy.SignedLicense(lic, null, null);"
                + "    return sl;"
                + "}";

        String identityBody = "{ return $1; }";

        List<CtBehavior> loadMethods = new ArrayList<>();
        for (CtBehavior it : clazz.getDeclaredBehaviors()) {
            if ("a".equals(it.getMethodInfo().getName()) && it.getParameterTypes().length == 2) {
                String p0 = it.getParameterTypes()[0].getName();
                if (p0.equals("java.lang.String")) {
                    loadMethods.add(it);
                }
            }
        }
        for (CtBehavior m : loadMethods) {
            m.setBody(legacyBody);
        }

        List<CtBehavior> decryptMethods = new ArrayList<>();
        for (CtBehavior it : clazz.getDeclaredBehaviors()) {
            if ("b".equals(it.getMethodInfo().getName()) && it.getParameterTypes().length == 1) {
                decryptMethods.add(it);
            }
        }
        for (CtBehavior m : decryptMethods) {
            m.setBody(identityBody);
        }

        List<CtBehavior> encryptMethods = new ArrayList<>();
        for (CtBehavior it : clazz.getDeclaredBehaviors()) {
            if ("a".equals(it.getMethodInfo().getName()) && it.getParameterTypes().length == 1) {
                String p0 = it.getParameterTypes()[0].getName();
                if (p0.equals("[B")) {
                    encryptMethods.add(it);
                }
            }
        }
        for (CtBehavior m : encryptMethods) {
            m.setBody(identityBody);
        }

        clazz.detach();
        return clazz.toBytecode();
    }

    private byte[] patchSignedLicense(CtClass clazz) throws Throwable {
        List<CtBehavior> signMethods = new ArrayList<>();
        for (CtBehavior it : clazz.getDeclaredBehaviors()) {
            if ("sign".equals(it.getMethodInfo().getName())) {
                signMethods.add(it);
            }
        }
        for (CtBehavior m : signMethods) {
            m.setBody("{}");
        }

        List<CtBehavior> verifyMethods = new ArrayList<>();
        for (CtBehavior it : clazz.getDeclaredBehaviors()) {
            if ("verify".equals(it.getMethodInfo().getName())) {
                verifyMethods.add(it);
            }
        }
        for (CtBehavior m : verifyMethods) {
            m.setBody("{}");
        }

        clazz.detach();
        return clazz.toBytecode();
    }

    private byte[] patchMultiplatformSignedLicense(CtClass clazz) throws Throwable {
        List<CtBehavior> signMethods = new ArrayList<>();
        for (CtBehavior it : clazz.getDeclaredBehaviors()) {
            if ("sign".equals(it.getMethodInfo().getName())) {
                signMethods.add(it);
            }
        }
        for (CtBehavior m : signMethods) {
            m.setBody("{}");
        }

        List<CtBehavior> verifyMethods = new ArrayList<>();
        for (CtBehavior it : clazz.getDeclaredBehaviors()) {
            if ("verify".equals(it.getMethodInfo().getName())) {
                verifyMethods.add(it);
            }
        }
        for (CtBehavior m : verifyMethods) {
            m.setBody("{}");
        }

        clazz.detach();
        return clazz.toBytecode();
    }

    private byte[] patchSignedProduct(ClassLoader loader, CtClass clazz) throws Throwable {
        List<CtBehavior> verifyMethods = new ArrayList<>();
        for (CtBehavior it : clazz.getDeclaredBehaviors()) {
            if ("verify".equals(it.getMethodInfo().getName())) {
                verifyMethods.add(it);
            }
        }
        for (CtBehavior m : verifyMethods) {
            m.setBody("{}");
        }

        List<CtBehavior> signMethods = new ArrayList<>();
        for (CtBehavior it : clazz.getDeclaredBehaviors()) {
            if ("sign".equals(it.getMethodInfo().getName())) {
                signMethods.add(it);
            }
        }
        for (CtBehavior m : signMethods) {
            m.setBody("{}");
        }

        clazz.detach();
        return clazz.toBytecode();
    }

    private byte[] patchJFrogLicenseManager(ClassLoader loader, CtClass clazz) throws Throwable {
        String body = "{"
                + "    org.jfrog.license.multiplatform.License lic = new org.jfrog.license.multiplatform.License();"
                + "    lic.setVersion(2);"
                + "    lic.setValidateOnline(java.lang.Boolean.FALSE);"
                + "    java.util.Map products = new java.util.LinkedHashMap();"
                + "    String[] ids = com.nimah79.artifactory.agent.Licenses.PRODUCT_IDS;"
                + "    for (int i = 0; i < ids.length; i++) {"
                + "        org.jfrog.license.api.Product p = new org.jfrog.license.api.Product();"
                + "        p.setId(ids[i]);"
                + "        p.setOwner(com.nimah79.artifactory.agent.Licenses.OWNER);"
                + "        java.util.Calendar cal = java.util.Calendar.getInstance();"
                + "        p.setValidFrom(cal.getTime());"
                + "        cal.add(java.util.Calendar.YEAR, com.nimah79.artifactory.agent.Licenses.VALID_YEARS);"
                + "        p.setExpires(cal.getTime());"
                + "        p.setType(org.jfrog.license.api.Product.Type.ENTERPRISE_PLUS);"
                + "        p.setTrial(false);"
                + "        org.jfrog.license.multiplatform.SignedProduct sp = new org.jfrog.license.multiplatform.SignedProduct(p, null, null);"
                + "        products.put(ids[i], sp);"
                + "    }"
                + "    lic.setProducts(products);"
                + "    return lic;"
                + "}";

        for (CtBehavior m : clazz.getDeclaredBehaviors()) {
            if ("load".equals(m.getMethodInfo().getName())) {
                CtClass[] params = m.getParameterTypes();
                // Target: load(String, PublicKey)
                if (params.length == 2
                    && "java.lang.String".equals(params[0].getName())
                    && "java.security.PublicKey".equals(params[1].getName())) {
                    m.setBody(body);
                }
            }
        }

        clazz.detach();
        return clazz.toBytecode();
    }

    private byte[] patchMultiplatformA(ClassLoader loader, CtClass clazz) throws Throwable {
        String body = "{"
                + "    org.jfrog.license.multiplatform.License lic = new org.jfrog.license.multiplatform.License();"
                + "    lic.setVersion(2);"
                + "    lic.setValidateOnline(java.lang.Boolean.FALSE);"
                + "    java.util.Map products = new java.util.LinkedHashMap();"
                + "    String[] ids = com.nimah79.artifactory.agent.Licenses.PRODUCT_IDS;"
                + "    for (int i = 0; i < ids.length; i++) {"
                + "        org.jfrog.license.api.Product p = new org.jfrog.license.api.Product();"
                + "        p.setId(ids[i]);"
                + "        p.setOwner(com.nimah79.artifactory.agent.Licenses.OWNER);"
                + "        java.util.Calendar cal = java.util.Calendar.getInstance();"
                + "        p.setValidFrom(cal.getTime());"
                + "        cal.add(java.util.Calendar.YEAR, com.nimah79.artifactory.agent.Licenses.VALID_YEARS);"
                + "        p.setExpires(cal.getTime());"
                + "        p.setType(org.jfrog.license.api.Product.Type.ENTERPRISE_PLUS);"
                + "        p.setTrial(false);"
                + "        org.jfrog.license.multiplatform.SignedProduct sp = new org.jfrog.license.multiplatform.SignedProduct(p, null, null);"
                + "        products.put(ids[i], sp);"
                + "    }"
                + "    lic.setProducts(products);"
                + "    return lic;"
                + "}";

        for (CtBehavior m : clazz.getDeclaredBehaviors()) {
            if ("a".equals(m.getMethodInfo().getName())) {
                CtClass[] params = m.getParameterTypes();
                if (params.length == 2
                    && "java.lang.String".equals(params[0].getName())
                    && "java.security.PublicKey".equals(params[1].getName())) {
                    m.setBody(body);
                }
            }
        }

        clazz.detach();
        return clazz.toBytecode();
    }

    private byte[] patchApiLicense(CtClass clazz) throws Throwable {
        clazz.detach();
        return clazz.toBytecode();
    }

    private byte[] patchApiProduct(ClassLoader loader, CtClass clazz) throws Throwable {
        String ctorBody = "{"
                + "    this.id = \"artifactory\";"
                + "    this.owner = com.nimah79.artifactory.agent.Licenses.OWNER;"
                + "    java.util.Calendar cal = java.util.Calendar.getInstance();"
                + "    this.validFrom = cal.getTime();"
                + "    cal.add(java.util.Calendar.YEAR, com.nimah79.artifactory.agent.Licenses.VALID_YEARS);"
                + "    this.expires = cal.getTime();"
                + "    this.type = org.jfrog.license.api.Product.Type.ENTERPRISE_PLUS;"
                + "    this.trial = false;"
                + "    this.signature = $1 == null ? null : $1.getSignature();"
                + "}";

        List<CtConstructor> ctors = new ArrayList<>();
        for (CtConstructor ctor : clazz.getDeclaredConstructors()) {
            if (ctor.getParameterTypes().length == 1) {
                String p0 = ctor.getParameterTypes()[0].getName();
                if (p0.equals("org.jfrog.license.legacy.SignedLicense")) {
                    ctors.add(ctor);
                }
            }
        }
        for (CtConstructor ctor : ctors) {
            ctor.setBody(ctorBody);
        }

        clazz.detach();
        return clazz.toBytecode();
    }
}