package jp.test.servercreate.api.data.entity;

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
        START_SERVER,
        STOP_SERVER,
        SHOW_SERVER_LIST,
        DELETE_SERVER,
        CREATE_USER,
        UPDATE_USER,
        DELETE_USER,
        SET_USER_PERMISSION,
        SHOW_USER_LIST
    }
}
