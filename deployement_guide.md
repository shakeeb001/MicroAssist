# Deployment guide

Deploy **massist-event-messaging-service** and RabbitMQ on a **single Amazon Linux 2023 EC2 instance**.

- RabbitMQ runs in Docker Compose on the instance (`localhost:5672`).
- The Spring Boot API runs as a systemd service (`massist-event-messaging`) on port **8080**.
- Ansible installs packages, copies this repo, starts RabbitMQ, builds the JAR, and enables systemd.
- Ansible does **not** create the EC2 instance. Launch the AMI in the AWS Console first.

```text
Laptop (Ansible + SSH)  -->  EC2 Amazon Linux 2023
                               |-- Docker: RabbitMQ (ports 5672 / 15672 bound on localhost)
                               +-- systemd: Java 21 JAR on :8080
```

## Prerequisites (laptop)

- AWS account and permission to launch EC2
- An SSH key pair in AWS, with the `.pem` file on your laptop
- Ansible 2.15+ (`brew install ansible` on macOS)
- `rsync` (present by default on macOS)
- This repository checked out locally

Install the Ansible collection used to copy files:

```bash
cd ansible
ansible-galaxy collection install -r requirements.yml
```

## 1. Launch Amazon Linux 2023

In the AWS Console:

1. Open **EC2** → **Launch instance**.
2. Name the instance (for example `massist-event-messaging`).
3. AMI: **Amazon Linux 2023**. Match architecture to the instance type (`x86_64` for `t3`, `arm64` for `t4g`).
4. Instance type: **t3.small** or larger. `t3.micro` is usually too small for JDK + RabbitMQ + the JAR.
5. Key pair: select (or create) a key you have as a `.pem` locally. SSH user is `ec2-user`.
6. Network: a public subnet with a public IPv4 address (or attach an Elastic IP afterwards).
7. Storage: 20 GiB gp3 is enough.

### Security group

Create or select a security group with:

| Port | Source | Why |
| --- | --- | --- |
| 22 | Your public IP `/32` | SSH and Ansible |
| 8080 | Your public IP `/32` | HTTP API and Swagger |

Do **not** open **5672** or **15672** to the internet. RabbitMQ stays on the instance loopback. Open 8080 to `0.0.0.0/0` only if you accept a public API.

Launch the instance, then copy its **Public IPv4 DNS** (or Elastic IP).

First SSH check from your laptop:

```bash
ssh -i ~/.ssh/your-key.pem ec2-user@ec2-xx-xx-xx-xx.compute.amazonaws.com
```

Accept the host key when prompted so Ansible `host_key_checking` succeeds. Exit the session.

## 2. Configure Ansible inventory

```bash
cd ansible
cp inventory.ini.example inventory.ini
```

Edit `inventory.ini`:

```ini
[massist]
ec2-xx-xx-xx-xx.compute.amazonaws.com ansible_user=ec2-user ansible_ssh_private_key_file=~/.ssh/your-key.pem
```

Replace the hostname and key path. `inventory.ini` is gitignored so secrets and hostnames stay local.

## 3. Deploy

From the `ansible/` directory:

```bash
ansible-playbook -i inventory.ini deploy.yml
```

The playbook, in order:

1. Installs Java 21 (Amazon Corretto), Git, Docker, and rsync via `dnf`
2. Installs Docker Compose (`docker-compose-plugin`, or a Compose v2 plugin binary if the package is missing)
3. Enables Docker and adds `ec2-user` to the `docker` group
4. Copies this repository to `/opt/massist` on the instance
5. Runs `docker compose up -d` in `massist-event-messaging-service` (pulls `rabbitmq:3-management` if needed)
6. Builds the JAR with `./mvnw -DskipTests package`
7. Installs and starts `massist-event-messaging.service`
8. Waits until `http://127.0.0.1:8080/actuator/health` returns HTTP 200

Re-running the playbook is safe. Changed sources are synced again; systemd restarts when the unit template changes.

## 4. Verify

Replace `<public-ip>` with the instance address:

```bash
curl http://<public-ip>:8080/actuator/health
```

Swagger UI: `http://<public-ip>:8080/swagger-ui.html`

Publish an event:

```bash
curl -X POST http://<public-ip>:8080/api/v1/events \
  -H "Content-Type: application/json" \
  -d '{
    "eventType": "user.registered",
    "routingKey": "massist.event.created",
    "source": "massist-user-service",
    "payload": { "userId": "42", "email": "ada@example.com" }
  }'
```

Recently consumed events:

```bash
curl http://<public-ip>:8080/api/v1/events/consumed
```

On the instance, RabbitMQ management is only on localhost:

```bash
ssh -i ~/.ssh/your-key.pem ec2-user@<public-ip>
curl -u guest:guest http://127.0.0.1:15672/api/overview
```

## 5. Configuration

The systemd unit sets `SPRING_RABBITMQ_HOST=localhost`. Other defaults come from [`massist-event-messaging-service/src/main/resources/application.properties`](massist-event-messaging-service/src/main/resources/application.properties).

| Property | Default | Override |
| --- | --- | --- |
| `server.port` | `8080` | `SERVER_PORT` in the unit |
| `spring.rabbitmq.host` | `localhost` | `SPRING_RABBITMQ_HOST` |
| `spring.rabbitmq.port` | `5672` | `SPRING_RABBITMQ_PORT` |
| `spring.rabbitmq.username` | `guest` | `SPRING_RABBITMQ_USERNAME` |
| `spring.rabbitmq.password` | `guest` | `SPRING_RABBITMQ_PASSWORD` |

To change environment values, edit [`ansible/templates/massist-event-messaging.service.j2`](ansible/templates/massist-event-messaging.service.j2) and re-run the playbook.

## 6. Stop, restart, tear down

On the instance:

```bash
sudo systemctl status massist-event-messaging
sudo systemctl restart massist-event-messaging
sudo systemctl stop massist-event-messaging

cd /opt/massist/massist-event-messaging-service
docker compose down
```

To remove the stack entirely, stop the service, run `docker compose down`, then terminate the EC2 instance in the console.

## Security notes

- RabbitMQ `guest` / `guest` is for this single-box setup only. Do not expose AMQP or the management UI on a public security group.
- Restrict SSH and 8080 to your IP.
- There is no TLS, authentication, or load balancer in this guide.

## Troubleshooting

| Symptom | What to check |
| --- | --- |
| SSH times out | Security group port 22, public IP, subnet route / IGW |
| `UNREACHABLE` from Ansible | Key path, `ec2-user`, host key not yet accepted (`ssh` once by hand) |
| `docker compose` missing | Playbook should install the plugin; check `docker compose version` on the host |
| Image pull fails | Instance needs outbound HTTPS to Docker Hub |
| `permission denied` talking to Docker | Log out/in or let the playbook `reset_connection` run; user must be in `docker` |
| Health stays DOWN / connection refused to 5672 | `docker compose ps` in `/opt/massist/massist-event-messaging-service` |
| Java OOM / instance unresponsive | Use at least `t3.small`; `t3.micro` is often too small |
| Port 8080 unreachable from laptop | Security group 8080 from your IP; confirm `systemctl status massist-event-messaging` |
| `ansible.posix.synchronize` missing | `ansible-galaxy collection install -r ansible/requirements.yml` |
