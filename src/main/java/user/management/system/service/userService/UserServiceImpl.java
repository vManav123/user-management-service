package user.management.system.service.userService;

import lombok.SneakyThrows;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.stereotype.Service;
import user.management.system.exception.InvalidContactNumberException;
import user.management.system.exception.InvalidEmailException;
import user.management.system.exception.UserNotExistException;
import user.management.system.model.user.*;
import user.management.system.repository.CredentialsRepository;
import user.management.system.repository.UserRepository;
import user.management.system.service.emailService.EmailService;
import user.management.system.service.sequenceGeneratorService.DataSequenceGeneratorService;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static user.management.system.utility.AppUtility.*;

@Service("userServiceImpl")
public class UserServiceImpl implements UserService {


    // *------------------ Autowired Reference Variables ----------------*
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CredentialsRepository credentialsRepository;
    @Autowired
    private DataSequenceGeneratorService sequenceGeneratorService;
    @Autowired
    private Credentials credentials;
    @Autowired
    private EmailService emailService;
    // *-----------------------------------------------------------------*



    @Override
    public List<User> getAllUser() {
        return userRepository.findAll();
    }

    public boolean isNumeric(String info) {
        try {
            Double.parseDouble(info);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }


    // *-------------User Account Creation Functionalities -------------*
    public boolean isEmailValid(String email) {
        String regex = "^[a-zA-Z0-9_+&*-]+(?:\\." +
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    @Override
    public String createUser(UserForm userForm) {
        try {
            if (userForm.getContact_no().length() != 10 && isNumeric(userForm.getContact_no()))
                throw new InvalidContactNumberException(INVALID_CONTACT_NUMBER_EXCEPTION);
            if (!isEmailValid(userForm.getEmail_address()))
                throw new InvalidEmailException(INVALID_EMAIL_EXCEPTION);
            InternetAddress internetAddress = new InternetAddress(userForm.getEmail_address());
            internetAddress.validate();
        } catch (InvalidContactNumberException | InvalidEmailException | AddressException e) {
            return e.getMessage();
        }

        User user = new User();

        user.setUser_id(sequenceGeneratorService.getUserSequenceNumber("user_sequence"));
        user.setFull_name(userForm.getFull_name());
        user.setAge(userForm.getAge());
        user.setContact_no(userForm.getContact_no());
        credentials.setUser_id(user.getUser_id());
        credentials.setUsername("user" + Arrays.stream(userForm.getFull_name().split(" ")).collect(Collectors.toList()).get(0)+new Random().nextInt(10000));
        user.setEmail_address(userForm.getEmail_address());
        // Generating password here
        user.setBank_name("no Bank added");
        user.setAccount_no(0L);
        user.setCredit_card_no("null");
        user.setCvv("null");
        user.setExpiry_date(LocalDate.now());
        user.setFailed_attempts(0);
        user.setAccount_non_locked(false);
        user.setLock_time(LocalTime.MIN);
        user.setTickets(new HashMap<>());
        String password = RandomStringUtils.randomAlphanumeric(new Random().nextInt(3)+8);
        Pbkdf2PasswordEncoder encoder = new Pbkdf2PasswordEncoder();
        credentials.setPassword(encoder.encode(password));
        if(SECRET_KEY.equals(userForm.getSecret_key()))
            user.setRoles("ADMIN");
        else
            user.setRoles("USER");
        credentials.setRoles(user.getRoles());
        userRepository.save(user);
        credentialsRepository.save(credentials);
        emailService.sendSimpleEmail(userForm.getEmail_address(),"Dear "+ userForm.getFull_name()+",\nThank you for registering on Railway application System\n\nWith Regards\nRailway Developer\nrailway.reservation.system@gmail.com","Welcome to Railway Reservation System");
        emailService.sendSimpleEmail(userForm.getEmail_address(),"User Created Successfully \n\n Your Credentials are here \n Username : " + credentials.getUsername() + " \n Password : " + password,"Your user account has been Created");
        return "Your User Account has been created with this "+user.getUser_id()+" and your will get your credentials on email address, go and check it";
    }
    // *---------------------------------------------------------------------------------*


    @Override
    public String getEmailAddress(long id) {
        if(userRepository.existsById(id))
            return userRepository.findById(id).get().getEmail_address();
        var listUser = userRepository.findAll().parallelStream().filter(p->p.getAccount_no().equals(id)).collect(Collectors.toList());
        if(listUser.isEmpty())
            return "null";
        return listUser.get(0).getEmail_address();
    }

    // *------------------------------- Basic Functionalities ---------------------------*
    @Override
    public String addUser(User user) {
        user.setUser_id(sequenceGeneratorService.getUserSequenceNumber("user_sequence"));
        return "*--------- User added Successfully ---------*";
    }

    @Override
    public boolean userExistById(Long user_id) {
        return userRepository.existsById(user_id);
    }

    @Override
    public String updateUser(User user) {
        try{
            if(!userRepository.existsById(user.getUser_id()))
                throw new UserNotExistException("Invalid User");
            if(!isEmailValid(user.getEmail_address()))
                throw new InvalidEmailException("Invalid Email");
            if(user.getAge()<=0)
                throw new InvalidEmailException("Invalid age");
            if(user.getFull_name().isBlank())
                throw new InvalidEmailException("Invalid Name");
        }
        catch (InvalidEmailException | UserNotExistException e)
        {
            return e.getMessage();
        }
        userRepository.save(user);
        return "User Details Updated for this user id : "+user.getUser_id();
    }

    @Override
    public User getUser(long user_id) {
        if(!userRepository.existsById(user_id))
            return new User(0L,"no User Exist",0,"null","null","null",0L,"null","null",LocalDate.now(),"null",1,false,LocalTime.MIN,new HashMap<>());
        return userRepository.findById(user_id).get();
    }

    @Override
    @SneakyThrows
    public String changePassword(ChangePassword changePassword) {
        try {
            if(!userRepository.existsById(changePassword.getUser_id()))
                throw new UserNotExistException(USER_NOT_EXIST_EXCEPTION);
            if(!changePassword.getNew_password().equals(changePassword.getConfirm_password()))
                throw new InputMismatchException("Password not matching");
        }
        catch (UserNotExistException | InputMismatchException e)
        {
            return e.getMessage();
        }
        User user = new User();
        Credentials credentials = credentialsRepository
                .findAll()
                .parallelStream()
                .filter(p->p.getUser_id().equals(changePassword.getUser_id()))
                .findFirst()
                .orElseThrow(UserNotExistException::newException);
        credentials.setPassword(changePassword.getNew_password());
        credentials.setRoles(user.getRoles());
        credentialsRepository.save(credentials);
        return "Your password Changed Successfully for this user_id : "+changePassword.getUser_id();
    }

    @Override
    public String saveTicket(Long account_no,long pnr, Ticket ticket) {
        List<User> userList = userRepository.findAll();
        try{
           if(userList.parallelStream().noneMatch(p -> p.getAccount_no().equals(account_no)))
               throw new UserNotExistException(USER_NOT_EXIST_EXCEPTION);
           if(pnr<0)
               throw new InvalidContactNumberException("Invalid PNR");
        }
        catch (UserNotExistException | InvalidContactNumberException e)
        {
            return e.getMessage();
        }

        User user=userList.parallelStream().filter(p->p.getAccount_no().equals(account_no)).collect(Collectors.toList()).get(0);
        user.getTickets().put(pnr,ticket);
        userRepository.save(user);
        return "*------ Ticket Added Successfully -------*";
    }

    @Override
    public String getCredentials(long user_id) {
        if(credentialsRepository.findAll().parallelStream().noneMatch(p -> p.getUser_id().equals(user_id)))
            return "Invalid User ID";
        Credentials credentials = credentialsRepository.findAll().parallelStream().filter(p->p.getUser_id().equals(user_id)).collect(Collectors.toList()).get(0);
        emailService.sendSimpleEmail(userRepository.findById(user_id).get().getEmail_address(),"Dear  "+ Arrays.stream(userRepository.findById(user_id).get().getFull_name().split(" ")).collect(Collectors.toList()).get(0)+",\n\n Your Credentials are here \n Username : " + credentials.getUsername() + " \n Password : " + credentials.getPassword()+"\n\nWith Regards\nUser Management Service\nuser.development.service@gmail.com","Your user account has been Created");
        return "Your Credentials send it to your gmail , go and check it" ;
    }

    @Override
    public String addAllUser(List<User> users) {
        users.forEach(p -> p.setUser_id(sequenceGeneratorService.getUserSequenceNumber("user_sequence")));
        userRepository.saveAll(users);
        return "*------ All Users Added Successfully -------*";
    }
    // *---------------------------------------------------------------------------------*


}
