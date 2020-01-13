create table assignment (
  id                            bigserial not null,
  timestamp                     timestamptz,
  image_id                      bigint,
  assignee                      varchar(4) not null,
  done                          boolean default false not null,
  username                      varchar(255),
  constraint ck_assignment_assignee check ( assignee in ('mdlc','adlc')),
  constraint pk_assignment primary key (id)
);

create table auth_token (
  id                            bigserial not null,
  token                         varchar(255) not null,
  username                      varchar(255) not null,
  admin                         boolean default false not null,
  constraint uq_auth_token_token unique (token),
  constraint uq_auth_token_username unique (username),
  constraint pk_auth_token primary key (id)
);

create table camera_gimbal_settings (
  id                            bigserial not null,
  timestamp                     timestamptz,
  mode                          integer not null,
  constraint ck_camera_gimbal_settings_mode check ( mode in (0,1,2)),
  constraint pk_camera_gimbal_settings primary key (id)
);

create table image (
  id                            bigserial not null,
  timestamp                     timestamptz,
  image_url                     varchar(255),
  telemetry_id                  bigint,
  img_mode                      integer not null,
  constraint ck_image_img_mode check ( img_mode in (0,1,2)),
  constraint uq_image_telemetry_id unique (telemetry_id),
  constraint pk_image primary key (id)
);

create table telemetry (
  id                            bigserial not null,
  altitude                      float,
  plane_yaw                     float,
  constraint pk_telemetry primary key (id)
);

create index ix_assignment_image_id on assignment (image_id);
alter table assignment add constraint fk_assignment_image_id foreign key (image_id) references image (id) on delete restrict on update restrict;

alter table image add constraint fk_image_telemetry_id foreign key (telemetry_id) references telemetry (id) on delete restrict on update restrict;
