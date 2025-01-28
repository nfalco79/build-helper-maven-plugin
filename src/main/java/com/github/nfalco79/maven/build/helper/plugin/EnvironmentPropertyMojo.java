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

import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Sets a property value if an environment variable exists.
 */
@Mojo(name = "environment-property", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true)
public class EnvironmentPropertyMojo extends AbstractMojo {

    /**
     * The property to set.
     */
    @Parameter(required = true)
    private String property;

    /**
     * The string to check with regex.
     */
    @Parameter(required = true)
    private String variable;

    /**
     * The value for the property if the variable exists.
     */
    @Parameter(defaultValue = "true")
    private String value;

    /**
     * The value for the property if the variable not exists.
     */
    @Parameter(defaultValue = "false")
    private String noExistValue;

    /**
     * Overwrite an existing property or not.
     */
    @Parameter(defaultValue = "true")
    private boolean overwrite;

    /**
     * This allows to skip the execution.
     */
    @Parameter(property = "buildhelper.environment-property.skip", defaultValue = "false")
    private boolean skip;

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

        String propertyValue = System.getenv().containsKey(getVariable()) ? value : getNoExistValue();

        defineProperty(getProperty(), propertyValue);
    }

    protected void validate() throws MojoFailureException {
        if (StringUtil.isBlank(getProperty())) {
            throw new MojoFailureException("property is required");
        }
        if (StringUtil.isBlank(getVariable())) {
            throw new MojoFailureException("variable is required");
        }
    }

    protected void defineProperty(String name, String value) {
        if (getLog().isDebugEnabled()) {
            getLog().debug("Define property " + name + " = \"" + value + "\"");
        }

        Properties prjProps = project.getProperties();
        if (isOverwrite()) {
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

    public String getNoExistValue() {
        return noExistValue;
    }

    public void setNoExistValue(String noExistValue) {
        this.noExistValue = noExistValue;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    public String getVariable() {
        return variable;
    }

    public void setVariable(String variable) {
        this.variable = variable;
    }
}
