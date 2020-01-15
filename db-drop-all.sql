alter table if exists alphanum_target drop constraint if exists fk_alphanum_target_geotag_id;

alter table if exists alphanum_target_sighting drop constraint if exists fk_alphanum_target_sighting_assignment_id;
drop index if exists ix_alphanum_target_sighting_assignment_id;

alter table if exists alphanum_target_sighting drop constraint if exists fk_alphanum_target_sighting_geotag_id;

alter table if exists alphanum_target_sighting drop constraint if exists fk_alphanum_target_sighting_target_id;
drop index if exists ix_alphanum_target_sighting_target_id;

alter table if exists assignment drop constraint if exists fk_assignment_image_id;
drop index if exists ix_assignment_image_id;

alter table if exists emergent_target drop constraint if exists fk_emergent_target_geotag_id;

alter table if exists emergent_target_sighting drop constraint if exists fk_emergent_target_sighting_assignment_id;
drop index if exists ix_emergent_target_sighting_assignment_id;

alter table if exists emergent_target_sighting drop constraint if exists fk_emergent_target_sighting_geotag_id;

alter table if exists emergent_target_sighting drop constraint if exists fk_emergent_target_sighting_target_id;
drop index if exists ix_emergent_target_sighting_target_id;

alter table if exists image drop constraint if exists fk_image_telemetry_id;

alter table if exists mgtimage drop constraint if exists fk_mgtimage_image_id;

drop table if exists alphanum_target cascade;

drop table if exists alphanum_target_sighting cascade;

drop table if exists assignment cascade;

drop table if exists auth_token cascade;

drop table if exists camera_gimbal_settings cascade;

drop table if exists emergent_target cascade;

drop table if exists emergent_target_sighting cascade;

drop table if exists geotag cascade;

drop table if exists image cascade;

drop table if exists mgtimage cascade;

drop table if exists telemetry cascade;
