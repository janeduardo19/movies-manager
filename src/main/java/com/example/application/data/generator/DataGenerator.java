package com.example.application.data.generator;

import com.example.application.data.Role;
import com.example.application.data.entity.Movie;
import com.example.application.data.entity.Room;
import com.example.application.data.entity.Session;
import com.example.application.data.entity.User;
import com.example.application.data.service.MovieRepository;
import com.example.application.data.service.RoomRepository;
import com.example.application.data.service.SessionRepository;
import com.example.application.data.service.UserRepository;
import com.vaadin.exampledata.DataType;
import com.vaadin.exampledata.ExampleDataGenerator;
import com.vaadin.flow.spring.annotation.SpringComponent;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringComponent
public class DataGenerator {

    @Bean
    public CommandLineRunner loadData(PasswordEncoder passwordEncoder, UserRepository userRepository,
            RoomRepository roomRepository, SessionRepository sessionRepository, MovieRepository movieRepository) {
        return args -> {
            Logger logger = LoggerFactory.getLogger(getClass());
            if (userRepository.count() != 0L) {
                logger.info("Using existing database");
                return;
            }
            int seed = 123;

            logger.info("Generating demo data");

            logger.info("... generating 2 User entities...");
            User user = new User();
            user.setName("John Normal");
            user.setUsername("user");
            user.setHashedPassword(passwordEncoder.encode("user"));
            user.setProfilePictureUrl(
                    "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&ixlib=rb-1.2.1&auto=format&fit=crop&w=128&h=128&q=80");
            user.setRoles(Collections.singleton(Role.USER));
            userRepository.save(user);
            User admin = new User();
            admin.setName("Emma Powerful");
            admin.setUsername("admin");
            admin.setHashedPassword(passwordEncoder.encode("admin"));
            admin.setProfilePictureUrl(
                    "https://images.unsplash.com/photo-1607746882042-944635dfe10e?ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&ixlib=rb-1.2.1&auto=format&fit=crop&w=128&h=128&q=80");
            admin.setRoles(Stream.of(Role.USER, Role.ADMIN).collect(Collectors.toSet()));
            userRepository.save(admin);
            logger.info("... generating 100 Room entities...");
            ExampleDataGenerator<Room> roomRepositoryGenerator = new ExampleDataGenerator<>(Room.class,
                    LocalDateTime.of(2021, 12, 30, 0, 0, 0));
            roomRepositoryGenerator.setData(Room::setId, DataType.ID);
            roomRepositoryGenerator.setData(Room::setName, DataType.WORD);
            roomRepositoryGenerator.setData(Room::setSeat, DataType.NUMBER_UP_TO_100);
            roomRepository.saveAll(roomRepositoryGenerator.create(100, seed));

            logger.info("... generating 100 Session entities...");
            ExampleDataGenerator<Session> sessionRepositoryGenerator = new ExampleDataGenerator<>(Session.class,
                    LocalDateTime.of(2021, 12, 30, 0, 0, 0));
            sessionRepositoryGenerator.setData(Session::setId, DataType.ID);
            sessionRepositoryGenerator.setData(Session::setDate, DataType.DATE_OF_BIRTH);
            sessionRepositoryGenerator.setData(Session::setIniTime, DataType.DATETIME_NEXT_30_DAYS);
            sessionRepositoryGenerator.setData(Session::setEndTime, DataType.DATETIME_LAST_10_YEARS);
            sessionRepositoryGenerator.setData(Session::setValue, DataType.NUMBER_UP_TO_100);
            sessionRepositoryGenerator.setData(Session::setAnimation, DataType.WORD);
            sessionRepositoryGenerator.setData(Session::setAudio, DataType.WORD);
            sessionRepository.saveAll(sessionRepositoryGenerator.create(100, seed));

            logger.info("... generating 100 Movie entities...");
            ExampleDataGenerator<Movie> movieRepositoryGenerator = new ExampleDataGenerator<>(Movie.class,
                    LocalDateTime.of(2021, 12, 30, 0, 0, 0));
            movieRepositoryGenerator.setData(Movie::setId, DataType.ID);
            movieRepositoryGenerator.setData(Movie::setImage, DataType.BOOK_IMAGE_URL);
            movieRepositoryGenerator.setData(Movie::setTitle, DataType.WORD);
            movieRepositoryGenerator.setData(Movie::setDescribe, DataType.WORD);
            movieRepositoryGenerator.setData(Movie::setDuration, DataType.NUMBER_UP_TO_100);
            movieRepository.saveAll(movieRepositoryGenerator.create(100, seed));

            logger.info("Generated demo data");
        };
    }

}