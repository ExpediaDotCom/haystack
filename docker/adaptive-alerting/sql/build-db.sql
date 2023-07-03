DROP DATABASE IF EXISTS aa_model_service;

CREATE DATABASE aa_model_service;

USE aa_model_service;

create table metric (
  id           int unsigned primary key not null auto_increment,
  ukey         varchar(255) unique      not null,
  hash         char(36) unique          not null,
  description  varchar(255),
  tags         json,
  date_created timestamp                         default CURRENT_TIMESTAMP
);

create table model_type (
  id           smallint unsigned primary key not null auto_increment,
  ukey         varchar(100) unique           not null,
  date_created timestamp                              default CURRENT_TIMESTAMP
);

CREATE TABLE detector (
  id            int unsigned primary key NOT NULL AUTO_INCREMENT,
  uuid          char(36) unique          not null,
  model_type_id smallint unsigned        not null,
  hyperparams   json,
  training_meta json,
  seyren_flag   boolean                           default false,
  date_created  timestamp                NULL     DEFAULT CURRENT_TIMESTAMP,
  created_by    varchar(100),
  constraint model_type_id_fk foreign key (model_type_id) references model_type (id)
);

create table model (
  id            int unsigned primary key not null auto_increment,
  params        json,
  detector_id   int unsigned             not null,
  weak_sigmas       decimal(3, 3),
  strong_sigmas     decimal(3, 3),
  other_stuff   json,
  date_created  timestamp                         default CURRENT_TIMESTAMP,
  constraint detector_id_fk foreign key (detector_id) references detector (id)
);

create table metric_detector_mapping (
  id           int unsigned primary key not null auto_increment,
  metric_id    int unsigned             not null,
  detector_id  int unsigned             not null,
  date_created timestamp                         default CURRENT_TIMESTAMP,
  constraint metric_id_fk foreign key (metric_id) references metric (id),
  constraint detector_id_mapping_fk foreign key (detector_id) references detector (id),
  unique index (metric_id, detector_id)
);

create table user (
  id           int unsigned primary key not null auto_increment,
  username     varchar(100) unique not null,
  password     varchar(100) not null,
  role         varchar(100),
  enabled      boolean
);

create table oauth_client_details (
  client_id VARCHAR(256) PRIMARY KEY,
  resource_ids VARCHAR(256),
  client_secret VARCHAR(256),
  scope VARCHAR(256),
  authorized_grant_types VARCHAR(256),
  web_server_redirect_uri VARCHAR(256),
  authorities VARCHAR(256),
  access_token_validity INTEGER,
  refresh_token_validity INTEGER,
  additional_information VARCHAR(4096),
  autoapprove VARCHAR(256)
);
