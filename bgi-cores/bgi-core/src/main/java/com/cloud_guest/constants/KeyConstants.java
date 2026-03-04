package com.cloud_guest.constants;

/**
 * @Author yan
 * @Date 2026/2/24 21:16:00
 * @Description
 */
public interface KeyConstants {
    String load_yml_key = "load_yml:ALL";
    String load_yml_save_key = "load_yml:save:ALL";
    String load_yml_save_update_time_key = "load_yml:save_update_time:ALL";

    String load_yml_write_key = "load_yml:write:ALL";
    String auto_plan_key = "AUTO_PLAN:UID:";
    String auto_plan_key_uid_all = "AUTO_PLAN:UID:ALL";
    String auto_plan_key_domain_all = "AUTO_PLAN_DOMAIN:ALL";
    String auto_plan_key_country_all = "AUTO_PLAN_COUNTRY:ALL";
    String all_application_key = "ALL:application";
    String all_application_datacenter_key = "ALL:DATACENTER:application";
    String restart_key = "restart";
    String online_application_key = "online_application";
    String outline_application_key = "outline_application";


    String redis_file_json_key = "redis:file:json:";
    String task_key = "task:";
    String lock_key = "lock:";
    String local_lock_key = lock_key + "local:";
    String redis_lock_key = lock_key + "redis:";
}
