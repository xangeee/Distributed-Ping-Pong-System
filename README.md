## Distributed Ping-Pong System

This project implements a distributed system using Java, consisting of a master node and multiple worker nodes. 
The system supports different messaging patterns including one-to-one, broadcasting, and round-robin messaging.

### System Components

- **Master Node**: Sends commands to worker nodes and handles incoming responses.
- **Worker Nodes**: Receive commands from the master node and send responses back.
- 
### Setup Instructions

1. **Clone the Repository**

   Clone the project repository to your local machine using:
   ```bash
   git clone [<repository-url>](https://github.com/xangeee/Distributed-Ping-Pong-System.git)
   ```

2. **Running the Code**


Navigate to the project directory `src/` and choose the messaging pattern to execute (one-to-one, broadcasting, or round-robin messaging):
```bash
cd src/<messaging_pattern>
./run_master.sh
./run_workers.sh
```
The `config.cfg` file contains the parameter settings.
---

