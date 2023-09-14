# Proof of Concept (PoC): Availability Software Architectural Pattern - Circuit Breaker

## Description:
This project is intended to evaluate at least 3 alternatives to implement the
**Circuit Breaker Architectural Pattern** for **Enterprise Java Applications** using **Spring** as its development framework, and to implement one of them.

In the Table bellow the **Circuit Breaker Architectural Pattern**, that is used to promote the Architecture Characteristic or Quality Attribute of **AVAILABILITY**,
is presented **[1]**:

| Architecture Characteristic<br/>(Quality Attribute)                                                                                | Tactic                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        | Architectural Pattern                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       | Benefits                                                                                                                                                       | Tradeoffs                                                                                                                                                                                            |
|------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Availability**: is the software's property that defines if the system is able to perform their tasks **WHEN** its users need it. | **Recover from faults - Retry**: considering that the software system is immerse in an environment in which the failures are expected or common to happen frequently, then retrying the requests should allow users to accomplish their task. A limit of retries should be defined in order not to advise a permanent failure of the system. <br/> <br/> For example, in Genesys PureCloud (one of the bests contact center platforms for the Cloud) **[7]** the retries are recommended to be triggered every exponentially increasing time intervals (3, 9, and 27 seconds) | **Circuit Breaker**: allows **invokers** ***to retry*** their tasks countless times until the failure rate surpasses a threshold **[6]**, instant in which it is considered that the system is dealing with a fault. Therefore, the system is supposed to start troubleshooting its fault and the circuit is then **OPEN** (Check State Diagram **[6]**). Then, after wait duration it changes to a **HALF-OPEN** state in which an amount of permitted task (calls **[6]**) are performed. Finally, it evaluates the failure rate and decide whether is bellow that threshold (case in which it returns to **CLOSE** state to allow the complete traffic) or above it to **OPEN** it gain. | 1. **Retry Policy defined for the whole system** and not per each component.<br/> <br/> 2. **Prevents cascading failures** problem that impacts other systems. | 1. **Too Short Retry value**: false positives could denigrate the **AVAILABILITY** and **PERFORMANCE** of the system. <br/> <br/> 2. **Too Long Retry values**: could derive in unnecessary latency. |

## State Diagram **[6]**:
![circuit_breaker_state_machine.jpg](circuit-breaker-pattern%2Fsrc%2Fmain%2Fresources%2Fstatic%2Fcircuit_breaker_state_machine.jpg)


******

## A special note for Cloud Native Applications:
According to the ***Cloud Native Computing Foundation*** **[8]**, "***Cloud Native** technologies empower organizations to build and run scalable applications in modern, dynamic environments such as public, private, and hybrid clouds.*" 
For those kind of applications (mostly the majority we found nowadays in the Cloud), **[8]** presents a set of **Design (Architectural) Patterns** in the context of six key areas, one in which the **Circuit Breaker Architectural Pattern** is presented: a **Resilient Connectivity Pattern**.
In that case, the **Circuit Breaker Architectural Pattern** will handle ***cascading failures*** scenarios, and will improve resilient connectivity of the caller microservices. For further information check **[8]**.

******

## Alternatives to Implement it:
Based on the Table above, the following 3 implementations are compared with its pros and cons
based on the following criteria: **strategy**, **dashboard** (includes or not), **aggregation of streams**, **API** (less or more convenient), **Rate Limiter** (if enable or configurable or not), **Retry** (if enable or configurable or not), **Cache** (if enable or configurable or not), **TimeLimiter** spent calling a service (if enable or configurable or not), **extensible** with add-ons, **templates** (retry templates), **callbacks** after retries for cross-cutting concerns support. 

|                Alternative                 |                                                                  Strategy                                                                   |                    Dashboard                    | Aggregation of Streams |                  API                   |                                                     Rate Limiter                                                     |           Retry           |                        Cache                        |                                 TimeLimiter                                  |                                                       Extensible<br/>(Add-ons)                                                       | Template | Callbacks upon retries |
|:------------------------------------------:|:-------------------------------------------------------------------------------------------------------------------------------------------:|:-----------------------------------------------:|:----------------------:|:--------------------------------------:|:--------------------------------------------------------------------------------------------------------------------:|:-------------------------:|:---------------------------------------------------:|:----------------------------------------------------------------------------:|:------------------------------------------------------------------------------------------------------------------------------------:|:--------:|:----------------------:|
|   Spring Cloud Netflix - Hystrix **[5]**   |                     **Against cascading failures**: **OPEN** the circuit and forward the call to a **fallback method**                      | To monitor the states with SpringBoot Actuator. | Spring Cloud - Turbine |            Less convenient             |                                                          NO                                                          |            NO             |                         NO                          |                                      NO                                      |                                                                  NO                                                                  |    NO    |           NO           |
|        Resilience4j **[4][11][12]**        |                                      **Resilient systems**: Fault tolerance for remote communications.                                      |                       NO                        |           NO           |            More convenient             | YES<br/> 1. **Limiter mode**: block too frequent requests. <br/> 2. **Bulkhead**: avoid too many concurrent requests | Automatically (Retry API) | [JCache - JSR-107](https://www.baeldung.com/jcache) |                                     TTL                                      |                           SpringBoot, Retpack, Retrofit, Vertx, Dropwizard (metrics), Prometheus (metrics)                           |    NO    |           NO           |
|            Spring Retry **[9]**            |                                  **Re-invoke a failed operation**: *when errors may be transient* **[9]**.                                  |                       NO                        |           NO           | More convenient<br/> (@ - Annotations) |                                                    Up to 3 times.                                                    |    Based on Exceptions    |                         NO                          | 1 second between retries <br/> (Parametrized in retryConfig.properties file) |                                                                  NO                                                                  |   YES    |       Listeners        |
|  Spring Cloud Circuit Breaker **[2][12]**  | **Abstraction layer**: *across different circuit breaker implementations* (Resilience4j, Netflix Hystrix, Sentinel, Spring Retry.) **[2]**. |                       NO                        |           NO           | More convenient (pluggable - builder)  |                                         Failure rate threshold configuration                                         | With Spring Retry plugin  |                         NO                          |                    TimeLimiter configuration (in seconds)                    | *Integrates one or more circuit breakers based on SpringBoot autoconfiguration* **[2]**. <br/> ***Pluggable architecture*** **[2]**. |   YES    |           NO           |


******

### Chosen implementation - Spring Cloud with Resilience4j **[4][11][12]**:

The following Application Properties should be defined per each CircuitBreaker instance: CustomCircuitBreaker in this PoC:

|            Application Properties            |                              Description                               |
|:--------------------------------------------:|:----------------------------------------------------------------------:|
|           minimum-number-of-calls            |    Minimum number of calls bellow threshold before get it **OPEN**     |
| permitted-number-of-calls-in-half-open-state |           Permitted number of calls when it is **HALF_OPEN**           |
|         wait-duration-in-open-state          | Circuit will be **OPEN** during the specified amount of time (seconds) |
|            failure-rate-threshold            |    Minimum number of calls above threshold before get it **CLOSE**     |


1. Service's interface definition: [ServiceController.java](circuit-breaker-pattern%2Fsrc%2Fmain%2Fjava%2Fcom%2Fsoftware%2Farchitecture%2Favailability%2Fcircuitbreakerpattern%2Fcontrollers%2FServiceController.java)

```
    @GetMapping("/service/{parameter}")
    String serviceCall(@PathVariable("parameter") String parameter);
```

2. Service's interface implementation with @CircuitBreaker annotation: [ServiceControllerImpl.java](circuit-breaker-pattern%2Fsrc%2Fmain%2Fjava%2Fcom%2Fsoftware%2Farchitecture%2Favailability%2Fcircuitbreakerpattern%2Fcontrollers%2FServiceControllerImpl.java)

```
    @CircuitBreaker(name= "CustomCircuitBreaker", fallbackMethod = "fallback")
    @Override
    public String serviceCall(String parameter) {
        throw new IllegalArgumentException("Service's error");
        // return service.process(parameter);
    }

    private String fallback(CallNotPermittedException exception) {
        return "Service Failing - Fallback: " + exception.getMessage() + "\n Preventing bottlenecks: try later";
    }
```
The serviceCall was forced to throw an IllegalArgumentException in order to *simulate* the system's faults.
The key code elements are explained in the following table:

|        Key Code Element        |                                                                                Description                                                                                |
|:------------------------------:|:-------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|
| **@CircuitBreaker** annotation | Used in a service's method will instantiate a new CircuitBreaker which name will be use as a reference to set application properties (see table above) for that instance. |
|            **name**            |                                        The name of the CircuitBreaker instance (Used in Application Properties, see table above).                                         |
|       **fallbackMethod**       |          The name of the method that will be used to redirect customer's requests to notify them the service will not be available until its faults get solved.           |

******

### How to test it?

- Execute: ``` mvn spring-boot:run```
- Navigate in your preferred web browser to the following URL: http://localhost:9090/service/parameter
- Try to get the resource the amount of times defined in the application property: ``` CustomCircuitBreaker.minimum-number-of-calls ``` 

![Error Message from the Service.jpg](circuit-breaker-pattern%2Fsrc%2Fmain%2Fresources%2Fstatic%2FError%20Message%20from%20the%20Service.jpg)

- Check the answer of the ***fallback response*** once the ***CustomCircuitBreaker*** is **OPEN**. 

![Fallback Response.jpg](circuit-breaker-pattern%2Fsrc%2Fmain%2Fresources%2Fstatic%2FFallback%20Response.jpg)

- Wait until half amount of seconds defined in the application property: ``` CustomCircuitBreaker.wait-duration-in-open-state ``` 
- Try it again and check that in **HALF_OPEN** it allows the amount of tries defined in the application property: ``` CustomCircuitBreaker.permitted-number-of-calls-in-half-open-state ```
- Check the answer of the fallback method once the ***CustomCircuitBreaker*** is **OPEN** again.

******
******

## References:

1. [Software Architecture in Practice, 4th Edition, Len Bass, Paul Clements, Rick Kazman, 2022, pages 66-68](https://www.amazon.com/Software-Architecture-Practice-SEI-Engineering/dp/0136886094)
2. [Quick Guide to Spring Cloud Circuit Breaker](https://www.baeldung.com/spring-cloud-circuit-breaker)
3. [Introduction to Hystrix](https://www.baeldung.com/introduction-to-hystrix)
4. [Guide to Resilience4j](https://www.baeldung.com/resilience4j)
5. [A Guide to Spring Cloud Netflix – Hystrix](https://www.baeldung.com/spring-cloud-netflix-hystrix)
6. [CircuitBreaker - Getting started with resilience4j-circuitbreaker](https://resilience4j.readme.io/docs/circuitbreaker)
7. [Developer Genesys Cloud - Rate Limits](https://developer.genesys.cloud/platform/api/rate-limits)
8. [Design Patterns for Cloud Native Applications, 1st edition, Kasun Indrasiri, Sriskandarajah Suhothayan, 2021, pages 57-72](https://www.amazon.com/Design-Patterns-Cloud-Native-Applications/dp/1492090719)
9. [Spring Retry](https://www.baeldung.com/spring-retry)
10. [Spring Microservices in Action, 2nd Edition, John Carnell, Illary Huaylupo Sánchez, 2021, pages 177-193](https://www.amazon.com/Spring-Microservices-Action-Second-Carnell/dp/1617296953)
11. [SpringBoot - Resilience4j](https://www.baeldung.com/spring-boot-resilience4j)
12. [Mejorar Microservicios con Spring Cloud y Resilience4j](https://openwebinars.net/academia/aprende/mejorar-microservicios-spring-cloud-resilience4j)