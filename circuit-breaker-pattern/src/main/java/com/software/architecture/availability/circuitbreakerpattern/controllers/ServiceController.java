package com.software.architecture.availability.circuitbreakerpattern.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

public interface ServiceController {

    @GetMapping("/service/{parameter}")
    String serviceCall(@PathVariable("parameter") String parameter);

}
