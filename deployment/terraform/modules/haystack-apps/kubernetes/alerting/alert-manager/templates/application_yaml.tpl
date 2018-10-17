spring:
  mail:
    host: "${smtp_host}"
    port: 25
    fromAddress: "${mail_from}"
  datasource:
    driver-class-name: "com.mysql.jdbc.Driver"
    url: "${db_endpoint}"
    username: $${DB_USERNAME}
    password: $${DB_PASSWORD}
    maximum-pool-size: 8
    minimum-idle: 0
    # TODO Add other data source settings here
  jpa:
    database-platform: org.hibernate.dialect.MySQLDialect

