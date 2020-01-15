create table alphanum_target (
  id                            bigserial not null,
  creator                       varchar(4),
  geotag_id                     bigint,
  judge_target_id               bigint,
  thumbnail_tsid                bigint,
  shape                         integer,
  shape_color                   integer,
  alpha                         varchar(255),
  alpha_color                   integer,
  offaxis                       boolean,
  constraint ck_alphanum_target_creator check ( creator in ('mdlc','adlc')),
  constraint ck_alphanum_target_shape check ( shape in (0,1,2,3,4,5,6,7,8,9,10,11,12)),
  constraint ck_alphanum_target_shape_color check ( shape_color in (0,1,2,3,4,5,6,7,8,9)),
  constraint ck_alphanum_target_alpha_color check ( alpha_color in (0,1,2,3,4,5,6,7,8,9)),
  constraint uq_alphanum_target_geotag_id unique (geotag_id),
  constraint pk_alphanum_target primary key (id)
);

create table alphanum_target_sighting (
  id                            bigserial not null,
  creator                       varchar(4),
  assignment_id                 bigint,
  geotag_id                     bigint,
  pixel_x                       integer,
  pixel_y                       integer,
  width                         integer,
  height                        integer,
  mdlc_class_conf               varchar(6),
  radians_from_top              float,
  orientation_confidence        float,
  target_id                     bigint,
  shape                         integer,
  shape_confidence              float,
  shape_color                   integer,
  shape_color_confidence        float,
  alpha                         varchar(255),
  alpha_confidence              float,
  alpha_color                   integer,
  alpha_color_confidence        float,
  adlc_class_conf               float,
  offaxis                       boolean,
  constraint ck_alphanum_target_sighting_creator check ( creator in ('mdlc','adlc')),
  constraint ck_alphanum_target_sighting_mdlc_class_conf check ( mdlc_class_conf in ('high','medium','low')),
  constraint ck_alphanum_target_sighting_shape check ( shape in (0,1,2,3,4,5,6,7,8,9,10,11,12)),
  constraint ck_alphanum_target_sighting_shape_color check ( shape_color in (0,1,2,3,4,5,6,7,8,9)),
  constraint ck_alphanum_target_sighting_alpha_color check ( alpha_color in (0,1,2,3,4,5,6,7,8,9)),
  constraint uq_alphanum_target_sighting_geotag_id unique (geotag_id),
  constraint pk_alphanum_target_sighting primary key (id)
);

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

create table emergent_target (
  id                            bigserial not null,
  creator                       varchar(4),
  geotag_id                     bigint,
  judge_target_id               bigint,
  thumbnail_tsid                bigint,
  description                   varchar(255),
  constraint ck_emergent_target_creator check ( creator in ('mdlc','adlc')),
  constraint uq_emergent_target_geotag_id unique (geotag_id),
  constraint pk_emergent_target primary key (id)
);

create table emergent_target_sighting (
  id                            bigserial not null,
  creator                       varchar(4),
  assignment_id                 bigint,
  geotag_id                     bigint,
  pixel_x                       integer,
  pixel_y                       integer,
  width                         integer,
  height                        integer,
  mdlc_class_conf               varchar(6),
  radians_from_top              float,
  orientation_confidence        float,
  target_id                     bigint,
  description                   varchar(255),
  constraint ck_emergent_target_sighting_creator check ( creator in ('mdlc','adlc')),
  constraint ck_emergent_target_sighting_mdlc_class_conf check ( mdlc_class_conf in ('high','medium','low')),
  constraint uq_emergent_target_sighting_geotag_id unique (geotag_id),
  constraint pk_emergent_target_sighting primary key (id)
);

create table geotag (
  id                            bigserial not null,
  latitude                      float,
  longitude                     float,
  radians_from_north            float,
  is_manual_geotag              boolean default false not null,
  constraint pk_geotag primary key (id)
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

create table mgtimage (
  id                            bigserial not null,
  image_id                      bigint,
  is_sent                       boolean,
  has_telemetry                 boolean,
  has_target                    boolean,
  constraint uq_mgtimage_image_id unique (image_id),
  constraint pk_mgtimage primary key (id)
);

create table telemetry (
  id                            bigserial not null,
  altitude                      float,
  plane_yaw                     float,
  constraint pk_telemetry primary key (id)
);

alter table alphanum_target add constraint fk_alphanum_target_geotag_id foreign key (geotag_id) references geotag (id) on delete restrict on update restrict;

create index ix_alphanum_target_sighting_assignment_id on alphanum_target_sighting (assignment_id);
alter table alphanum_target_sighting add constraint fk_alphanum_target_sighting_assignment_id foreign key (assignment_id) references assignment (id) on delete restrict on update restrict;

alter table alphanum_target_sighting add constraint fk_alphanum_target_sighting_geotag_id foreign key (geotag_id) references geotag (id) on delete restrict on update restrict;

create index ix_alphanum_target_sighting_target_id on alphanum_target_sighting (target_id);
alter table alphanum_target_sighting add constraint fk_alphanum_target_sighting_target_id foreign key (target_id) references alphanum_target (id) on delete restrict on update restrict;

create index ix_assignment_image_id on assignment (image_id);
alter table assignment add constraint fk_assignment_image_id foreign key (image_id) references image (id) on delete restrict on update restrict;

alter table emergent_target add constraint fk_emergent_target_geotag_id foreign key (geotag_id) references geotag (id) on delete restrict on update restrict;

create index ix_emergent_target_sighting_assignment_id on emergent_target_sighting (assignment_id);
alter table emergent_target_sighting add constraint fk_emergent_target_sighting_assignment_id foreign key (assignment_id) references assignment (id) on delete restrict on update restrict;

alter table emergent_target_sighting add constraint fk_emergent_target_sighting_geotag_id foreign key (geotag_id) references geotag (id) on delete restrict on update restrict;

create index ix_emergent_target_sighting_target_id on emergent_target_sighting (target_id);
alter table emergent_target_sighting add constraint fk_emergent_target_sighting_target_id foreign key (target_id) references emergent_target (id) on delete restrict on update restrict;

alter table image add constraint fk_image_telemetry_id foreign key (telemetry_id) references telemetry (id) on delete restrict on update restrict;

alter table mgtimage add constraint fk_mgtimage_image_id foreign key (image_id) references image (id) on delete restrict on update restrict;
