alter table if exists image drop constraint if exists fk_image_telemetry_id;

drop table if exists camera_gimbal_settings cascade;

drop table if exists image cascade;

drop table if exists telemetry cascade;

