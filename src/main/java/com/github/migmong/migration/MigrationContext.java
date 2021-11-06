package com.github.migmong.migration;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

/**
 * MigrationInfo context to be passed to every migration unit.
 */
@Data
public class MigrationContext
{
    private Map<String, Object> customVariables = new HashMap<>();


    public void setVariable(String name, Object variable)
    {
        customVariables.put(name, variable);
    }

    public Object getVariable(String name)
    {
        return customVariables.get(name);
    }

}
