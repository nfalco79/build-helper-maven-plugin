/*
 * Copyright 2023 Falco Nikolas
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

import org.apache.maven.project.MavenProject;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class MultiRegexpPropertyMojoTest {

    @Test
    public void multiple_rule_at_least_one_matches() throws Exception {
        MavenProject mavenProject = new MavenProject();
        MultiRegexpPropertyMojo mojo = prepareMojo(mavenProject);

        mojo.setValue("master");
        mojo.execute();

        Assertions.assertThat(mavenProject.getProperties()).containsEntry(mojo.getProperty(), "latest");
    }

    @Test
    public void replacement_placeholder_rule() throws Exception {
        MavenProject mavenProject = new MavenProject();
        MultiRegexpPropertyMojo mojo = prepareMojo(mavenProject);

        mojo.setValue("support/1.0.x");
        mojo.execute();

        Assertions.assertThat(mavenProject.getProperties()).containsEntry(mojo.getProperty(), "1.0-latest");
    }

    @Test
    public void multiple_replacement_placeholder_rule() throws Exception {
        MavenProject mavenProject = new MavenProject();
        MultiRegexpPropertyMojo mojo = prepareMojo(mavenProject);

        mojo.addRules(new RegexPropertyRule("(feature)/(\\w+)-(\\d+)", "\\3 prj \\2"));
        mojo.setValue("feature/ALM-123");
        mojo.execute();

        Assertions.assertThat(mavenProject.getProperties()).containsEntry(mojo.getProperty(), "123 prj ALM");
    }

    @Test
    public void test_no_match_regexp_value() throws Exception {
        MavenProject mavenProject = new MavenProject();
        MultiRegexpPropertyMojo mojo = prepareMojo(mavenProject);

        mojo.addRules(new RegexPropertyRule("(feature)/(\\w+)-(\\d+)", "\\3 prj \\2"));
        mojo.setValue("123");
        mojo.execute();

        Assertions.assertThat(mavenProject.getProperties()).containsEntry(mojo.getProperty(), mojo.getNoRuleMatchValue());
    }

    private MultiRegexpPropertyMojo prepareMojo(MavenProject mavenProject) {
        MultiRegexpPropertyMojo mojo = new MultiRegexpPropertyMojo();
        mojo.setProject(mavenProject);
        mojo.addRules(new RegexPropertyRule("^master$", "latest"));
        mojo.addRules(new RegexPropertyRule("^support\\/(.+).x$", "\\1-latest"));
        mojo.setNoRuleMatchValue("1.0.0-SNAPSHOT");
        mojo.setProperty("image.tag");
        return mojo;
    }
}
