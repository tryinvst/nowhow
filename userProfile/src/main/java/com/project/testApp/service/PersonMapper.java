package com.project.testApp.service;

import com.project.testApp.dto.response.MasterPublicDto;
import com.project.testApp.dto.response.PersonResponseDto;
import com.project.testApp.entity.Person;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Сборка ответов из Person: абсолютные ссылки на аватар строятся от app.public-url.
 */
@Component
public class PersonMapper {

    private final String publicUrl;

    public PersonMapper(@Value("${app.public-url}") String publicUrl) {
        this.publicUrl = publicUrl;
    }

    public PersonResponseDto toResponseDto(Person person) {
        return new PersonResponseDto(person.getId(),
         person.getPhoneNumber(),
         person.getUserName(),
         person.getEmail(),
         person.getPersonRoles().name(),
         person.getDisplayName(),
         person.getSpecialization() == null ? null : person.getSpecialization().name(),
         person.getAbout(),
         person.getExperienceYears(),
         avatarUrl(person));
    }

    public MasterPublicDto toMasterPublicDto(Person person) {
        return new MasterPublicDto(person.getId(),
                person.getDisplayName(),
                person.getSpecialization() == null ? null : person.getSpecialization().name(),
                person.getSpecialization() == null ? null : person.getSpecialization().getTitle(),
                person.getAbout(),
                person.getExperienceYears(),
                avatarUrl(person));
    }

    /**
     * В ссылке имя файла: при смене аватара меняется URL, и браузерный кэш не мешает.
     */
    private String avatarUrl(Person person) {
        if (person.getAvatarStorageFilename() == null) {
            return null;
        }
        return publicUrl + "/masters/" + person.getId() + "/avatar/" + person.getAvatarStorageFilename();
    }
}
