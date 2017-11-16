# Architecture

## Haystack Components Architecture Diagram
<img src="../images/Haystack_Components.png"/>

**Haystack Architecture** is designed on the distributed design principles of building a decentralized and decoupled system.
To enable that, we have used **Kafka** as our nervous system that helps us achieve the following:

1. **"batteries included but removable" Docker like Design**: If we consider all our subsystems as 'Batteries', then we have included them making the system ready to use, but have also ensured that the design is in such a way that makes them replaceable as well. 
1. **Highly Resilient**: There is no single point of failure. 
3. **Highly Scalable**: We have completely decentralized our system which helps us to scale every component individually. 

We provide the following **6 subsystems**:

1. [Traces](https://expediadotcom.github.io/haystack/subsystems/traces.html)
2. [Trends](https://expediadotcom.github.io/haystack/subsystems/trends.html)
3. [Collectors](https://expediadotcom.github.io/haystack/subsystems/collectors.html)
4. [Pipes](https://expediadotcom.github.io/haystack/subsystems/pipes.html)
5. [Dependencies](https://expediadotcom.github.io/haystack/subsystems/dependencies.html)
6. [Anomaly Detection](https://expediadotcom.github.io/haystack/subsystems/anomaly_detection.html)

