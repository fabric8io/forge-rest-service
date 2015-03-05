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
package org.jboss.forge.rest.dto;

import io.fabric8.utils.Strings;
import org.jboss.forge.addon.convert.Converter;
import org.jboss.forge.addon.convert.ConverterFactory;
import org.jboss.forge.addon.projects.ProjectProvider;
import org.jboss.forge.addon.projects.ProjectType;
import org.jboss.forge.addon.ui.command.UICommand;
import org.jboss.forge.addon.ui.controller.CommandController;
import org.jboss.forge.addon.ui.input.InputComponent;
import org.jboss.forge.addon.ui.input.SelectComponent;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.result.CompositeResult;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.util.InputComponents;
import org.jboss.forge.rest.ui.RestUIContext;
import org.jboss.forge.rest.ui.RestUIProvider;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static org.jboss.forge.furnace.util.Strings.capitalize;
import static org.jboss.forge.rest.dto.JsonSchemaTypes.getJsonSchemaTypeName;

/**
 */
public class UICommands {
    public static CommandInfoDTO createCommandInfoDTO(RestUIContext context, UICommand command) {
        CommandInfoDTO answer;
        UICommandMetadata metadata = command.getMetadata(context);
        String metadataName = unshellifyName(metadata.getName());
        String id = shellifyName(metadataName);
        String description = metadata.getDescription();
        String category = toStringOrNull(metadata.getCategory());
        String docLocation = toStringOrNull(metadata.getDocLocation());
        boolean enabled = command.isEnabled(context);
        answer = new CommandInfoDTO(id, metadataName, description, category, docLocation, enabled);
        return answer;
    }


    public static CommandInputDTO createCommandInputDTO(RestUIContext context, UICommand command, CommandController controller) throws Exception {
        CommandInfoDTO info = createCommandInfoDTO(context, command);
        CommandInputDTO inputInfo = new CommandInputDTO(info);
        Map<String, InputComponent<?, ?>> inputs = controller.getInputs();
        if (inputs != null) {
            Set<Map.Entry<String, InputComponent<?, ?>>> entries = inputs.entrySet();
            for (Map.Entry<String, InputComponent<?, ?>> entry : entries) {
                String key = entry.getKey();
                InputComponent<?, ?> input = entry.getValue();
                PropertyDTO dto = UICommands.createInputDTO(input);
                inputInfo.addProperty(key, dto);
            }
        }
        return inputInfo;
    }

    protected static String toStringOrNull(Object value) {
        return value != null ? value.toString() : null;
    }

    public static PropertyDTO createInputDTO(InputComponent<?, ?> input) {
        String name = input.getName();
        String description = input.getDescription();
        String label = input.getLabel();
        String requiredMessage = input.getRequiredMessage();
        char shortNameChar = input.getShortName();
        String shortName = Character.toString(shortNameChar);
        Object value = input.getValue();
        if (value != null) {
            // lets make a safe way to turn to JSON
            value = toSafeJsonValue(value);
        }
        Class<?> valueType = input.getValueType();
        String javaType = null;
        if (valueType != null) {
            javaType = valueType.getCanonicalName();
        }
        String type = getJsonSchemaTypeName(valueType);
        boolean enabled = input.isEnabled();
        boolean required = input.isRequired();
        List<Object> enumValues = new ArrayList<>();
        if (input instanceof SelectComponent) {
            SelectComponent selectComponent = (SelectComponent) input;
            Iterable valueChoices = selectComponent.getValueChoices();
            for (Object valueChoice : valueChoices) {
                Object jsonValue = toSafeJsonValue(valueChoice);
                enumValues.add(jsonValue);
            }

        }
        if (enumValues.isEmpty()) {
            enumValues = null;
        }
        return new PropertyDTO(name, description, label, requiredMessage, value, javaType, type, enabled, required, enumValues);
    }

    /**
     * Lets return a safe JSON value
     */
    protected static Object toSafeJsonValue(Object value) {
        if (value == null) {
            return null;
        } else {
            if (value instanceof Number) {
                return value;
            }
            if (value instanceof ProjectProvider) {
                ProjectProvider projectProvider = (ProjectProvider) value;
                return projectProvider.getType();
            }
            if (value instanceof ProjectType) {
                ProjectType projectType = (ProjectType) value;
                return projectType.getType();
            }
            return value.toString();
        }
    }

    private static final Pattern WHITESPACES = Pattern.compile("\\W+");
    private static final Pattern COLONS = Pattern.compile("\\:");

    /**
     * "Shellifies" a name (that is, makes the name shell-friendly) by replacing spaces with "-" and removing colons
     *
     * @param name
     * @return
     */
    public static String shellifyName(String name) {
       return COLONS.matcher(WHITESPACES.matcher(name.trim()).replaceAll("-")).replaceAll("").toLowerCase();
    }


    /**
     * A name of the form "foo-bar-whatnot" is turned into "Foo: Bar Whatnot"
     */
    public static String unshellifyName(String name) {
        if (Strings.isNotBlank(name)) {
            if (name.indexOf('-') >= 0 && name.toLowerCase().equals(name)) {
                String[] split = name.split("-");
                StringBuffer buffer = new StringBuffer();
                int idx = 0;
                for (String part : split) {
                    if (idx == 1) {
                        buffer.append(": ");
                    } else if (idx > 1) {
                        buffer.append(" ");
                    }
                    buffer.append(capitalize(part));
                    idx++;
                }
                return buffer.toString();
            }
        }
        return name;
    }

    public static void populateController(Map<String, String> requestedInputs, CommandController controller, ConverterFactory converterFactory) {
        Map<String, InputComponent<?, ?>> inputs = controller.getInputs();
        Set<String> inputKeys = new HashSet<>(inputs.keySet());
        if (requestedInputs != null) {
            inputKeys.retainAll(requestedInputs.keySet());
            for (String key : inputKeys) {
                String textValue = requestedInputs.get(key);
                Object value = textValue;
                InputComponent<?, ?> component = inputs.get(key);
                if (component != null) {
                    Converter<String, ?> valueConverter = component.getValueConverter();
                    if (valueConverter != null) {
                        value = valueConverter.convert(textValue);
                    } else {
                        Class<?> valueType = component.getValueType();
                        if (valueType.isEnum()) {
                            Class<? extends Enum> enumType = (Class<? extends Enum>) valueType;
                            value = Enum.valueOf(enumType, textValue);
                        }
                    }
                    InputComponents.setValueFor(converterFactory, component, value);
                } else {
                    controller.setValueFor(key, value);
                }

                Object actual = controller.getValueFor(key);
                System.out.println("=== " + key + " = " + actual + " Class: " + (actual != null ? actual.getClass().getName() : "null"));
            }
        }
    }

    public static ExecutionResult createExecutionResult(RestUIContext context, Result result, boolean canMoveToNextStep) {
        RestUIProvider provider = context.getProvider();
        String out = provider.getOut();
        String err = provider.getErr();
        String message = result != null ? getResultMessage(result) : null;
        String detail = null;
        ExecutionStatus status = ExecutionStatus.SUCCESS;
        return new ExecutionResult(status, message, out, err, detail, canMoveToNextStep);
    }

    protected static String getResultMessage(Result result) {
        if (result instanceof CompositeResult) {
            CompositeResult compositeResult = (CompositeResult) result;
            List<Result> results = compositeResult.getResults();
            StringBuilder buffer = new StringBuilder();
            for (Result childResult : results) {
                String childResultMessage = getResultMessage(childResult);
                if (Strings.isNotBlank(childResultMessage)) {
                    if (buffer.length() > 0) {
                        buffer.append("\n");
                    }
                    buffer.append(childResultMessage);
                }
            }
            return buffer.toString();
        } else {
            return result.getMessage();
        }
    }
}
