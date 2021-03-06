/*
 * Copyright 2017 the original author or authors.
 *
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
 */

package org.gradle.language.base.internal.compile;

import org.gradle.api.tasks.WorkResult;
import org.gradle.language.base.compile.CompilerVersion;
import org.gradle.util.VersionNumber;

public class VersionAwareCompiler<T extends CompileSpec> implements Compiler<T> {

    private final CompilerVersion compilerVersion;
    private final Compiler<T> compiler;

    public VersionAwareCompiler(Compiler<T> compiler, String type, VersionNumber version) {
        this.compiler = compiler;
        this.compilerVersion = new DefaultCompilerVersion(type, version);
    }

    @Override
    public WorkResult execute(T spec) {
        return compiler.execute(spec);
    }

    public CompilerVersion getVersion() {
        return compilerVersion;
    }

}
