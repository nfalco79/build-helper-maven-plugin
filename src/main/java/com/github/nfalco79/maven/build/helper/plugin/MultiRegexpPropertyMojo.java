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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Sets a property using a set of defined rules to calculate a version for .
 */
@Mojo(name = "multi-regexp-property", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true)
public class MultiRegexpPropertyMojo extends AbstractMojo {

    @Parameter(required = true)
    private List<RegexPropertyRule> rules = new ArrayList<>();
    /**
     * The property to set.
     */
    @Parameter(required = true)
    private String property;

    /**
     * The value against apply the regular expression rules.
     */
    @Parameter
    private String value;

    /**
     * The value if no one regular expression matches.
     */
    @Parameter
    private String noRuleMatchValue;

    /**
     * This allows to skip the execution.
     */
    @Parameter(property = "buildhelper.multi-regexp-property.skip", defaultValue = "false")
    private boolean skip;

    /**
     * Overwrite an existing property or not.
     */
    @Parameter(readonly = true, defaultValue = "true")
    private boolean overwrite = true;

    /**
     * The maven project
     */
    @Parameter(readonly = true, defaultValue = "${project}")
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (isSkip()) {
            getLog().info("Skip match regex property per configuration");
            return;
        }

        validate();

        String propertyValue = noRuleMatchValue;
        String valueToMatch = StringUtil.trimToEmpty(value);
        for (RegexPropertyRule rule : getRules()) {
            Pattern pattern = Pattern.compile(rule.getRegexp());
            Matcher matcher = pattern.matcher(valueToMatch);
            if (matcher.find()) {
                String replacement = rule.getReplacement();
                if (matcher.groupCount() > 0) {
                    for (int i = 1; i <= matcher.groupCount(); i++) {
                        replacement = replacement.replace("\\" + i, matcher.group(i));
                    }
                }
                propertyValue = matcher.replaceAll(StringUtil.trimToEmpty(replacement));
                break;
            }
        }

        defineProperty(getProperty(), propertyValue == null ? "" : propertyValue);
    }

    protected void validate() throws MojoFailureException {
        if (StringUtil.isBlank(property)) {
            throw new MojoFailureException("property is required");
        }

        if (getRules().isEmpty()) {
            throw new MojoFailureException("At least a regexp rule is required");
        }
        for (RegexPropertyRule rule : getRules()) {
            if (StringUtil.isBlank(rule.getRegexp())) {
                throw new MojoFailureException("rule regexp is required");
            }
        }
    }

    protected void defineProperty(String name, String value) {
        if (getLog().isDebugEnabled()) {
            getLog().debug("Define property " + name + " = \"" + value + "\"");
        }

        Properties prjProps = project.getProperties();
        if (overwrite) {
            prjProps.put(name, value);
        } else {
            prjProps.putIfAbsent(name, value);
        }
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    public boolean isSkip() {
        return skip;
    }

    public void setSkip(boolean skip) {
        this.skip = skip;
    }

    /* package test purpose */ void setProject(MavenProject project) {
        this.project = project;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public List<RegexPropertyRule> getRules() {
        return rules;
    }

    public void setRules(List<RegexPropertyRule> rules) {
        this.rules = rules != null ? rules : Collections.emptyList();
    }

    public void addRules(RegexPropertyRule rule) {
        this.rules.add(rule);
    }

    public String getNoRuleMatchValue() {
        return noRuleMatchValue;
    }

    public void setNoRuleMatchValue(String noRuleMatchValue) {
        this.noRuleMatchValue = noRuleMatchValue;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }
}
