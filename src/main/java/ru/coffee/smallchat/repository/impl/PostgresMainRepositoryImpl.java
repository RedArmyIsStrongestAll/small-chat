package ru.coffee.smallchat.repository.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.coffee.smallchat.dto.*;
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
    public String rigestryUser(OAuthRegistryDTO registry) throws DataAccessException {
        String query = "insert into users (auth_id, auth_type_id) \n" +
                "values (?, ?)" +
                "RETURNING id;";
        return jdbcTemplate.queryForObject(query, Long.class,
                Long.valueOf(registry.getId()), registry.getCodeType()).toString();
    }

    @Override
    @Transactional
    public int saveName(String name, String userId) throws DataAccessException {
        String query = "update users \n" +
                "set name = ? \n" +
                "where id = ?;";
        return jdbcTemplate.update(query, name, Long.valueOf(userId));
    }

    @Override
    @Transactional
    public String getPhotoPath(String userId) throws DataAccessException {
        String query = "select us.photo_path \n" +
                "from users us \n" +
                "where us.id = ?;";
        return jdbcTemplate.queryForObject(query, String.class, Long.valueOf(userId));
    }

    @Override
    @Transactional
    public int savePhotoPath(String path, String type, String userId) throws DataAccessException {
        String query = "update users \n" +
                "set photo_path = ?, photo_type = ? \n" +
                "where id = ?;";
        return jdbcTemplate.update(query, path, type, Long.valueOf(userId));
    }

    @Override
    @Transactional
    public int deletePhotoPath(String userId) throws DataAccessException {
        String query = "update users \n" +
                "set photo_path = null \n" +
                "where id = ?;";
        return jdbcTemplate.update(query, Long.valueOf(userId));
    }

    @Override
    @Transactional
    public List<PublicMessageResponseDTO> getPublicHistory(int offset) throws DataAccessException {
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
                        String.valueOf(rs.getLong("producer_user_id"))));
    }

    @Override
    @Transactional
    public List<ChatDTO> getListPersonalChatByUserId(String userId) throws DataAccessException {
        List<ChatDTO> producerChatIdList = getPersonalChatListByProducerId(userId);
        List<ChatDTO> consumerChatIdList = getPersonalChatListByConsumerId(userId);
        producerChatIdList.addAll(consumerChatIdList);
        return producerChatIdList;
    }

    @Override
    @Transactional
    public ChatAdapterWithFlagProducerDTO getPersonalChatByUserIdAndChatId(long chatId, String userId) throws DataAccessException {
        List<ChatDTO> chatUserProducerList = getPersonalChatByProducerIdAndChatId(chatId, userId);
        if (!chatUserProducerList.isEmpty() &&
                chatUserProducerList.get(0).getPartnerUser().getUserId() != null) {
            return new ChatAdapterWithFlagProducerDTO(chatUserProducerList.get(0), true);
        }
        List<ChatDTO> chatUserConsumerList = getPersonalChatByConsumerIdAndChatId(chatId, userId);
        if (!chatUserConsumerList.isEmpty() &&
                chatUserConsumerList.get(0).getPartnerUser().getUserId() != null) {
            return new ChatAdapterWithFlagProducerDTO(chatUserConsumerList.get(0), false);
        }
        return null;
    }

    @Transactional
    public List<ChatDTO> getPersonalChatByProducerIdAndChatId(long chatId, String producerId) throws DataAccessException {
        String query = "select c.consumer_user_id \n" +
                "from chats c \n" +
                "where c.id = ?\n" +
                "and c.producer_user_id = ?\n" +
                "and c.deleted_at is null;";
        return jdbcTemplate.query(query, new Object[]{chatId, Long.valueOf(producerId)},
                new int[]{Types.BIGINT, Types.BIGINT},
                (rs, rowNum) -> new ChatDTO(chatId,
                        new UserDTO(String.valueOf(rs.getLong("consumer_user_id")))));
    }

    @Transactional
    public List<ChatDTO> getPersonalChatByConsumerIdAndChatId(long chatId, String consumerId) throws DataAccessException {
        String query = "select c.producer_user_id \n" +
                "from chats c \n" +
                "where c.id = ?\n" +
                "and c.consumer_user_id = ?\n" +
                "and c.deleted_at is null;";
        return jdbcTemplate.query(query, new Object[]{chatId, Long.valueOf(consumerId)},
                new int[]{Types.BIGINT, Types.BIGINT},
                (rs, rowNum) -> new ChatDTO(chatId,
                        new UserDTO(String.valueOf(rs.getLong("producer_user_id")))));
    }

    @Transactional
    public List<ChatDTO> getPersonalChatListByProducerId(String userId) throws DataAccessException {
        String query = "select c.id, c.consumer_user_id \n" +
                "from chats c \n" +
                "where c.producer_user_id = ? \n" +
                "and c.deleted_at is null;";
        return jdbcTemplate.query(query, new Object[]{Long.valueOf(userId)}, new int[]{Types.BIGINT},
                (rs, ri) -> new ChatDTO(rs.getLong("id"),
                        new UserDTO(String.valueOf(rs.getLong("consumer_user_id")))));
    }

    @Transactional
    public List<ChatDTO> getPersonalChatListByConsumerId(String userId) throws DataAccessException {
        String query = "select c.id, c.producer_user_id \n" +
                "from chats c \n" +
                "where c.consumer_user_id = ? \n" +
                "and c.deleted_at is null;";
        return jdbcTemplate.query(query, new Object[]{Long.valueOf(userId)}, new int[]{Types.BIGINT},
                (rs, ri) -> new ChatDTO(rs.getLong("id"),
                        new UserDTO(String.valueOf(rs.getLong("producer_user_id")))));
    }

    @Override
    public List<PersonalMessageResponseDTO> getPersonalHistory(long chatId, int offset) throws DataAccessException {
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
                        String.valueOf(rs.getLong("producer_user_id")),
                        String.valueOf(rs.getLong("consumer_user_id")),
                        chatId,
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
        return jdbcTemplate.queryForObject(query, new Object[]{Long.valueOf(userId)}, new int[]{Types.BIGINT},
                (rs, ri) -> new UserDTO(userId,
                        rs.getString("name"),
                        rs.getString("photo_path"),
                        null,
                        rs.getString("photo_type")));
    }

    @Override
    @Transactional
    public List<UserAuthDTO> getUserByAuthId(String authId) throws DataAccessException {
        String query = "select us.id, us.deleted_at \n" +
                "from users us \n" +
                "where us.auth_id = ? \n;";
        return jdbcTemplate.query(query, new Object[]{Long.valueOf(authId)}, new int[]{Types.BIGINT},
                (rs, ri) -> new UserAuthDTO(String.valueOf(rs.getLong("id")),
                        rs.getString("deleted_at")));
    }

    @Override
    @Transactional
    public int updateLastLoginTime(String userId) {
        String query = "update users \n" +
                "set last_login_at = now() \n" +
                "where id = ?;";
        return jdbcTemplate.update(query, Long.valueOf(userId));
    }

    @Override
    public String getLastLoginTime(String userId) {
        String query = "select us.last_login_at \n" +
                "from users us \n" +
                "where us.id = ? \n;";
        return jdbcTemplate.queryForObject(query, new Object[]{Long.valueOf(userId)},
                new int[]{Types.BIGINT}, String.class);
    }

    @Override
    @Transactional
    public int deleteUser(String userId) throws DataAccessException {
        String query = "update users \n" +
                "set deleted_at = now() \n" +
                "where id = ?;";
        return jdbcTemplate.update(query, Long.valueOf(userId));
    }


    @Override
    @Transactional
    public int reDeleteUser(String userId) throws DataAccessException {
        String query = "update users \n" +
                "set deleted_at = null \n" +
                "where id = ?;";
        return jdbcTemplate.update(query, Long.valueOf(userId));
    }

    @Override
    @Transactional
    public int savePublicMessage(String message, String producerUserId,
                                 LocalDateTime currentTime) throws DataAccessException {
        String query = "insert into public_messages (message, producer_user_id, send_time) \n" +
                "values (?, ?, ?);";
        return jdbcTemplate.update(query, message, Long.valueOf(producerUserId), currentTime);
    }

    @Override
    @Transactional
    public int savePersonalMessage(String message, LocalDateTime currentTime,
                                   long chatId, boolean senderIsProducer) throws DataAccessException {
        String query = "insert into personal_messages (message, send_time, chat_id, sender_is_producer) \n" +
                "values (?, ?, ?, ?);";
        return jdbcTemplate.update(query, message, currentTime, chatId, senderIsProducer);
    }

    @Override
    @Transactional
    public long saveChat(String producerUserId, String consumerUserId) throws DataAccessException {
        String query = "insert into chats (producer_user_id, consumer_user_id) \n" +
                "values (?, ?) \n" +
                "RETURNING id;";
        return jdbcTemplate.queryForObject(query, Long.class, Long.valueOf(producerUserId), Long.valueOf(consumerUserId));
    }
}
