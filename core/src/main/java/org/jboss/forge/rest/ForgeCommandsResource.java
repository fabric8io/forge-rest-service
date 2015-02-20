package org.jboss.forge.rest;

import io.fabric8.utils.Strings;
import org.jboss.forge.addon.resource.Resource;
import org.jboss.forge.addon.resource.ResourceFactory;
import org.jboss.forge.addon.ui.command.CommandFactory;
import org.jboss.forge.addon.ui.command.UICommand;
import org.jboss.forge.addon.ui.controller.CommandController;
import org.jboss.forge.addon.ui.controller.CommandControllerFactory;
import org.jboss.forge.addon.ui.controller.WizardCommandController;
import org.jboss.forge.addon.ui.input.InputComponent;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.addons.AddonRegistry;
import org.jboss.forge.furnace.services.Imported;
import org.jboss.forge.rest.dto.CommandInfoDTO;
import org.jboss.forge.rest.dto.CommandInputDTO;
import org.jboss.forge.rest.dto.ExecutionRequest;
import org.jboss.forge.rest.dto.ExecutionResult;
import org.jboss.forge.rest.dto.ExecutionStatus;
import org.jboss.forge.rest.dto.UICommands;
import org.jboss.forge.rest.ui.RestUIContext;
import org.jboss.forge.rest.ui.RestUIProvider;
import org.jboss.forge.rest.ui.RestUIRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.jboss.forge.rest.dto.UICommands.createCommandInputDTO;

@Path("/api/forge")
@Stateless
public class ForgeCommandsResource {
    private static final transient Logger LOG = LoggerFactory.getLogger(ForgeCommandsResource.class);

    @Inject
    private Furnace furnace;

    @Inject
    private CommandControllerFactory commandControllerFactory;

    @Inject
    private CommandFactory commandFactory;

    @GET
    public String getInfo() {
        return furnace.getVersion().toString();
    }

    @GET
    @Path("/commandNames")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getCommandNames() {
        List<String> answer = new ArrayList<>();
        try (RestUIContext context = new RestUIContext()) {
            for (String commandName : commandFactory.getCommandNames(context)) {
                answer.add(commandName);
            }
        }
        return answer;
    }

    @GET
    @Path("/commands")
    @Produces(MediaType.APPLICATION_JSON)
    public List<CommandInfoDTO> getCommands() {
        return getCommands(null);
    }

    @GET
    @Path("/commands/{path: .*}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<CommandInfoDTO> getCommands(@PathParam("path") String resourcePath) {
        List<CommandInfoDTO> answer = new ArrayList<>();
        try (RestUIContext context = createUIContext(resourcePath)) {
            for (String name : commandFactory.getCommandNames(context)) {
                CommandInfoDTO dto = createCommandInfoDTO(context, name);
                if (dto != null && dto.isEnabled()) {
                    answer.add(dto);
                }
            }
        }
        return answer;
    }

    @GET
    @Path("/command/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCommandInfo(@PathParam("name") String name) {
        return getCommandInfo(name, null);
    }

    @GET
    @Path("/command/{name}/{path: .*}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCommandInfo(@PathParam("name") String name, @PathParam("path") String resourcePath) {
        CommandInfoDTO answer = null;
        try (RestUIContext context = createUIContext(resourcePath)) {
            answer = createCommandInfoDTO(context, name);
        }
        if (answer != null) {
            return Response.ok(answer).build();
        } else {
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    @GET
    @Path("/commandInput/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCommandInput(@PathParam("name") String name) throws Exception {
        return getCommandInput(name, null);
    }

    @GET
    @Path("/commandInput/{name}/{path: .*}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCommandInput(@PathParam("name") String name, @PathParam("path") String resourcePath) throws Exception {
        System.out.println("Command " + name + " path: " + resourcePath);
        try {
            CommandInputDTO answer = null;
            try (RestUIContext context = createUIContext(resourcePath)) {
                UICommand command = getCommandByName(context, name);
                if (command != null) {
                    CommandController controller = createController(context, command);
                    answer = createCommandInputDTO(context, command, controller);
                }
                if (answer != null) {
                    return Response.ok(answer).build();
                } else {
                    return Response.status(Status.NOT_FOUND).build();
                }
            }
        } catch (Throwable e) {
            LOG.warn("Failed to find input for command " + name + ". " + e, e);
            throw e;
        }
    }

    protected CommandInfoDTO createCommandInfoDTO(RestUIContext context, String name) {
        UICommand command = getCommandByName(context, name);
        CommandInfoDTO answer = null;
        if (command != null) {
            answer = UICommands.createCommandInfoDTO(context, command);
        }
        return answer;
    }

    protected UICommand getCommandByName(RestUIContext context, String name) {
        return commandFactory.getCommandByName(context, name);
    }

    @POST
    @Path("/command/{name}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response executeCommand(@PathParam("name") String name, ExecutionRequest executionRequest) throws Exception {
        try {
            String resourcePath = executionRequest.getResource();
            try (RestUIContext context = createUIContext(resourcePath)) {

                UICommand command = getCommandByName(context, name);
                if (command == null) {
                    return Response.status(Status.NOT_FOUND).build();
                }
                CommandController controller = createController(context, command);
                Map<String, String> requestedInputs = executionRequest.getInputs();
                ExecutionResult answer = null;
                if (controller instanceof WizardCommandController) {
                    LOG.warn("TODO: WizardCommandController " + controller);
                    answer = new ExecutionResult(ExecutionStatus.FAILED, "TODO: WizardCommandController not supported yet", "" + controller, null, null);
                } else {
                    Map<String, InputComponent<?, ?>> inputs = controller.getInputs();
                    Set<String> inputKeys = new HashSet<>(inputs.keySet());
                    if (requestedInputs != null) {
                        inputKeys.retainAll(requestedInputs.keySet());
                        for (String key : inputKeys) {
                            controller.setValueFor(key, requestedInputs.get(key));
                        }
                    }
                    Result result = controller.execute();
                    LOG.debug("Invoked command " + name + " with " + executionRequest + " result: " + result);

                    RestUIProvider provider = context.getProvider();
                    String out = provider.getOut();
                    String err = provider.getErr();
                    String message = result.getMessage();
                    String detail = null;
                    ExecutionStatus status = ExecutionStatus.SUCCESS;
                    answer = new ExecutionResult(status, message, out, err, detail);
                }
                return Response.ok(answer).build();
            }
        } catch (Throwable e) {
            LOG.warn("Failed to invoke command " + name + " on " + executionRequest + ". " + e, e);
            throw e;
        }
    }

    protected CommandController createController(RestUIContext context, UICommand command) throws Exception {
        RestUIRuntime runtime = new RestUIRuntime();
        CommandController controller = commandControllerFactory.createController(context, runtime,
                command);
        controller.initialize();
        return controller;
    }

    protected RestUIContext createUIContext(String resourcePath) {
        AddonRegistry addonRegistry = furnace.getAddonRegistry();
        Imported<ResourceFactory> resourceFactoryImport = addonRegistry.getServices(ResourceFactory.class);
        ResourceFactory resourceFactory = resourceFactoryImport.get();
        Resource<?> selection = null;
        if (Strings.isNotBlank(resourcePath) && resourceFactory != null) {
            File file = new File(resourcePath);
            if (!file.exists() && !resourcePath.startsWith("/")) {
                resourcePath = "/" + resourcePath;
                file = new File(resourcePath);
            }
            if (file.exists()) {
                selection = resourceFactory.create(file);
            } else {
                selection = resourceFactory.create(resourcePath);
            }
        }
        return new RestUIContext(selection);
    }

}
