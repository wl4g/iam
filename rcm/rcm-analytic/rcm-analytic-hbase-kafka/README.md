# IAM analytic for HBase-Kafka

## Debugging quick

- Create kafka topic

```bash
kafka-topics.sh --zookeeper localhost:2181 --create --topic iam_event --partitions 3 --replication-factor 1
```

- Simulate production event messages

```bash
for (( i=1; i<=100; i++ )); do \
echo "mykey-$i:{\"timestamp\":$(date +'%s'),\"eventType\":\"AUTHC_SUCCESS\",\"principal\":\"jack_$i\",\"remoteIp\":\"2.2.$(($RANDOM*10/$RANDOM)).$(($RANDOM*10/$RANDOM))\",\"coordinates\":\"113.$RANDOM,20.$RANDOM\",\"message\":\"my message $i\",\"attributes\":null}" | kafka-console-producer.sh \
--broker-list localhost:9092 \
--topic iam_event \
--property parse.key=true --property key.separator=:; \
done;
```

- Console consuming

```bash
kafka-console-consumer.sh --zookeeper localhost:2181 --topic iam_event
```

- Cleanup scene

```bash
kafka-topics.sh --zookeeper localhost:2181 --delete --topic iam_event
```
