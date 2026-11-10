package com.project.testApp.entity;

/**
 * Master и Student выбираются при регистрации, остальные роли назначаются только вручную.
 */
public enum Roles {
    Develop_admin,
    Admin,
    User,
    Trial_user,
    Master,
    Student;
}
