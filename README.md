## Step 1: Setup the Spring Boot Kafka Tracking API

### 1.1 Clone the Repository
Clone the project repository with source code:

```bash
git clone <repo_url>
cd kafka-tracking-api
```

### 1.2 Build the Spring Boot Application
Use Maven to build the project:

```bash
mvn clean package
```

This generates a JAR file located in the `target` directory.

## Step 2: Dockerize the Application

### 2.1 Create Dockerfile
Ensure the following `Dockerfile` is in the root directory:

```dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/kafka-tracking-api.jar kafka-tracking-api.jar

ENTRYPOINT ["java", "-jar", "kafka-tracking-api.jar"]
```

### 2.2 Build Docker Image
Build the Docker image:

```bash
docker build -t kafka-tracking-api:latest .
```

## Step 3: Setup Kubernetes

### 3.1 Create Kubernetes YAML files
The project uses Kubernetes resources to deploy Kafka, Redis, and Spring Boot API.

- **Zookeeper Deployment**: A required component for Kafka.
- **Kafka Deployment**: Kafka broker deployment.
- **Redis Deployment**: Caching layer.
- **Spring Boot API Deployment**: The actual tracking API microservice.

These YAML files are in the `kubernetes/` directory and include:

- `zookeeper-deployment.yaml`
- `kafka-deployment.yaml`
- `redis-deployment.yaml`
- `tracking-api-deployment.yaml`

### 3.2 Apply Kubernetes Resources
Run the following command to deploy the resources to Kubernetes:

```bash
kubectl apply -f kubernetes/zookeeper-deployment.yaml
kubectl apply -f kubernetes/kafka-deployment.yaml
kubectl apply -f kubernetes/redis-deployment.yaml
kubectl apply -f kubernetes/tracking-api-deployment.yaml
```

## Step 4: Install Prometheus and Grafana

### 4.1 Install Prometheus
Add the Prometheus Helm repository:

```bash
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update
```

Install Prometheus using Helm:

```bash
helm install prometheus-release prometheus-community/kube-prometheus-stack
```

### 4.2 Expose Prometheus Dashboard
Run this command to expose Prometheus:

```bash
kubectl port-forward svc/prometheus-release-prometheus 9090:9090
```

Now you can access Prometheus at `http://localhost:9090`.

## Step 5: Setup KEDA for Kafka Autoscaling

### 5.1 Install KEDA
Add KEDA Helm repository:

```bash
helm repo add kedacore https://kedacore.github.io/charts
helm repo update
```

Install KEDA:

```bash
helm install keda kedacore/keda --namespace keda --create-namespace
```

### 5.2 Create KEDA ScaledObject
KEDA autoscaling configuration is defined in a `ScaledObject` that triggers scaling based on Kafka lag.

Create the `kafka-scaledobject.yaml`:

```yaml
apiVersion: keda.sh/v1alpha1
kind: ScaledObject
metadata:
  name: kafka-consumer-scaler
  namespace: default
spec:
  scaleTargetRef:
    name: tracking-api
  minReplicaCount: 1
  maxReplicaCount: 5
  triggers:
    - type: kafka
      metadata:
        bootstrapServers: kafka:9092
        topic: tracking-events
        consumerGroup: tracking-consumer-group
        lagThreshold: "5"
```

Apply the configuration:

```bash
kubectl apply -f kafka-scaledobject.yaml
```

## Step 6: Configure Prometheus to Scrape Kafka Metrics

Add the following to the Prometheus ConfigMap to scrape Kafka metrics:

```yaml
  - job_name: 'kafka'
    static_configs:
      - targets: ['kafka:9092']
```

Restart Prometheus:

```bash
kubectl rollout restart deployment prometheus-release-prometheus
```

## Step 7: Setup Grafana

### 7.1 Expose Grafana Dashboard
Run this command to expose Grafana:

```bash
kubectl port-forward svc/prometheus-release-grafana 3000:80
```

Access Grafana at `http://localhost:3000`.

### 7.2 Configure Grafana
- Add Prometheus as a Data Source with URL `http://prometheus-release-prometheus:9090`.
- Import Kafka Overview (ID: `7589`) and Spring Boot Metrics (ID: `10280`) dashboards.

## Step 8: Monitor Kafka Autoscaling

### 8.1 Generate Tracking Numbers
Publish some tracking numbers by sending HTTP requests to your API:

```bash
curl "http://<API_SERVICE_IP>:8080/generate-tracking-number?customerId=12345"
```

### 8.2 Check Auto-Scaling in Action
To see autoscaling, check the HPA (Horizontal Pod Autoscaler):

```bash
kubectl get hpa
```

## Step 9: Clean Up

To clean up the resources:

```bash
kubectl delete -f kubernetes/zookeeper-deployment.yaml
kubectl delete -f kubernetes/kafka-deployment.yaml
kubectl delete -f kubernetes/redis-deployment.yaml
kubectl delete -f kubernetes/tracking-api-deployment.yaml
kubectl delete -f kafka-scaledobject.yaml
```

## Conclusion
This setup provides a scalable Kafka-based tracking number generator API with autoscaling, monitoring, and alerting capabilities. By using Spring Boot, Kafka, Redis, KEDA, and Prometheus, you have an efficient and highly available solution for managing tracking numbers in real-time.
