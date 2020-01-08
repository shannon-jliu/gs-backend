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
  image_url                     varchar(255) not null,
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

alter table image add constraint fk_image_telemetry_id foreign key (telemetry_id) references telemetry (id) on delete restrict on update restrict;

