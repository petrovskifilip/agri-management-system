package com.finki.agrimanagement.service;

public interface IrrigationExecutionService {

    void executeIrrigation(Long irrigationId);

    void stopIrrigation(Long irrigationId);
}
