package xx.json.parser;

import java.io.Serializable;

/**
 * Created by xiaoxin on 17-8-1.
 */

public class Person implements Serializable{

    String name;
    String sex;
    String QQ;
    String contact;

    public Person() {
        // TODO Auto-generated constructor stub
    }

    public Person(String name, String sex, String qQ, String contact) {
        super();
        this.name = name;
        this.sex = sex;
        QQ = qQ;
        this.contact = contact;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the sex
     */
    public String getSex() {
        return sex;
    }

    /**
     * @param sex
     *            the sex to set
     */
    public void setSex(String sex) {
        this.sex = sex;
    }

    /**
     * @return the qQ
     */
    public String getQQ() {
        return QQ;
    }

    /**
     * @param qQ
     *            the qQ to set
     */
    public void setQQ(String qQ) {
        QQ = qQ;
    }

    /**
     * @return the contact
     */
    public String getContact() {
        return contact;
    }

    /**
     * @param contact
     *            the contact to set
     */
    public void setContact(String contact) {
        this.contact = contact;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Person [name=" + name + ", sex=" + sex + ", QQ=" + QQ
                + ", contact=" + contact + "]";
    }

}
