package liza.com.rateinstructor;

/**
 * Created by SONY on 3/13/2015.
 */
public class Instructor {

    //Static final variables
    public static final String KEY_INSTRUCTOR_ID = "id";
    public static final String KEY_FULL_NAME = "fullName" ;
    public static final String KEY_FIRST_NAME = "firstName";
    public static final String KEY_LAST_NAME = "lastName";
    public static final String KEY_OFFICE = "office";
    public static final String KEY_PHONE = "phone";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_AVG_RATING = "average";
    public static final String KEY_TOTAL_RATING = "totalRatings";
    public static final String KEY_COMMENT_LIST ="comments";


    //private variables
    long _id;
    String fullName;
    String firstName;
    String lastName;
    String office;
    String phone;
    String email;
    String average;
    String total;
    String comments;

    public long get_id() {
        return _id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getOffice() {
        return office;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getAverage() {
        return average;
    }

    public String getTotal() {
        return total;
    }

    public String getComments() {
        return comments;
    }

    // constructor
    public Instructor(long id, String firstName, String lastName){
        this._id = id;
        this.firstName = firstName;
        this.lastName = lastName;

    }
    // constructor
    public Instructor(long id,String firstName, String lastName, String office,String phone,
                      String email,String average,String total){
        this._id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.office = office;
        this.phone = phone;
        this.email = email;
        this.average = average;
        this.total = total;
    }
    // constructor
    public Instructor(long id,String comments){
        this._id = id;
        this.comments = comments;
    }

    @Override
    public String toString(){
        return firstName + " " + lastName;
    }

}
