package com.nimah79.artifactory.agent;

import com.nimah79.artifactory.agent.patches.LicensePatcher;

import java.lang.instrument.Instrumentation;

public class AgentMain {
    public static void premain(String args, Instrumentation ins) {
        System.out.println("[License Patcher] Agent loaded");
        ins.addTransformer(new LicensePatcher());
    }
}
