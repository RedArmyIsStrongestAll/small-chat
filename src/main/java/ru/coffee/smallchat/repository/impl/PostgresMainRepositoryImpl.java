package ru.coffee.smallchat.repository.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.coffee.smallchat.dto.ChatDTO;
import ru.coffee.smallchat.dto.PersonalMessageResponseDTO;
import ru.coffee.smallchat.dto.PublicMessageResponseDTO;
import ru.coffee.smallchat.dto.UserDTO;
import ru.coffee.smallchat.repository.MainRepository;

import java.sql.Types;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class PostgresMainRepositoryImpl implements MainRepository {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public PostgresMainRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public Integer saveName(String name, String userUuid) throws DataAccessException {
        String query = "update user_sessions \n" +
                "set name = ? \n" +
                "where session_id = ?;";
        return jdbcTemplate.update(query, name, userUuid);
    }

    @Override
    @Transactional
    public String getPhotoPath(String userUuid) throws DataAccessException {
        String query = "select us.photo_path \n" +
                "from user_sessions us \n" +
                "where us.session_id = ?;";
        return jdbcTemplate.queryForObject(query, String.class, userUuid);
    }

    @Override
    @Transactional
    public Integer savePhotoPath(String path, String type, String userUuid) throws DataAccessException {
        String query = "update user_sessions \n" +
                "set photo_path = ?, photo_type = ? \n" +
                "where session_id = ?;";
        return jdbcTemplate.update(query, path, type, userUuid);
    }

    @Override
    @Transactional
    public Integer deletePhotoPath(String userUuid) throws DataAccessException {
        String query = "update user_sessions \n" +
                "set photo_path = null \n" +
                "where session_id = ?;";
        return jdbcTemplate.update(query, userUuid);
    }

    @Override
    @Transactional
    public List<PublicMessageResponseDTO> getPublicHistory(Integer offset) throws DataAccessException {
        offset = offset * 50;
        String query = "select pm.message, \n" +
                "pm.producer_user_session_id, \n" +
                "pm.send_time \n" +
                "from public_messages pm \n" +
                "order by id \n" +
                "offset ? \n" +
                "limit 50;";
        return jdbcTemplate.query(query, new Object[]{offset}, new int[]{Types.INTEGER},
                (rs, ri) -> new PublicMessageResponseDTO(rs.getString("message"),
                        rs.getString("send_time"),
                        rs.getString("producer_user_session_id")));
    }

    @Override
    @Transactional
    public List<ChatDTO> getPersonalChatProducerId(String userUuid) throws DataAccessException {
        List<ChatDTO> producerChatIdList = getPersonalChatIdListForProducer(userUuid);
        List<ChatDTO> consumerChatIdList = getPersonalChatIdListForConsumer(userUuid);
        producerChatIdList.addAll(consumerChatIdList);
        return producerChatIdList;
    }

    @Override
    @Transactional
    public ChatDTO getPersonalChatProducerId(String producerUserUuid, String consumerUserUuid) throws DataAccessException {
        String query = "select c.id, c.producer_user_session_id \n" +
                "from chats c \n" +
                "where c.producer_user_session_id = ? \n" +
                "and c.consumer_user_session_id = ?;";
        return jdbcTemplate.queryForObject(query, new Object[]{producerUserUuid, consumerUserUuid},
                new int[]{Types.CHAR, Types.CHAR},
                (rs, ri) -> new ChatDTO(rs.getLong("id"),
                        new UserDTO(rs.getString("producer_user_session_id"))));
    }

    @Transactional
    public List<ChatDTO> getPersonalChatIdListForProducer(String userUuid) throws DataAccessException {
        String query = "select c.id \n" +
                "from chats c \n" +
                "where c.producer_user_session_id = ?;";
        return jdbcTemplate.query(query, new Object[]{userUuid}, new int[]{Types.CHAR},
                (rs, ri) -> new ChatDTO(rs.getLong("id"),
                        new UserDTO(rs.getString("consumer_user_session_id"))));
    }

    @Transactional
    public List<ChatDTO> getPersonalChatIdListForConsumer(String userUuid) throws DataAccessException {
        String query = "select c.id, c.producer_user_session_id \n" +
                "from chats c \n" +
                "where c.consumer_user_session_id = ?;";
        return jdbcTemplate.query(query, new Object[]{userUuid}, new int[]{Types.CHAR},
                (rs, ri) -> new ChatDTO(rs.getLong("id"),
                        new UserDTO(rs.getString("producer_user_session_id"))));
    }

    @Override
    public List<PersonalMessageResponseDTO> getPersonalHistory(Long chatId, Integer offset) throws DataAccessException {
        offset = offset * 50;
        String query = "select pm.message, \n" +
                "c.producer_user_session_id, \n" +
                "c.consumer_user_session_id, \n" +
                "pm.sender_is_producer, \n" +
                "pm.send_time \n" +
                "from personal_messages pm \n" +
                "left join chats c on pm.chat_id = c.id \n" +
                "where pm.chat_id = ? \n" +
                "order by pm.id \n" +
                "offset ? \n" +
                "limit 50;";
        return jdbcTemplate.query(query, new Object[]{chatId, offset}, new int[]{Types.BIGINT, Types.INTEGER},
                (rs, ri) -> new PersonalMessageResponseDTO(rs.getString("message"),
                        rs.getString("send_time"),
                        rs.getString("producer_user_session_id"),
                        rs.getString("consumer_user_session_id"),
                        null,
                        rs.getBoolean("sender_is_producer"),
                        null));
    }

    @Override
    @Transactional
    public UserDTO getUserByUuid(String userUuid) throws DataAccessException {
        String query = "select us.\"name\", us.photo_path \n" +
                "from user_sessions us \n" +
                "where us.session_id = ?;";
        return jdbcTemplate.queryForObject(query, new Object[]{userUuid}, new int[]{Types.CHAR},
                (rs, ri) -> new UserDTO(userUuid,
                        rs.getString("name"),
                        rs.getString("photo_path"),
                        null));
    }

    @Override
    @Transactional
    public Integer savePublicMessage(String message, String producerUserUuid,
                                     LocalDateTime currentTime) throws DataAccessException {
        String query = "insert into public_messages (message, producer_user_session_id, send_time) \n" +
                "values (?, ?, ?);";
        return jdbcTemplate.update(query, message, producerUserUuid, currentTime);
    }

    @Override
    @Transactional
    public Integer savePersonalMessage(String message, LocalDateTime currentTime,
                                       Long chatId, String producerUserUuid, Boolean senderIsProducer) throws DataAccessException {
        String query = "insert into personal_messages (message, send_time, chat_id, sender_is_producer) \n" +
                "values (?, ?, ?, ?);";
        return jdbcTemplate.update(query, message, currentTime, chatId, senderIsProducer);
    }

    @Override
    @Transactional
    public Long saveChat(String producerUserUuid, String consumerUserUuid) throws DataAccessException {
        String query = "insert into chats (producer_user_session_id, consumer_user_session_id) \n" +
                "values (?, ?) \n" +
                "RETURNING id;";
        return jdbcTemplate.queryForObject(query, Long.class, producerUserUuid, consumerUserUuid);
    }
}
