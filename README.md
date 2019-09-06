# Gerrit-RabbitMQ-Eiffel-Service

*(PoC)* This service listens to a RabbitMQ queue containing Gerrit events, converts them to Eiffel events and sends them back to a RabbitMQ exchange.

See supported events at https://github.com/chrillebile/gerrit-to-eiffel-event

This service is part of the [Eiffel Community](https://eiffel-community.github.io/).

Read more about the Eiffel protocol on https://github.com/eiffel-community/eiffel.

## Usage 

#### Prepare
Download and cd repo `git clone https://github.com/chrillebile/gerrit-rabbitmq-eiffel-service.git && cd gerrit-rabbitmq-eiffel-service`

Set config in `src/main/resources/config.properties` 

#### Build
docker-compose build

#### Run
docker-compose up -d

## Maintainers
 * Christian Bilevits
    - <christian.bilevits@axis.com>

## License
[Apache-2.0](LICENSE)

