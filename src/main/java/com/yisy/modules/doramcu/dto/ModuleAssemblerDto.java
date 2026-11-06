package com.yisy.modules.doramcu.dto;

/**
 * Status of a module assembler.
 */
public class ModuleAssemblerDto {

    /** Module type being assembled */
    private String moduleType;

    /** Whether the assembler is currently assembling */
    private boolean assembling;

    /** Turns remaining until next module (0 if producing now) */
    private int turnsRemaining;

    public ModuleAssemblerDto() {
    }

    public String getModuleType() {
        return moduleType;
    }

    public void setModuleType(String moduleType) {
        this.moduleType = moduleType;
    }

    public boolean isAssembling() {
        return assembling;
    }

    public void setAssembling(boolean assembling) {
        this.assembling = assembling;
    }

    public int getTurnsRemaining() {
        return turnsRemaining;
    }

    public void setTurnsRemaining(int turnsRemaining) {
        this.turnsRemaining = turnsRemaining;
    }
}
