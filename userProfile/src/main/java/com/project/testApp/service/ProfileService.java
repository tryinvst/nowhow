package com.project.testApp.service;

import com.project.common.ImageFormat;
import com.project.testApp.dto.request.ProfileUpdateRequestDto;
import com.project.testApp.dto.response.MasterPublicDto;
import com.project.testApp.dto.response.PersonResponseDto;
import com.project.testApp.entity.Person;
import com.project.testApp.entity.Roles;
import com.project.testApp.exeption.handler.UserNotFoundException;
import com.project.testApp.repository.PersonRepository;
import com.project.testApp.storage.AvatarStorage;
import jakarta.transaction.Transactional;
import org.springframework.core.io.Resource;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Публичный профиль мастера и аватар.
 */
@Service
public class ProfileService {
    private static final int MAX_BATCH = 100;

    private final PersonRepository personRepository;
    private final PersonMapper personMapper;
    private final AvatarStorage avatarStorage;

    public ProfileService(PersonRepository personRepository, PersonMapper personMapper, AvatarStorage avatarStorage) {
        this.personRepository = personRepository;
        this.personMapper = personMapper;
        this.avatarStorage = avatarStorage;
    }

    @Transactional
    public PersonResponseDto updateProfile(ProfileUpdateRequestDto requestDto, Jwt jwt) {
        Person person = currentPerson(jwt);
        if (person.getPersonRoles() == Roles.Master && requestDto.specialization() == null) {
            throw new IllegalArgumentException("specialization: мастеру нужно указать специализацию");
        }
        person.setDisplayName(requestDto.displayName().trim());
        person.setSpecialization(person.getPersonRoles() == Roles.Master ? requestDto.specialization() : null);
        person.setAbout(requestDto.about());
        person.setExperienceYears(requestDto.experienceYears());
        return personMapper.toResponseDto(personRepository.save(person));
    }

    @Transactional
    public PersonResponseDto updateAvatar(MultipartFile file, Jwt jwt) {
        Person person = currentPerson(jwt);
        String oldAvatar = person.getAvatarStorageFilename();
        person.setAvatarStorageFilename(avatarStorage.save(file));
        Person saved = personRepository.save(person);
        avatarStorage.delete(oldAvatar);
        return personMapper.toResponseDto(saved);
    }

    @Transactional
    public PersonResponseDto deleteAvatar(Jwt jwt) {
        Person person = currentPerson(jwt);
        avatarStorage.delete(person.getAvatarStorageFilename());
        person.setAvatarStorageFilename(null);
        return personMapper.toResponseDto(personRepository.save(person));
    }

    public MasterPublicDto findMaster(Long id) {
        return personMapper.toMasterPublicDto(findMasterEntity(id));
    }

    /**
     * Для карточек каталога: неизвестные id и не-мастера молча пропускаются.
     */
    public List<MasterPublicDto> findMasters(List<Long> ids) {
        if (ids.size() > MAX_BATCH) {
            throw new IllegalArgumentException("Не больше " + MAX_BATCH + " id за запрос");
        }
        return personRepository.findAllById(ids).stream()
                .filter(person -> person.getPersonRoles() == Roles.Master)
                .map(personMapper::toMasterPublicDto)
                .toList();
    }

    /**
     * Имя файла из URL сверяем с текущим, а не используем как путь.
     */
    public Avatar loadAvatar(Long masterId, String filename) {
        Person person = findMasterEntity(masterId);
        if (person.getAvatarStorageFilename() == null || !person.getAvatarStorageFilename().equals(filename)) {
            throw new UserNotFoundException("Аватар не найден");
        }
        return new Avatar(avatarStorage.load(filename), ImageFormat.fromFilename(filename));
    }

    private Person findMasterEntity(Long id) {
        return personRepository.findById(id)
                .filter(person -> person.getPersonRoles() == Roles.Master)
                .orElseThrow(() -> new UserNotFoundException("Мастер с id " + id + " не найден"));
    }

    private Person currentPerson(Jwt jwt) {
        Long id = Long.valueOf(jwt.getSubject());
        return personRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с id " + id + " не найден"));
    }

    public record Avatar(Resource resource, ImageFormat format) { }
}
