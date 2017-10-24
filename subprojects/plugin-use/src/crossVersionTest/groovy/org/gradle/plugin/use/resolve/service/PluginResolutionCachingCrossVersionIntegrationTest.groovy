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

package org.gradle.plugin.use.resolve.service

import org.gradle.integtests.fixtures.CrossVersionIntegrationSpec
import org.gradle.integtests.fixtures.TargetVersions
import org.gradle.test.fixtures.file.LeaksFileHandles
import org.gradle.test.fixtures.file.TestFile
import org.gradle.test.fixtures.plugin.PluginBuilder
import org.gradle.test.fixtures.server.http.MavenHttpPluginRepository

@TargetVersions("4.4+")
@LeaksFileHandles
class PluginResolutionCachingCrossVersionIntegrationTest extends CrossVersionIntegrationSpec {

    public static final String PLUGIN_ID = "org.my.myplugin"
    public static final String VERSION = "1.0"
    public static final String GROUP = "my"
    public static final String ARTIFACT = "plugin"

    TestFile gradleUserHome

    void setup() {
        requireOwnGradleUserHomeDir()
        gradleUserHome = file("gradle-home")
    }

    def "cached resolution by previous version is used by this version"() {
        given:
        def currentExecuter = version(current).withGradleUserHomeDir(gradleUserHome)
        def previousExecuter = version(previous).withGradleUserHomeDir(gradleUserHome)

        and:
        MavenHttpPluginRepository pluginRepo = MavenHttpPluginRepository.asGradlePluginPortal(currentExecuter, mavenRepo)

        and:
        pluginRepo.start()

        and:
        new PluginBuilder(file(ARTIFACT)).with {
            addPlugin("project.ext.pluginApplied = true", PLUGIN_ID)
            publishAs(GROUP, ARTIFACT, VERSION, pluginRepo, currentExecuter).allowAll()
        }

        and:
        file("build.gradle") << """
            plugins { id '$PLUGIN_ID' version '$VERSION' }
            task pluginApplied {
                doLast {
                    assert project.pluginApplied
                }
            }
        """.stripIndent()

        expect:
        pluginRepo.expectPluginResolution(PLUGIN_ID, VERSION, GROUP, ARTIFACT, VERSION)

        and:
        previousExecuter.withTasks("pluginApplied").run()

        and:
        pluginRepo.expectCachedPluginResolution(PLUGIN_ID, VERSION, GROUP, ARTIFACT, VERSION)

        and:
        currentExecuter.withTasks("pluginApplied").run()

        cleanup:
        pluginRepo.stop()
        currentExecuter.stop()
        previousExecuter.stop()
    }
}
