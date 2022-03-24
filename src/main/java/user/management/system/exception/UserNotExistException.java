package user.management.system.exception;

import static user.management.system.utility.AppUtility.USER_NOT_EXIST_EXCEPTION;

public class UserNotExistException extends Exception {
    public UserNotExistException(String s) {
        super(s);
    }
    public static UserNotExistException newException()
    {
        return new UserNotExistException(USER_NOT_EXIST_EXCEPTION);
    }
}
