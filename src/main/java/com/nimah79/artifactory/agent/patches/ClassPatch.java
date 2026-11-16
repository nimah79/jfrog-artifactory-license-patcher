package com.nimah79.artifactory.agent.patches;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.List;

abstract public class ClassPatch implements ClassFileTransformer {
    public final List<String> targetClasses;

    public ClassPatch(String... targetClasses) {
        this.targetClasses = Arrays.asList(targetClasses);
    }

    @Override
    public final byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        var clazz = className.replace("/", ".");
        if (targetClasses.contains(clazz)) {
            System.out.println("[License Patcher] Patching class: " + clazz + " (loader = " + loader + ", source = " + protectionDomain.getCodeSource().getLocation() + ")");
            // The classes being patched live inside webapp classloaders. The default
            // ClassPool can only see the system classpath, so without appending the
            // defining loader here, every generated body that references sibling
            // classes (License, Product, SignedLicense, ...) fails to compile.
            var ctPool = ClassPool.getDefault();
            ctPool.appendClassPath(new LoaderClassPath(loader));

            try {
                if (classBeingRedefined == null) {
                    return this.onTransform(loader, clazz, ctPool.makeClass(new ByteArrayInputStream(classfileBuffer)), classfileBuffer);
                } else {
                    return this.onRetransform(clazz, ctPool.makeClass(new ByteArrayInputStream(classfileBuffer)), classfileBuffer, classBeingRedefined);
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        return classfileBuffer;
    }

    byte[] onTransform(ClassLoader loader, String className, CtClass clazz, byte[] classBuf) throws Throwable {
        return classBuf;
    }

    byte[] onRetransform(String className, CtClass clazz, byte[] classBuf, Class<?> classBeingRedefined) throws Throwable {
        return classBuf;
    }
}
