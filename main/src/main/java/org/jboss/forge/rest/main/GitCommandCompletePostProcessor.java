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

import org.jboss.forge.addon.ui.controller.CommandController;
import org.jboss.forge.rest.dto.ExecutionRequest;
import org.jboss.forge.rest.dto.ExecutionResult;
import org.jboss.forge.rest.hooks.CommandCompletePostProcessor;
import org.jboss.forge.rest.ui.RestUIContext;

import javax.inject.Inject;
import java.io.File;

/**
 * For new projects; lets git add, git commit, git push otherwise lets git add/commit/push any new/udpated changes
 */
public class GitCommandCompletePostProcessor implements CommandCompletePostProcessor {

    @Inject
    public GitCommandCompletePostProcessor() {
    }

    @Override
    public void firePostCompleteActions(String name, ExecutionRequest executionRequest, RestUIContext context, CommandController controller, ExecutionResult results) {
        File basedir = context.getInitialSelectionFile();
        if (name.equals("project-new")) {
            System.out.println("===== adding generated code to git from source folder: " + basedir.getAbsolutePath());
        } else {
            System.out.println("===== added or mutated files in folder: " + basedir.getAbsolutePath());
            File gitFolder = new File(basedir, ".git");
            if (gitFolder.exists() && gitFolder.isDirectory()) {
                System.out.println("======== has .git folder so lets add/commit files then push!");
            }
        }
    }
}
