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
package com.thedigitalstack.maven.build.helper.plugin;

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

        String propertyValue = System.getenv().containsKey(variable) ? value : noExistValue;

        defineProperty(getProperty(), propertyValue);
    }

    protected void validate() throws MojoFailureException {
        if (isBlank(getProperty())) {
            throw new MojoFailureException("property is required");
        }
        if (isBlank(variable)) {
            throw new MojoFailureException("variable is required");
        }
    }

    /* we do not want import third party library just for a method neither plexus util */
    private boolean isBlank(String value) {
        return value == null || value.trim().length() == 0;
    }

    protected void defineProperty(String name, String value) {
        if (getLog().isDebugEnabled()) {
            getLog().debug("Define property " + name + " = \"" + value + "\"");
        }

        project.getProperties().put(name, value);
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
}
