package com.software.architecture.availability.circuitbreakerpattern.services;

@org.springframework.stereotype.Service
public class Service {

    public String process(String parameter) {
        return "Service Response: " + parameter;
    }

}
