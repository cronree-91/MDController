package jp.cron.mdcontroller.api.data.entity;

import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.List;

public class UserEntity {
    @Id
    public Long id;
    public String name;
    public List<Permission> permissions = new ArrayList<>();
    public int server_limit = 1;

    public enum Permission {
        CREATE_SERVER,
        CREATE_USER,
        START_SERVER,
        STOP_SERVER,
        SHOW_SERVER_LIST
    }
}
