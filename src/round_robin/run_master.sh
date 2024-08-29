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

# Start MasterNode
java -jar ./master.jar $MASTER_PORT $ROUND_ROBIN_PERIOD &

