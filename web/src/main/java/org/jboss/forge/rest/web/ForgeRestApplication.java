package org.jboss.forge.rest.web;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.jboss.forge.rest.CommandsResource;
import org.jboss.forge.rest.ProjectsResource;
import org.jboss.forge.rest.RootResource;

import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/")
public class ForgeRestApplication extends Application {
    @Inject
    RootResource rootResource;

    @Inject
    CommandsResource forgeResource;

    @Inject
    ProjectsResource projectsResource;

    @Override
    public Set<Object> getSingletons() {
        return new HashSet<Object>(
                Arrays.asList(
                        rootResource,
                        forgeResource,
                        projectsResource,
                        new JacksonJsonProvider()
                )
        );
    }
}