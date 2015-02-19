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

public class PropertyDTO {
    private final String name;
    private final String description;
    private final String label;
    private final String requiredMessage;
    private final Object value;
    private final String javaType;
    private final String type;
    private final boolean enabled;
    private final boolean required;

    public PropertyDTO(String name, String description, String label, String requiredMessage, Object value, String javaType, String type, boolean enabled, boolean required) {
        this.name = name;
        this.description = description;
        this.label = label;
        this.requiredMessage = requiredMessage;
        this.value = value;
        this.javaType = javaType;
        this.type = type;
        this.enabled = enabled;
        this.required = required;
    }

    @Override
    public String toString() {
        return "PropertyDTO{" +
                "name='" + name + '\'' +
                ", javaType='" + javaType + '\'' +
                ", description='" + description + '\'' +
                '}';
    }

    public String getDescription() {
        return description;
    }

    public String getJavaType() {
        return javaType;
    }

    public String getLabel() {
        return label;
    }

    public String getName() {
        return name;
    }

    public String getRequiredMessage() {
        return requiredMessage;
    }

    public Object getValue() {
        return value;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isRequired() {
        return required;
    }

    public String getType() {
        return type;
    }
}
