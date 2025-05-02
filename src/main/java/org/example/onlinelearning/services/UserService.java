package org.example.onlinelearning.services;

import lombok.RequiredArgsConstructor;
import org.example.onlinelearning.config.PasswordUtil;
import org.example.onlinelearning.dtos.UpdateUserDTO;
import org.example.onlinelearning.dtos.UserDTO;
import org.example.onlinelearning.exceptions.NotFoundException;
import org.example.onlinelearning.exceptions.SecurityException;
import org.example.onlinelearning.mappers.UserMapper;
import org.example.onlinelearning.models.User;
import org.example.onlinelearning.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private static final String USER_NOT_FOUND = "User not found with %s: %s";

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Autowired
    public UserService(
            UserRepository userRepository,
            UserMapper userMapper
    ) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Transactional(readOnly = true)
    public List<UserDTO> findAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toUserDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserDTO createUser(UserDTO userDTO) {
        User user = userMapper.toUser(userDTO);

        try {
            String salt = PasswordUtil.generateSalt();
            String hashedPassword = PasswordUtil.hashPassword(userDTO.getPassword(), salt);
            user.setPasswordSalt(salt);
            user.setPassword(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException("Password encryption failed", e);
        }

        User savedUser = userRepository.save(user);
        return userMapper.toUserDTO(savedUser);
    }

    @Transactional(readOnly = true)
    public UserDTO findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(userMapper::toUserDTO)
                .orElseThrow(() -> new NotFoundException(
                        USER_NOT_FOUND.formatted("username", username)
                ));
    }

    @Transactional(readOnly = true)
    public UserDTO findUserById(Long userId) {
        return userRepository.findById(userId)
                .map(userMapper::toUserDTO)
                .orElseThrow(() -> new NotFoundException(
                        USER_NOT_FOUND.formatted("id", userId)
                ));
    }

    @Transactional
    public UserDTO updateUser(UpdateUserDTO dto) throws NoSuchAlgorithmException {
        User user = userRepository.findById(dto.getId())
                .orElseThrow(() -> new NotFoundException("User not found by id " + dto.getId()));

        // Проверяем текущий пароль
        String hashedCurrent = PasswordUtil.hashPassword(dto.getPassword(), user.getPasswordSalt());
        if (!hashedCurrent.equals(user.getPassword())) {
            throw new SecurityException("Неверный текущий пароль");
        }

        // Проверяем изменение username
        if (!user.getUsername().equals(dto.getUsername())) {
            if (userRepository.existsByUsername(dto.getUsername())) {
                throw new SecurityException("Username уже занят");
            }
            user.setUsername(dto.getUsername());
        }

        // Обновляем fullname
        user.setFullname(dto.getFullname());

        // Обработка смены пароля
        if (dto.getNewPassword() != null && !dto.getNewPassword().isEmpty()) {
            String newSalt = PasswordUtil.generateSalt();
            String newHashed = PasswordUtil.hashPassword(dto.getNewPassword(), newSalt);
            user.setPasswordSalt(newSalt);
            user.setPassword(newHashed);
        }

        User saved = userRepository.save(user);
        return userMapper.toUserDTO(saved);
    }

    @Transactional
    public void deleteUserById(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(USER_NOT_FOUND.formatted("id", userId));
        }
        userRepository.deleteById(userId);
    }

    private void updateUserFields(User user, UserDTO dto) {
        if (StringUtils.hasText(dto.getFullname())) {
            user.setFullname(dto.getFullname());
        }
        if (StringUtils.hasText(dto.getUsername())) {
            user.setUsername(dto.getUsername());
        }
    }

    private void handlePasswordUpdate(User user, String newPassword) {
        if (StringUtils.hasText(newPassword)) {
            try {
                String newSalt = PasswordUtil.generateSalt();
                String newHashedPassword = PasswordUtil.hashPassword(newPassword, newSalt);
                user.setPasswordSalt(newSalt);
                user.setPassword(newHashedPassword);
            } catch (NoSuchAlgorithmException e) {
                throw new SecurityException("Password encryption failed", e);
            }
        }
    }

    public UserDTO authenticateUser(String username, String password) {
        validatePassword(password);

        UserDTO user = findUserByUsername(username);

        try {
            String hashedPassword = PasswordUtil.hashPassword(
                    password,
                    user.getPasswordSalt()
            );

            if (!hashedPassword.equals(user.getPassword())) {
                throw new SecurityException("Invalid credentials");
            }

            return user;
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException("Password verification failed", e);
        }
    }

    public UserDTO registerUser(UserDTO userDTO) {
        if (userExistsByUsername(userDTO.getUsername())) {
            throw new SecurityException("Username already exists"); // Используем новый конструктор
        }
        return createUser(userDTO);
    }

    private boolean userExistsByUsername(String username) {
        try {
            findUserByUsername(username);
            return true;
        } catch (NotFoundException e) {
            return false;
        }
    }

    private void validatePassword(String password) {
        if (!StringUtils.hasText(password)) {
            throw new SecurityException("Password cannot be empty");
        }
    }

    public User getUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }
}