#!/bin/bash

CONFIG_FILE="config.cfg"

# Check if config file exists
if [[ ! -f "$CONFIG_FILE" ]]; then
    echo "Config file not found!"
    exit 1
fi

# Source the configuration file
source "$CONFIG_FILE"

# Check if the required parameters are set
if [[ -z "$MASTER_PORT" || -z "$ROUND_ROBIN_PERIOD"
|| -z "$START_WORKER_PORT" || -z "$NUM_WORKERS"
|| -z "$MASTER_ADDRESS"
]]; then
    echo "Missing parameters in the configuration file!"
    exit 1
fi

# Wait a moment to ensure MasterNode starts first
sleep 2

for (( i=0; i<$NUM_WORKERS; i++ ))
do
  worker_port=$(($START_WORKER_PORT + i))
  # Usage: java Worker <master_address> <master_port> <previous_node_address>
  # <next_node_address> <start_port> <index> <total_workers>
  java -jar ./worker.jar $MASTER_ADDRESS  $MASTER_PORT "localhost" "localhost" $START_WORKER_PORT $i $NUM_WORKERS &
  echo "Started WorkerNode $i on port $worker_port"
done

echo "All nodes have been started."
