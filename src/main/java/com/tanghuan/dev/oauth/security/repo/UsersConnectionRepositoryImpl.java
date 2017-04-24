package com.tanghuan.dev.oauth.security.repo;

import com.tanghuan.dev.oauth.entity.domain.UserConnection;
import com.tanghuan.dev.oauth.repository.UserConnectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.social.connect.*;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Arthur on 2017/4/24.
 */
public class UsersConnectionRepositoryImpl implements UsersConnectionRepository {

    @Autowired
    private UserConnectionRepository userConnectionRepository;

    private final ConnectionFactoryLocator connectionFactoryLocator;

    private final TextEncryptor textEncryptor;

    private ConnectionSignUp connectionSignUp;

    public UsersConnectionRepositoryImpl(ConnectionFactoryLocator connectionFactoryLocator, TextEncryptor textEncryptor) {
        this.connectionFactoryLocator = connectionFactoryLocator;
        this.textEncryptor = textEncryptor;
    }

    @Override
    public List<String> findUserIdsWithConnection(Connection<?> connection) {
        ConnectionKey connectionKey = connection.getKey();

        List<UserConnection> userConnections = userConnectionRepository.findByProviderIdAndProviderUserId(connectionKey.getProviderId(), connectionKey.getProviderUserId());

        if (userConnections == null || userConnections.size() == 0) return null;

        List<String> localUserIds = userConnections.stream().map(UserConnection::getUserId).distinct().collect(Collectors.toList());

        if (localUserIds.size() == 0 && connectionSignUp != null) {
            String newUserId = connectionSignUp.execute(connection);
            if (newUserId != null)
            {
                createConnectionRepository(newUserId).addConnection(connection);
                return Arrays.asList(newUserId);
            }
        }

        return localUserIds;
    }

    @Override
    public Set<String> findUserIdsConnectedTo(String providerId, Set<String> providerUserIds) {

        List<UserConnection> userConnections = userConnectionRepository.findByProviderIdAndProviderUserIdIn(providerId, providerUserIds);

        if (userConnections == null || userConnections.size() == 0) return null;

        Set<String> localUserIds = userConnections.stream().map(UserConnection::getUserId).distinct().collect(Collectors.toSet());

        return localUserIds;
    }

    @Override
    public ConnectionRepository createConnectionRepository(String userId) {
        return new ConnectionRepositoryImpl(userId, connectionFactoryLocator, textEncryptor);
    }

    public void setConnectionSignUp(ConnectionSignUp connectionSignUp) {
        this.connectionSignUp = connectionSignUp;
    }

}
