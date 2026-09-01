package com.cloud_guest.constants;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    String ws_proxy_access_key = "WS_PROXY_ACCESS:UID:";
    String bettergi_scripts_list = "BETTERGI_SCRIPTS_LIST:";
    String mapping_uid_key = "MAPPING:UID:";
    String mapping_uid_team_key = "MAPPING:UID:TEAM:";
    String auto_plan_key = "AUTO_PLAN:UID:";
    String auto_plan_global_key = "AUTO_PLAN:UID:GLOBAL:";
    String auto_plan_key_uid_all = "AUTO_PLAN:UID:ALL";
    String auto_plan_key_boss_all = "AUTO_PLAN_BOSS:ALL";
    String auto_plan_key_domain_all = "AUTO_PLAN_DOMAIN:ALL";
    String auto_plan_key_country_all = "AUTO_PLAN_COUNTRY:ALL";
    String all_application_key = "ALL:application";
    String all_application_datacenter_key = "ALL:DATACENTER:application";
    String restart_key = "restart";
    String online_application_key = "online_application";
    String outline_application_key = "outline_application";
    String key = "TEMP:KEY:";
    String db_kv_key = "db:kv:";
    String encrypt_salt = "sign.encrypt.salt";
    List<String> ex_backup_list = new ArrayList<>(Arrays.asList(restart_key, online_application_key, outline_application_key));
    String cache_key="cache:";
    String redis_file_json_key = "redis:file:json:";
    String task_key = "task:";
    String lock_key = "lock:";
    String local_lock_key = lock_key + "local:";
    String redis_lock_key = lock_key + "redis:";
}
