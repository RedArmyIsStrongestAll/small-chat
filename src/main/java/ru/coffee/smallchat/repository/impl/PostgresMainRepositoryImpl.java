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
    public Integer saveName(String name, String userId) throws DataAccessException {
        String query = "update users \n" +
                "set name = ? \n" +
                "where id = ?;";
        return jdbcTemplate.update(query, name, userId);
    }

    @Override
    @Transactional
    public String getPhotoPath(String userId) throws DataAccessException {
        String query = "select us.photo_path \n" +
                "from users us \n" +
                "where us.id = ?;";
        return jdbcTemplate.queryForObject(query, String.class, userId);
    }

    @Override
    @Transactional
    public Integer savePhotoPath(String path, String type, String userId) throws DataAccessException {
        String query = "update users \n" +
                "set photo_path = ?, photo_type = ? \n" +
                "where id = ?;";
        return jdbcTemplate.update(query, path, type, userId);
    }

    @Override
    @Transactional
    public Integer deletePhotoPath(String userId) throws DataAccessException {
        String query = "update users \n" +
                "set photo_path = null \n" +
                "where id = ?;";
        return jdbcTemplate.update(query, userId);
    }

    @Override
    @Transactional
    public List<PublicMessageResponseDTO> getPublicHistory(Integer offset) throws DataAccessException {
        offset = offset * 50;
        String query = "select pm.message, \n" +
                "pm.producer_user_id, \n" +
                "pm.send_time \n" +
                "from public_messages pm \n" +
                "where pm.deleted_at is null \n" +
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
    public List<ChatDTO> getPersonalChatProducerId(String userId) throws DataAccessException {
        List<ChatDTO> producerChatIdList = getPersonalChatIdListForProducer(userId);
        List<ChatDTO> consumerChatIdList = getPersonalChatIdListForConsumer(userId);
        producerChatIdList.addAll(consumerChatIdList);
        return producerChatIdList;
    }

    @Override
    @Transactional
    public ChatDTO getPersonalChatProducerId(String producerUserId, String consumerUserId) throws DataAccessException {
        String query = "select c.id, c.producer_user_id \n" +
                "from chats c \n" +
                "where c.producer_user_id = ? \n" +
                "and c.consumer_user_id = ? \n" +
                "and c.deleted_at is null \n";
        return jdbcTemplate.queryForObject(query, new Object[]{producerUserId, consumerUserId},
                new int[]{Types.CHAR, Types.CHAR},
                (rs, ri) -> new ChatDTO(rs.getLong("id"),
                        new UserDTO(rs.getString("producer_user_session_id"))));
    }

    @Transactional
    public List<ChatDTO> getPersonalChatIdListForProducer(String userId) throws DataAccessException {
        String query = "select c.id, c.consumer_user_id \n" +
                "from chats c \n" +
                "where c.producer_user_id = ? \n" +
                "and c.deleted_at is null;";
        return jdbcTemplate.query(query, new Object[]{userId}, new int[]{Types.CHAR},
                (rs, ri) -> new ChatDTO(rs.getLong("id"),
                        new UserDTO(rs.getString("consumer_user_session_id"))));
    }

    @Transactional
    public List<ChatDTO> getPersonalChatIdListForConsumer(String userId) throws DataAccessException {
        String query = "select c.id, c.producer_user_id \n" +
                "from chats c \n" +
                "where c.consumer_user_id = ? \n" +
                "and c.deleted_at is null;";
        return jdbcTemplate.query(query, new Object[]{userId}, new int[]{Types.CHAR},
                (rs, ri) -> new ChatDTO(rs.getLong("id"),
                        new UserDTO(rs.getString("producer_user_session_id"))));
    }

    @Override
    public List<PersonalMessageResponseDTO> getPersonalHistory(Long chatId, Integer offset) throws DataAccessException {
        offset = offset * 50;
        String query = "select pm.message, \n" +
                "c.producer_user_id, \n" +
                "c.consumer_user_id, \n" +
                "pm.sender_is_producer, \n" +
                "pm.send_time \n" +
                "from personal_messages pm \n" +
                "left join chats c on pm.chat_id = c.id \n" +
                "where pm.chat_id = ? \n" +
                "and c.deleted_at is null \n" +
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
    public UserDTO getUserById(String userId) throws DataAccessException {
        String query = "select us.\"name\", us.photo_path, us.photo_type \n" +
                "from users us \n" +
                "where us.id = ? \n" +
                "and us.deleted_at is null;";
        return jdbcTemplate.queryForObject(query, new Object[]{userId}, new int[]{Types.CHAR},
                (rs, ri) -> new UserDTO(userId,
                        rs.getString("name"),
                        rs.getString("photo_path"),
                        null,
                        rs.getString("photo_type")));
    }

    @Override
    @Transactional
    public Integer deleteUser(String userId) throws DataAccessException {
        String query = "update users \n" +
                "set deleted_at = now() \n" +
                "where id = ?;";
        return jdbcTemplate.update(query, userId);
    }

    @Override
    @Transactional
    public Integer savePublicMessage(String message, String producerUserId,
                                     LocalDateTime currentTime) throws DataAccessException {
        String query = "insert into public_messages (message, producer_user_id, send_time) \n" +
                "values (?, ?, ?);";
        return jdbcTemplate.update(query, message, producerUserId, currentTime);
    }

    @Override
    @Transactional
    public Integer savePersonalMessage(String message, LocalDateTime currentTime,
                                       Long chatId, String producerUserId, Boolean senderIsProducer) throws DataAccessException {
        String query = "insert into personal_messages (message, send_time, chat_id, sender_is_producer) \n" +
                "values (?, ?, ?, ?);";
        return jdbcTemplate.update(query, message, currentTime, chatId, senderIsProducer);
    }

    @Override
    @Transactional
    public Long saveChat(String producerUserId, String consumerUserId) throws DataAccessException {
        String query = "insert into chats (producer_user_id, consumer_user_id) \n" +
                "values (?, ?) \n" +
                "RETURNING id;";
        return jdbcTemplate.queryForObject(query, Long.class, producerUserId, consumerUserId);
    }
}
