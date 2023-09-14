package com.software.architecture.availability.circuitbreakerpattern.controllers;

import com.software.architecture.availability.circuitbreakerpattern.services.Service;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ServiceControllerImpl implements ServiceController {

    @Autowired
    Service service;

    @CircuitBreaker(name= "CustomCircuitBreaker", fallbackMethod = "fallback")
    @Override
    public String serviceCall(String parameter) {
        throw new IllegalArgumentException("Service's error");
        // return service.process(parameter);
    }

    private String fallback(CallNotPermittedException exception) {
        return "Service Failing - Fallback: " + exception.getMessage() + "\n Preventing bottlenecks: try later";
    }
}
