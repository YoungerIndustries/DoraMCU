package com.yisy.modules.doramcu.dto;

import java.util.List;
import java.util.Map;

/**
 * Comprehensive details about a player's resource pipeline.
 * <p>
 * Extends ResourcePipelineSummary with detailed information about
 * mining operations, transport orders, and construction queue.
 * </p>
 */
public class PipelineDetailsDto {

    /** Summary of the pipeline state */
    private ResourcePipelineSummary summary;

    /** Detailed mining operations */
    private List<MiningOperationDto> miningOperations;

    /** Active transport orders */
    private List<TransportOrderDto> transportOrders;

    /** Active construction orders */
    private List<ConstructionOrderDto> constructionOrders;

    /** Processing facility status */
    private List<ProcessingFacilityDto> processingFacilities;

    /** Module assembler status */
    private List<ModuleAssemblerDto> assemblers;

    public PipelineDetailsDto() {
    }

    public ResourcePipelineSummary getSummary() {
        return summary;
    }

    public void setSummary(ResourcePipelineSummary summary) {
        this.summary = summary;
    }

    public List<MiningOperationDto> getMiningOperations() {
        return miningOperations;
    }

    public void setMiningOperations(List<MiningOperationDto> miningOperations) {
        this.miningOperations = miningOperations;
    }

    public List<TransportOrderDto> getTransportOrders() {
        return transportOrders;
    }

    public void setTransportOrders(List<TransportOrderDto> transportOrders) {
        this.transportOrders = transportOrders;
    }

    public List<ConstructionOrderDto> getConstructionOrders() {
        return constructionOrders;
    }

    public void setConstructionOrders(List<ConstructionOrderDto> constructionOrders) {
        this.constructionOrders = constructionOrders;
    }

    public List<ProcessingFacilityDto> getProcessingFacilities() {
        return processingFacilities;
    }

    public void setProcessingFacilities(List<ProcessingFacilityDto> processingFacilities) {
        this.processingFacilities = processingFacilities;
    }

    public List<ModuleAssemblerDto> getAssemblers() {
        return assemblers;
    }

    public void setAssemblers(List<ModuleAssemblerDto> assemblers) {
        this.assemblers = assemblers;
    }
}
