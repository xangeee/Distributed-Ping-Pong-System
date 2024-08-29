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
if [[ -z "$MASTER_PORT"  || -z "$NUM_WORKERS" || -z "$MASTER_ADDRESS"
]]; then
    echo "Missing parameters in the configuration file!"
    exit 1
fi


for (( i=0; i<$NUM_WORKERS; i++ ))
do
  # Usage: java Worker <master_address> <master_port>
  java -jar ./worker.jar $MASTER_ADDRESS  $MASTER_PORT &
  echo "Started WorkerNode $i"
done

echo "All nodes have been started."
