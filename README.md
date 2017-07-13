# Elk from Scratch

#### Step 1

Head to the [PCF Dev](https://pivotal.io/pcf-dev) website and folllow the instructions to download and install.

#### Step 2

Start pcf dev:

```sh
cf dev start
```

#### Step 3

Push an app to Cloud Foundry. Username: user Password: pass

```sh
cf login -a https://api.local.pcfdev.io --skip-ssl-validation
./gradlew build
cf push ok -p build/libs/elk-from-scratch-0.0.1-SNAPSHOT.jar
```

#### Step 4

Check out the logs in:

https://uaa.local.pcfdev.io/login

#### Step 5

Check out the overview of the CF loggregator system:

https://docs.cloudfoundry.org/loggregator/architecture.html

#### Step 6

We know that we need to use consume the firehose. One tool available is:
https://github.com/cloudfoundry-community/firehose-to-syslog

Download and install firehose-to-syslog

#### Step 7

Connect the firehose to syslog app.

```sh
uaac target https://uaa.local.pcfdev.io --skip-ssl-validation
uaac client add firehose-to-syslog --name firehose-to-syslog --secret admin-client-secret --authorized_grant_types client_credentials,refresh_token  --authorities doppler.firehose,cloud_controller.admin
./firehose-to-syslog_darwin_amd64  --api-endpoint="https://api.local.pcfdev.io" --client-secret admin-client-secret --client-id firehose-to-syslog  --skip-ssl-validation --debug  --events="LogMessage,ContainerMetric"
```

#### Step 8
What is elk:
![](http://blog.arungupta.me/wp-content/uploads/2015/07/elk-stack.png)


Start up elk
```sh
docker run -p 5601:5601 -p 9200:9200 -it --name elk sebp/elk:es241_l240_k461
```


#### Step 9

Setup a Logstash Config.
Create a logstash configfile:

```conf
input {
  tcp {
    port => 5044
    type => syslog
  }
}

output {
  elasticsearch {
    hosts => ["localhost"]
  }
}
```

And then startup elk with that config:

```sh
docker run -p 5601:5601 -p 9200:9200 -p 5044:5044 -it --name elk -v `pwd`:/etc/logstash/conf.d/ sebp/elk:es241_l240_k461
```

#### Step 10

Lets parse the syslog message. Add the following to the syslog config:

```conf
filter {
  grok {
    match => { "message" => "%{SYSLOGBASE2} %{GREEDYDATA:firehose}" }
  }
}
```

And then startup elk with that config:

```sh
docker run -p 5601:5601 -p 9200:9200 -p 5044:5044 -it --name elk -v `pwd`:/etc/logstash/conf.d/ sebp/elk:es241_l240_k461
```

#### Step 11

Lets parse the json message. Add the following to the config:

```conf
filter {
    json {
      source => "firehose"
    }
}
```

And then startup elk with that config:

```sh
docker run -p 5601:5601 -p 9200:9200 -p 5044:5044 -it --name elk -v `pwd`:/etc/logstash/conf.d/ sebp/elk:es241_l240_k461
```

#### Step 12

Create a logback-spring.xml configuration to convert newlines to invisible line seperators
```xml
<configuration debug="false">
    <property name="LOG_EXCEPTION_CONVERSION_WORD"
              value="%replace(%replace(%wEx){'.java','.jaba'}){'\n(?=.+)',' '}%nopex"/>
    <include resource="org/springframework/boot/logging/logback/base.xml"/>
</configuration>
```

Push that app to CF.

```sh
./gradlew build
cf push ok -p build/libs/elk-from-scratch-0.0.1-SNAPSHOT.jar
```


#### Step 13

Lets split the line seperators to a new lines:

```conf
filter {
    mutate {
      gsub => [ "msg", '\u2028', "
"]
}
```

And then startup elk with that config:

```sh
docker run -p 5601:5601 -p 9200:9200 -p 5044:5044 -it --name elk -v `pwd`:/etc/logstash/conf.d/ sebp/elk:es241_l240_k461
```

#### Step 14
Hooray!
![](https://media.giphy.com/media/3oEjI2Cb8lhLNKzGyQ/giphy.gif)


