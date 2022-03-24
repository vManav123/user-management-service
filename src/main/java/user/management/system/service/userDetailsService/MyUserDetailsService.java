package user.management.system.service.userDetailsService;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import user.management.system.exception.UserNotExistException;
import user.management.system.model.user.Credentials;
import user.management.system.repository.CredentialsRepository;

import java.util.List;


import static user.management.system.utility.AppUtility.USER_NOT_EXIST_EXCEPTION;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private CredentialsRepository credentialsRepository;

    @Override
    @SneakyThrows
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        List<Credentials> userList = credentialsRepository.findAll();
        if(userList.parallelStream().noneMatch(p -> p.getUsername().equals(username)))
            throw new UsernameNotFoundException(USER_NOT_EXIST_EXCEPTION);
        Credentials credentials = userList.parallelStream().filter(p->p.getUsername().equals(username)).findFirst().orElseThrow(UserNotExistException::newException);
        Credentials updateCredentials = new Credentials(credentials.getUsername(), credentials.getPassword(),credentials.getUser_id(),credentials.getRoles());
        return new User(updateCredentials.getUsername(),updateCredentials.getPassword(),updateCredentials.getAuthorities());
    }
}