alter table if exists assignment drop constraint if exists fk_assignment_image_id;
drop index if exists ix_assignment_image_id;

alter table if exists image drop constraint if exists fk_image_telemetry_id;

drop table if exists assignment cascade;

drop table if exists auth_token cascade;

drop table if exists camera_gimbal_settings cascade;

drop table if exists geotag cascade;

drop table if exists image cascade;

drop table if exists telemetry cascade;
