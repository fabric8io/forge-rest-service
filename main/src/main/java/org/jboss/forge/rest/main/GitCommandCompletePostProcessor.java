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
package org.jboss.forge.rest.main;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.StoredConfig;
import org.jboss.forge.addon.ui.controller.CommandController;
import org.jboss.forge.furnace.util.Strings;
import org.jboss.forge.rest.dto.ExecutionRequest;
import org.jboss.forge.rest.dto.ExecutionResult;
import org.jboss.forge.rest.hooks.CommandCompletePostProcessor;
import org.jboss.forge.rest.ui.RestUIContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import org.apache.deltaspike.core.api.config.ConfigProperty;
import io.fabric8.cdi.annotations.Service;

/**
 * For new projects; lets git add, git commit, git push otherwise lets git add/commit/push any new/udpated changes
 */
public class GitCommandCompletePostProcessor implements CommandCompletePostProcessor {
    private static final transient Logger LOG = LoggerFactory.getLogger(GitCommandCompletePostProcessor.class);
    private final String gitUser;
    private final String gitPassword;
    private final URL gogsUrl;

    @Inject
    public GitCommandCompletePostProcessor(@Service("GOGS_HTTP_SERVICE") URL gogsUrl,
                                           @ConfigProperty(name = "GIT_DEFAULT_USER") String gitUser,
                                           @ConfigProperty(name = "GIT_DEFAULT_PASSWORD") String gitPassword) {
        this.gogsUrl = gogsUrl;
        this.gitUser = gitUser;
        this.gitPassword = gitPassword;
    }

    @Override
    public void firePostCompleteActions(String name, ExecutionRequest executionRequest, RestUIContext context, CommandController controller, ExecutionResult results) {
        if (name.equals("project-new")) {
            String targetLocation = null;
            String named = null;
            List<Map<String, String>> inputList = executionRequest.getInputList();
            for (Map<String, String> map : inputList) {
                if (Strings.isNullOrEmpty(targetLocation)) {
                    targetLocation = map.get("targetLocation");
                }
                if (Strings.isNullOrEmpty(named)) {
                    named = map.get("named");
                }
            }
            if (Strings.isNullOrEmpty(targetLocation)) {
                LOG.warn("No targetLocation could be found!");
            } else if (Strings.isNullOrEmpty(named)) {
                LOG.warn("No named could be found!");
            } else {
                File basedir = new File(targetLocation, named);
                if (!basedir.isDirectory() || !basedir.exists()) {
                    LOG.warn("Generated project folder does not exist: " + basedir.getAbsolutePath());
                } else {
                    // lets git init...
                    System.out.println("About to git init folder " + basedir.getAbsolutePath());
                    InitCommand initCommand = Git.init();
                    initCommand.setDirectory(basedir);
                    try {
                        Git git = initCommand.call();
                        LOG.info("Initialised an empty git configuration repo at {}", basedir.getAbsolutePath());

                        String remote = gogsUrl.toString();
                        if (!remote.endsWith("/")) {
                            remote += "/" + gitUser + "/" + named + ".git";
                        }
                        LOG.info("Using remote: " + remote);
                        String branch = "master";
                        configureBranch(git, branch, remote);
                        doAddCommitAndPushFiles(git);
                    } catch (GitAPIException e) {
                        handleGitException(e);
                    }

                }
            }
        } else {
            File basedir = context.getInitialSelectionFile();
            String absolutePath = basedir != null ? basedir.getAbsolutePath() : null;
            System.out.println("===== added or mutated files in folder: " + absolutePath);
            if (basedir != null) {
                File gitFolder = new File(basedir, ".git");
                if (gitFolder.exists() && gitFolder.isDirectory()) {
                    System.out.println("======== has .git folder so lets add/commit files then push!");

                }
            }
        }
    }

    protected void configureBranch(Git git, String branch, String remote) {
        // lets update the merge config
        if (!Strings.isNullOrEmpty(branch)) {
            StoredConfig config = git.getRepository().getConfig();
            if (io.hawt.util.Strings.isBlank(config.getString("branch", branch, "remote")) || io.hawt.util.Strings.isBlank(config.getString("branch", branch, "merge"))) {
                config.setString("branch", branch, "remote", remote);
                config.setString("branch", branch, "merge", "refs/heads/" + branch);
                try {
                    config.save();
                } catch (IOException e) {
                    LOG.error("Failed to save the git configuration to " + git.getRepository().getDirectory()
                            + " with branch " + branch + " on remote repo: " + remote + " due: " + e.getMessage() + ". This exception is ignored.", e);
                }
            }
        }
    }

    protected void doAddCommitAndPushFiles(Git git) {
    }

    protected void handleGitException(GitAPIException e) {
    }
}
