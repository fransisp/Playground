# Employee API Gateway

#### 'A Welcome mat'
In this project I tried to leverage Vert.x framework to create a simple REST API gateway. I’ve choose for this framework because of the nature of a gateway where it has to handle lot of requests from the frontend to REST API backend without a lot of logic built-in. 

#### What does it do?
The asynchronous non-blocking development model and the built-in event bus in Vert.x fit for this very purpose, beside that Vert.x also has some other interesting features / components that really help on implementing a fully functional gateway with lot of features. The gateway in the sample project will have the following basic functions:
1. **Router**
<br />Component that will route request from the frontend to the appropriate backend REST endpoint registered in the Service Discovery
2. **Service discovery**
<br />Component that hold records of all available backend REST endpoint
3. **Circuit breaker**
<br />Component that helps make the gateway more resilient. The component took care if the endpoint is unreachable / down so that the exception can be handled gracefully. Furthermore this component also responsible to stop the unreachable endpoint getting bombarded with requests and only let some requests through after each period of time to check if it’s up and running again (half-open state).

#### Some sanity checks
Since the application only contains of very few business logic, I’ve only accompanied them with 2 sets of integration tests to check if all the above 3 components are working properly. The 2 tests are described as follows:
1. Happy flow tests
<br />Create a REST API request to localhost:8080 where the gateway is configured to run and the router will route this request (after lookup in the service discovery) to the mock backend that runs on port 9000 (configured on the tests) which should return a response back.
2. Circuit breaker tests
<br />Create a REST API request to localhost:8080 where the gateway is configured to run but since there is no endpoint found for this specific API, the circuit breaker should take over and return an bad gateway error back without crashing the whole gateway application.

#### Going above and beyond
Further improvements for the future:
1. Make the REST backend able to register themselves in the Service Discovery records instead of registering manually during the start of application.
2. Add authorization and authentication, since the gateway is the doorway to our backend it should take care of request credentials.
3. Implement HTTPS for communicating with the front end, doing request from the gateway to the REST backend can be done with HTTP if both of them located in one private network.
