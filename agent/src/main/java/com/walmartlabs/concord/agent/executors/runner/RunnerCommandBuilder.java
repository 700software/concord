package com.walmartlabs.concord.agent.executors.runner;

/*-
 * *****
 * Concord
 * -----
 * Copyright (C) 2017 - 2018 Walmart Inc.
 * -----
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =====
 */

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class RunnerCommandBuilder {

    private String javaCmd;
    private Path procDir;
    private Path runnerPath;
    private Path runnerCfgPath;
    private String logLevel;
    private Path extraDockerVolumesFile;
    private List<String> extraJvmParams;
    private String mainClass;

    public RunnerCommandBuilder() {
    }

    public RunnerCommandBuilder javaCmd(String javaCmd) {
        this.javaCmd = javaCmd;
        return this;
    }

    public RunnerCommandBuilder procDir(Path procDir) {
        this.procDir = procDir;
        return this;
    }

    public RunnerCommandBuilder runnerPath(Path runnerPath) {
        this.runnerPath = runnerPath;
        return this;
    }

    public RunnerCommandBuilder runnerCfgPath(Path runnerCfgPath) {
        this.runnerCfgPath = runnerCfgPath;
        return this;
    }

    public RunnerCommandBuilder logLevel(String logLevel) {
        this.logLevel = logLevel;
        return this;
    }

    public RunnerCommandBuilder extraDockerVolumesFile(Path extraDockerVolumesFile) {
        this.extraDockerVolumesFile = extraDockerVolumesFile;
        return this;
    }

    public RunnerCommandBuilder jvmParams(List<String> jvmParams) {
        this.extraJvmParams = jvmParams;
        return this;
    }

    public RunnerCommandBuilder mainClass(String mainClass) {
        this.mainClass = mainClass;
        return this;
    }

    public String[] build() {
        List<String> l = new ArrayList<>();

        l.add(javaCmd);

        // JVM arguments, can be customized
        if (extraJvmParams != null) {
            l.addAll(extraJvmParams);
        }

        // mandatory JVM parameters

        // speeds up the start, we don't care much about all potential optimizations done by HotSpot
        l.add("-client");
        // don't do bytecode verification
        l.add("-noverify");
        // enable support for calling vararg methods in JUEL
        l.add("-Djavax.el.varArgs=true");
        // avoid blocking on crypto
        l.add("-Djava.security.egd=file:/dev/./urandom");
        // avoid some performance issues by preferring IPv4 instead of IPv6
        l.add("-Djava.net.preferIPv4Stack=true");
        // workaround for JDK-8142508
        l.add("-Dsun.zip.disableMemoryMapping=true");

        // working directory
        if (procDir != null) {
            l.add("-Duser.dir=" + procDir.toString());
        }

        // default to UTF-8
        l.add("-Dfile.encoding=UTF-8");

        // logback configuration looks for logLevel in JVM properties
        if (logLevel != null) {
            l.add("-DlogLevel=" + logLevel);
        }

        // additional Docker volumes to mount when running containers inside the flow
        if (extraDockerVolumesFile != null) {
            // TODO move into RunnerConfiguration
            l.add("-Dconcord.dockerExtraVolumes=" + extraDockerVolumesFile);
        }

        // classpath
        l.add("-cp");

        // the runner's runtime is stored somewhere in the agent's libraries
        String runner = runnerPath.toString();
        l.add(runner);

        // main class
        if (mainClass == null) {
            mainClass = "com.walmartlabs.concord.runner.Main";
        }
        l.add(mainClass);

        l.add(runnerCfgPath.toString());

        return l.toArray(new String[0]);
    }
}
