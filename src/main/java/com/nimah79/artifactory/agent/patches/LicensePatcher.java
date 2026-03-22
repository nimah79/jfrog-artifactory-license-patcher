package com.nimah79.artifactory.agent.patches;

import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LicensePatcher extends ClassPatch {

    public LicensePatcher() {
        super("org.jfrog.license.legacy.LegacyLicenseManager", 
              "org.jfrog.license.legacy.SignedLicense",
              "org.jfrog.license.api.LicenseManager",
              "org.jfrog.license.multiplatform.JFrogLicenseManager",
              "org.jfrog.license.multiplatform.SignedLicense",
              "org.jfrog.license.legacy.b");
    }

    @Override
    byte[] onTransform(String className, CtClass clazz, byte[] classBuf) throws Throwable {
        try {
            if (className.equals("org.jfrog.license.legacy.LegacyLicenseManager")) {
                return patchLegacyLicenseManager(clazz);
            } else if (className.equals("org.jfrog.license.legacy.SignedLicense")) {
                return patchSignedLicense(clazz);
            } else if (className.equals("org.jfrog.license.api.LicenseManager")) {
                return patchLicenseManager(clazz);
            } else if (className.equals("org.jfrog.license.multiplatform.JFrogLicenseManager")) {
                return patchJFrogLicenseManager(clazz);
            } else if (className.equals("org.jfrog.license.multiplatform.SignedLicense")) {
                return patchMultiplatformSignedLicense(clazz);
            } else if (className.equals("org.jfrog.license.legacy.b")) {
                return patchLegacyB(clazz);
            }
        } catch (Exception e) {
            System.out.println("[License Patcher] Error patching class " + className + ": " + e.getMessage());
            e.printStackTrace();
        }
        return clazz.toBytecode();
    }

    private byte[] patchLegacyLicenseManager(CtClass clazz) throws Throwable {
        // Patch constructor to be empty
        List<CtConstructor> constructors = Arrays.stream(clazz.getDeclaredConstructors()).collect(Collectors.toList());
        for (CtConstructor constructor : constructors) {
            constructor.setBody("{}");
        }

        // Patch encrypt method to return input unchanged
        List<CtBehavior> encryptMethods = Arrays.stream(clazz.getDeclaredBehaviors())
                .filter(it -> "encrypt".equals(it.getMethodInfo().getName()))
                .collect(Collectors.toList());
        for (CtBehavior encrypt : encryptMethods) {
            if (encrypt.getParameterTypes().length == 1) {
                encrypt.setBody("{ return $1; }");
            }
        }

        // Patch decrypt method to return input unchanged
        List<CtBehavior> decryptMethods = Arrays.stream(clazz.getDeclaredBehaviors())
                .filter(it -> "decrypt".equals(it.getMethodInfo().getName()))
                .collect(Collectors.toList());
        for (CtBehavior decrypt : decryptMethods) {
            if (decrypt.getParameterTypes().length == 1) {
                decrypt.setBody("{ return $1; }");
            }
        }

        // Patch load method to return a hardcoded SignedLicense
        List<CtBehavior> loadMethods = Arrays.stream(clazz.getDeclaredBehaviors())
                .filter(it -> "load".equals(it.getMethodInfo().getName()))
                .collect(Collectors.toList());
        for (CtBehavior load : loadMethods) {
            load.setBody(
                "{" +
                "    System.out.println(\"[License Patcher] load() called - returning hardcoded license\");" +
                "    org.jfrog.license.legacy.License license = new org.jfrog.license.legacy.License();" +
                "    license.setProductId(1);" +
                "    license.setProductName(\"Artifactory\");" +
                "    license.setSubject(\"nimah79\");" +
                "    java.util.Calendar cal = java.util.Calendar.getInstance();" +
                "    cal.add(java.util.Calendar.YEAR, 30);" +
                "    license.setValidUntil(cal.getTime());" +
                "    license.setType(org.jfrog.license.legacy.License.Type.COMMERCIAL);" +
                "    org.jfrog.license.legacy.SignedLicense signedLicense = new org.jfrog.license.legacy.SignedLicense();" +
                "    org.jfrog.license.legacy.JsonLicenseSerializer serializer = new org.jfrog.license.legacy.JsonLicenseSerializer();" +
                "    try { java.lang.reflect.Field f = org.jfrog.license.legacy.SignedLicense.class.getDeclaredField(\"license\"); f.setAccessible(true); f.set(signedLicense, serializer.serialize(license)); } catch (Exception e) { throw new RuntimeException(e); }" +
                "    return signedLicense;" +
                "}"
            );
        }

        clazz.detach();
        return clazz.toBytecode();
    }

    private byte[] patchSignedLicense(CtClass clazz) throws Throwable {
        // Patch constructor with parameters - keep serialization but don't call sign
        List<CtConstructor> constructors = Arrays.stream(clazz.getDeclaredConstructors())
                .collect(Collectors.toList());
        for (CtConstructor constructor : constructors) {
            if (constructor.getParameterTypes().length == 3) {
                constructor.setBody(
                    "{" +
                    "    org.jfrog.license.legacy.a licenseSerializer = new org.jfrog.license.legacy.a();" +
                    "    this.license = licenseSerializer.a($1);" +
                    "}"
                );
            }
        }

        // Patch sign method to be empty
        List<CtBehavior> signMethods = Arrays.stream(clazz.getDeclaredBehaviors())
                .filter(it -> "sign".equals(it.getMethodInfo().getName()))
                .collect(Collectors.toList());
        for (CtBehavior sign : signMethods) {
            sign.setBody("{}");
        }

        // Patch verify method to be empty
        List<CtBehavior> verifyMethods = Arrays.stream(clazz.getDeclaredBehaviors())
                .filter(it -> "verify".equals(it.getMethodInfo().getName()))
                .collect(Collectors.toList());
        for (CtBehavior verify : verifyMethods) {
            verify.setBody("{}");
        }

        clazz.detach();
        return clazz.toBytecode();
    }

    private byte[] patchLicenseManager(CtClass clazz) throws Throwable {
        // Hardcoded license creation
        String hardcodedLicense = 
            "{" +
            "    System.out.println(\"[License Patcher] Creating hardcoded license\");" +
            "    org.jfrog.license.api.License license = new org.jfrog.license.api.License();" +
            "    license.setVersion(1);" +
            "    license.setValidateOnline(java.lang.Boolean.FALSE);" +
            "    java.util.Map products = new java.util.LinkedHashMap();" +
            "    org.jfrog.license.api.Product product = new org.jfrog.license.api.Product();" +
            "    product.setId(\"1\");" +
            "    product.setOwner(\"nimah79\");" +
            "    product.setType(org.jfrog.license.api.Product.Type.ENTERPRISE_PLUS);" +
            "    products.put(\"artifactory\", product);" +
            "    license.setProducts(products);" +
            "    return license;" +
            "}";

        String hardcodedLoadReturn =
            "{" +
            "    System.out.println(\"[License Patcher] load() returning hardcoded products\");" +
            "    org.jfrog.license.api.License license = new org.jfrog.license.api.License();" +
            "    license.setVersion(1);" +
            "    license.setValidateOnline(java.lang.Boolean.FALSE);" +
            "    java.util.Map products = new java.util.LinkedHashMap();" +
            "    org.jfrog.license.api.Product product = new org.jfrog.license.api.Product();" +
            "    product.setId(\"1\");" +
            "    product.setOwner(\"nimah79\");" +
            "    product.setType(org.jfrog.license.api.Product.Type.ENTERPRISE_PLUS);" +
            "    products.put(\"artifactory\", product);" +
            "    license.setProducts(products);" +
            "    return license.getProducts();" +
            "}";

        // Patch loadLicense methods
        List<CtBehavior> loadLicenseMethods = Arrays.stream(clazz.getDeclaredBehaviors())
                .filter(it -> "loadLicense".equals(it.getMethodInfo().getName()))
                .collect(Collectors.toList());
        for (CtBehavior method : loadLicenseMethods) {
            method.setBody(hardcodedLicense);
        }

        // Patch loadTestKey
        List<CtBehavior> loadTestKeyMethods = Arrays.stream(clazz.getDeclaredBehaviors())
                .filter(it -> "loadTestKey".equals(it.getMethodInfo().getName()))
                .collect(Collectors.toList());
        for (CtBehavior method : loadTestKeyMethods) {
            method.setBody(hardcodedLicense);
        }

        // Patch loadLegacyLicense
        List<CtBehavior> loadLegacyLicenseMethods = Arrays.stream(clazz.getDeclaredBehaviors())
                .filter(it -> "loadLegacyLicense".equals(it.getMethodInfo().getName()))
                .collect(Collectors.toList());
        for (CtBehavior method : loadLegacyLicenseMethods) {
            method.setBody(hardcodedLicense);
        }

        // Patch loadJFrogLicense
        List<CtBehavior> loadJFrogLicenseMethods = Arrays.stream(clazz.getDeclaredBehaviors())
                .filter(it -> "loadJFrogLicense".equals(it.getMethodInfo().getName()))
                .collect(Collectors.toList());
        for (CtBehavior method : loadJFrogLicenseMethods) {
            method.setBody(hardcodedLicense);
        }

        // Patch load methods (return Map)
        List<CtBehavior> loadMethods = Arrays.stream(clazz.getDeclaredBehaviors())
                .filter(it -> "load".equals(it.getMethodInfo().getName()))
                .collect(Collectors.toList());
        for (CtBehavior method : loadMethods) {
            method.setBody(hardcodedLoadReturn);
        }

        // Patch checkInternalNetworkConnectivity - empty body
        List<CtBehavior> checkInternalNetworkMethods = Arrays.stream(clazz.getDeclaredBehaviors())
                .filter(it -> "checkInternalNetworkConnectivity".equals(it.getMethodInfo().getName()))
                .collect(Collectors.toList());
        for (CtBehavior method : checkInternalNetworkMethods) {
            method.setBody("{}");
        }

        // Patch assertInternalNetwork - empty body
        List<CtBehavior> assertInternalNetworkMethods = Arrays.stream(clazz.getDeclaredBehaviors())
                .filter(it -> "assertInternalNetwork".equals(it.getMethodInfo().getName()))
                .collect(Collectors.toList());
        for (CtBehavior method : assertInternalNetworkMethods) {
            method.setBody("{}");
        }

        clazz.detach();
        return clazz.toBytecode();
    }

    private byte[] patchJFrogLicenseManager(CtClass clazz) throws Throwable {
        // Hardcoded license creation
        String hardcodedLicense = 
            "{" +
            "    System.out.println(\"[License Patcher] JFrogLicenseManager load() returning hardcoded license\");" +
            "    org.jfrog.license.multiplatform.License license = new org.jfrog.license.multiplatform.License();" +
            "    license.setVersion(1);" +
            "    license.setValidateOnline(java.lang.Boolean.FALSE);" +
            "    java.util.Map products = new java.util.LinkedHashMap();" +
            "    org.jfrog.license.multiplatform.SignedProduct signedProduct = new org.jfrog.license.multiplatform.SignedProduct();" +
            "    org.jfrog.license.api.Product product = new org.jfrog.license.api.Product();" +
            "    product.setId(\"1\");" +
            "    product.setOwner(\"nimah79\");" +
            "    product.setType(org.jfrog.license.api.Product.Type.ENTERPRISE_PLUS);" +
            "    signedProduct.setProduct(product);" +
            "    products.put(\"artifactory\", signedProduct);" +
            "    license.setProducts(products);" +
            "    return license;" +
            "}";

        // Patch load method
        List<CtBehavior> loadMethods = Arrays.stream(clazz.getDeclaredBehaviors())
                .filter(it -> "load".equals(it.getMethodInfo().getName()))
                .collect(Collectors.toList());
        for (CtBehavior method : loadMethods) {
            if (method.getParameterTypes().length == 2) {
                method.setBody(hardcodedLicense);
            }
        }

        // Patch loadLicenseV1 method
        List<CtBehavior> loadLicenseV1Methods = Arrays.stream(clazz.getDeclaredBehaviors())
                .filter(it -> "loadLicenseV1".equals(it.getMethodInfo().getName()))
                .collect(Collectors.toList());
        for (CtBehavior method : loadLicenseV1Methods) {
            method.setBody(hardcodedLicense);
        }

        clazz.detach();
        return clazz.toBytecode();
    }

    private byte[] patchMultiplatformSignedLicense(CtClass clazz) throws Throwable {
        // Patch verify method to be empty
        List<CtBehavior> verifyMethods = Arrays.stream(clazz.getDeclaredBehaviors())
                .filter(it -> "verify".equals(it.getMethodInfo().getName()))
                .collect(Collectors.toList());
        for (CtBehavior method : verifyMethods) {
            method.setBody("{}");
        }

        // Patch sign method to be empty
        List<CtBehavior> signMethods = Arrays.stream(clazz.getDeclaredBehaviors())
                .filter(it -> "sign".equals(it.getMethodInfo().getName()))
                .collect(Collectors.toList());
        for (CtBehavior method : signMethods) {
            method.setBody("{}");
        }

        clazz.detach();
        return clazz.toBytecode();
    }

    private byte[] patchLegacyB(CtClass clazz) throws Throwable {
        // Patch constructor to be empty
        List<CtConstructor> constructors = Arrays.stream(clazz.getDeclaredConstructors()).collect(Collectors.toList());
        for (CtConstructor constructor : constructors) {
            constructor.setBody("{}");
        }

        // Patch a(License, PrivateKey) - return the License unchanged (this returns String)
        List<CtBehavior> aMethods1 = Arrays.stream(clazz.getDeclaredBehaviors())
                .filter(it -> "a".equals(it.getMethodInfo().getName()))
                .collect(Collectors.toList());
        for (CtBehavior method : aMethods1) {
            // This method takes License and PrivateKey and returns String
            // We need to return the License serialized as String
            if (method.getParameterTypes().length == 2) {
                String param1Type = method.getParameterTypes()[0].getName();
                if (param1Type.equals("org.jfrog.license.legacy.License")) {
                    // Returns String - serialize the license and return
                    method.setBody(
                        "{" +
                        "    org.jfrog.license.legacy.a serializer = new org.jfrog.license.legacy.a();" +
                        "    byte[] serialized = serializer.a($1);" +
                        "    return new String(serialized);" +
                        "}"
                    );
                }
            }
        }

        // Patch b(byte[]) - return the byte[] unchanged
        List<CtBehavior> bMethods = Arrays.stream(clazz.getDeclaredBehaviors())
                .filter(it -> "b".equals(it.getMethodInfo().getName()))
                .collect(Collectors.toList());
        for (CtBehavior method : bMethods) {
            if (method.getParameterTypes().length == 1) {
                method.setBody("{ return $1; }");
            }
        }

        // Patch a(String, PublicKey) - return hardcoded SignedLicense (this returns SignedLicense)
        List<CtBehavior> aMethods2 = Arrays.stream(clazz.getDeclaredBehaviors())
                .filter(it -> "a".equals(it.getMethodInfo().getName()))
                .collect(Collectors.toList());
        for (CtBehavior method : aMethods2) {
            if (method.getParameterTypes().length == 2) {
                String param1Type = method.getParameterTypes()[0].getName();
                if (param1Type.equals("java.lang.String")) {
                    // This method takes String and PublicKey, returns SignedLicense
                    method.setBody(
                        "{" +
                        "    System.out.println(\"[License Patcher] org.jfrog.license.legacy.b.a(String,PublicKey) returning hardcoded SignedLicense\");" +
                        "    org.jfrog.license.legacy.License license = new org.jfrog.license.legacy.License();" +
                        "    license.setProductId(1);" +
                        "    license.setProductName(\"Artifactory\");" +
                        "    license.setSubject(\"nimah79\");" +
                        "    java.util.Calendar cal = java.util.Calendar.getInstance();" +
                        "    cal.add(java.util.Calendar.YEAR, 30);" +
                        "    license.setValidUntil(cal.getTime());" +
                        "    license.setType(org.jfrog.license.legacy.License.Type.COMMERCIAL);" +
                        "    org.jfrog.license.legacy.SignedLicense signedLicense = new org.jfrog.license.legacy.SignedLicense();" +
                        "    org.jfrog.license.legacy.a serializer = new org.jfrog.license.legacy.a();" +
                        "    try { java.lang.reflect.Field f = org.jfrog.license.legacy.SignedLicense.class.getDeclaredField(\"license\"); f.setAccessible(true); f.set(signedLicense, serializer.a(license)); } catch (Exception e) { throw new RuntimeException(e); }" +
                        "    return signedLicense;" +
                        "}"
                    );
                }
            }
        }

        clazz.detach();
        return clazz.toBytecode();
    }
}
