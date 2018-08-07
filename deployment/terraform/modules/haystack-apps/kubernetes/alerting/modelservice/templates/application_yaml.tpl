spring:
  main:
    web-environment: true
    show-banner: true
  datasource:
    driver-class-name: com.mysql.jdbc.Driver
    url: "${db_endpoint}"
    username: root
    password:
    maximum-pool-size: 8
    minimum-idle: 0
    # TODO Add other data source settings here
  jpa:
    openInView: true
    database: MYSQL
    show-sql: false
    generate-ddl: false
    hibernate:
      naming_strategy: org.hibernate.cfg.ImprovedNamingStrategy

endpoints:
  health:
    sensitive: false

management:
  context-path: "/admin"

server:
  port: 8080