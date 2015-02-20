/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.forge.rest.model;

import io.fabric8.utils.Objects;
import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.jboss.forge.rest.dto.ProjectDTO;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Represents the storage of the known projects on the file system
 */
@Singleton
public class ProjectsModel {
    private final File projectsDir;
    private final File projectsFile;
    private List<ProjectDTO> projects;

    @Inject
    public ProjectsModel(@ConfigProperty(name = "FORGE_PROJECTS_DIRECTORY", defaultValue = "forgeProjects") String projectsFolder) throws IOException {
        this.projectsDir = new File(projectsFolder);
        this.projectsDir.mkdirs();
        this.projectsFile = new File(projectsDir, "projects.json");
        this.projects = Models.loadJsonValues(projectsFile, ProjectDTO.class);
    }

    public List<ProjectDTO> getProjects() {
        return Collections.unmodifiableList(projects);
    }

    public File getProjectsDir() {
        return projectsDir;
    }

    public File getProjectsFile() {
        return projectsFile;
    }

    public ProjectDTO findByPath(String path) {
        for (ProjectDTO project : projects) {
            if (Objects.equal(path, project.getPath())) {
                return project;
            }
        }
        return null;
    }

    public void setProjects(List<ProjectDTO> projects) throws IOException {
        this.projects = projects;
        save(projects);
    }

    public void add(ProjectDTO element) throws IOException {
        if (!projects.contains(element)) {
            projects.add(element);
            save(projects);
        }
    }

    public void remove(ProjectDTO element) throws IOException {
        if (projects.remove(element)) {
            save(projects);
        }
    }

    public void remove(String path) throws IOException {
        ProjectDTO project = findByPath(path);
        if (project != null) {
            remove(project);
        }
    }

    protected void save(List<ProjectDTO> projects) throws IOException {
        Models.saveJson(projectsFile, projects);
    }

}
