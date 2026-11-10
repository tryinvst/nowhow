package com.project.testApp.service;

import com.project.testApp.dto.request.LoginRequestDto;
import com.project.testApp.dto.request.PersonRequestDto;
import com.project.testApp.dto.response.PersonResponseDto;
import com.project.testApp.dto.response.TokenResponseDto;
import com.project.testApp.dto.response.UserShortResponseDto;
import com.project.testApp.entity.Person;
import com.project.testApp.entity.Roles;
import com.project.testApp.exeption.handler.UserAlreadyExistsException;
import com.project.testApp.exeption.handler.UserNotFoundException;
import com.project.testApp.repository.PersonRepository;
import com.project.testApp.storage.AvatarStorage;
import com.project.testApp.util.UserValidatiorUtil;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PersonalService {

   private static final String PASSWORD_RULES = "Пароль: от 8 символов, минимум 1 заглавная буква и 1 спецсимвол";

   private PersonRepository personRepository;
   private UserValidatiorUtil userValidatiorUtil;
   private PasswordEncoder passwordEncoder;
   private TokenService tokenService;
   private PersonMapper personMapper;
   private AvatarStorage avatarStorage;

    public PersonalService(PersonRepository personRepository ,UserValidatiorUtil userValidatiorUtil,
                           PasswordEncoder passwordEncoder, TokenService tokenService, PersonMapper personMapper,
                           AvatarStorage avatarStorage) {
        this.personRepository = personRepository;
        this.userValidatiorUtil = userValidatiorUtil;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.personMapper = personMapper;
        this.avatarStorage = avatarStorage;
    }

    @Transactional
    public PersonResponseDto create(PersonRequestDto requestDto) {
        if (requestDto.role() == null) {
            throw new IllegalArgumentException("role: укажите тип аккаунта - master или student");
        }
        if (!userValidatiorUtil.password(requestDto.password())) {
            throw new IllegalArgumentException(PASSWORD_RULES);
        }
        if (personRepository.existsByUserName ( requestDto.username () )) {
            throw new UserAlreadyExistsException ( "Username is already exists" );
        }
        if (personRepository.existsByEmail ( requestDto.email () )) {
            throw new UserAlreadyExistsException ( "Email is already exists" );
        }
        Person person = new Person ();
        person.setUserName(requestDto.username());
        person.setPassword(passwordEncoder.encode(requestDto.password()));
        person.setPersonRoles ( "master".equals(requestDto.role()) ? Roles.Master : Roles.Student );
        person.setPhoneNumber ( requestDto.phonenumber () );
        person.setEmail ( requestDto.email () );
        Person savePerson = personRepository.save( person );
        return toResponseDto(savePerson);
    }

    /**
     * Одинаковая ошибка для "нет пользователя" и "не тот пароль", чтобы не подсказывать, какие username заняты.
     */
    public TokenResponseDto login(LoginRequestDto requestDto) {
        Person person = personRepository.findByUserName(requestDto.username());
        if (person == null || !passwordEncoder.matches(requestDto.password(), person.getPassword())) {
            throw new BadCredentialsException("Неверный username или пароль");
        }
        return tokenService.issueTokens(person);
    }

    public PersonResponseDto findCurrentPerson(Jwt jwt) {
        return toResponseDto(findById(currentUserId(jwt)));
    }

    public PersonResponseDto findPersonByUsername(String username, Jwt jwt) {
       Person findThatPerson = personRepository.findByUserName (username);
       if (findThatPerson == null) {
           throw new UserNotFoundException("Пользователь с username " + username + " не найден");
       }
       checkSelfOrAdmin(findThatPerson.getId(), jwt);
       return toResponseDto(findThatPerson);
    }

    public UserShortResponseDto findPersonById(Long id) {
        Person findThatPerson = findById(id);
        return new UserShortResponseDto(findThatPerson.getId(), findThatPerson.getUserName());
    }

    public void putPerson (PersonRequestDto user, Jwt jwt) {
        Person existingUser = personRepository.findByUserName (user.username());
        if (existingUser == null) {
            throw new UserNotFoundException("Невозможно отредактировать пользователя, такого username не существует");
        }
        checkSelfOrAdmin(existingUser.getId(), jwt);
        Boolean isPasswordValid = userValidatiorUtil.password(user.password());
        if (!isPasswordValid) {
            System.out.println("Пользователь не отредактирован");
            throw new IllegalArgumentException(PASSWORD_RULES);
        }
        existingUser.setEmail(user.email());
        existingUser.setPassword(passwordEncoder.encode(user.password()));
        existingUser.setPhoneNumber(user.phonenumber());
        personRepository.save(existingUser);
        System.out.println("Пользователь успешно отредактирован");
    }

    public void deletePerson(Long id, Jwt jwt) {
        checkSelfOrAdmin(id, jwt);
        Person person = findById(id);
        personRepository.delete(person);
        avatarStorage.delete(person.getAvatarStorageFilename());
    }

    public List<PersonResponseDto> findAllPersons(){
        return personRepository.findAll().stream().map(this::toResponseDto).toList();
    }

    private Person findById(Long id) {
        return personRepository.findById(id).orElseThrow(()
         -> new UserNotFoundException("Пользователь с id " + id + " не найден"));
    }

    private Long currentUserId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }

    /**
     * Менять и смотреть чужие данные может только администратор.
     */
    private void checkSelfOrAdmin(Long targetUserId, Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        boolean isAdmin = roles != null
                && (roles.contains(Roles.Admin.name()) || roles.contains(Roles.Develop_admin.name()));
        if (!isAdmin && !targetUserId.equals(currentUserId(jwt))) {
            throw new AccessDeniedException("Нет доступа к данным другого пользователя");
        }
    }

    private PersonResponseDto toResponseDto(Person person) {
        return personMapper.toResponseDto(person);
    }
}
