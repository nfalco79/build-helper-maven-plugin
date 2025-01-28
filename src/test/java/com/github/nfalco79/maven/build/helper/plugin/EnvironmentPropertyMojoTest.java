/*
 * Copyright 2025 Falco Nikolas
 *
 * Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.github.nfalco79.maven.build.helper.plugin;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.maven.project.MavenProject;
import org.junit.Test;

public class EnvironmentPropertyMojoTest {

    @Test
    public void test_overwrite_false() throws Exception {
        MavenProject mavenProject = new MavenProject();
        EnvironmentPropertyMojo mojo = prepareMojo(mavenProject);
        mojo.setProperty("prop1");
        mojo.setOverwrite(false);

        String originalValue = "some value";
        mavenProject.getProperties().put(mojo.getProperty(), originalValue);

        mojo.setValue("value");
        mojo.execute();

        assertThat(mavenProject.getProperties()).containsEntry(mojo.getProperty(), originalValue);
    }

    @Test
    public void test_overwrite_true() throws Exception {
        MavenProject mavenProject = new MavenProject();
        EnvironmentPropertyMojo mojo = prepareMojo(mavenProject);
        mojo.setProperty("prop1");
        mojo.setOverwrite(true);

        mavenProject.getProperties().put(mojo.getProperty(), "some value");

        String expectedValue = "value";
        mojo.setValue(expectedValue);
        mojo.execute();

        assertThat(mavenProject.getProperties()).containsEntry(mojo.getProperty(), expectedValue);
    }

    private EnvironmentPropertyMojo prepareMojo(MavenProject mavenProject) {
        EnvironmentPropertyMojo mojo = new EnvironmentPropertyMojo();
        mojo.setProject(mavenProject);
        mojo.setVariable("JAVA_HOME");
        return mojo;
    }
}
