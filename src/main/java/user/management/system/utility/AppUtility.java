package user.management.system.utility;

import lombok.experimental.UtilityClass;

@UtilityClass
public class AppUtility {

    public String SECRET_KEY = "user-management-system";
    // *------------------------ Exception Messages ---------------------*
    public String BANK_NOT_EXIST_EXCEPTION = " !!! There is no Bank Exist from this name Kindly Check the name that are listed here !!!";
    public String USER_NOT_EXIST_EXCEPTION = "!!! There is no User exist with this ID !!!  ----> Kindly Please Check the User Id";
    public String INVALID_CONTACT_NUMBER_EXCEPTION = "!!! Invalid Number !!! , Please take care that the number should be length : 10 and all Digits";
    public String INVALID_EMAIL_EXCEPTION = "!!! Invalid Email Address !!! Please Take of this Format email@mail.com";

    // *-----------------------------------------------------------------*
}
