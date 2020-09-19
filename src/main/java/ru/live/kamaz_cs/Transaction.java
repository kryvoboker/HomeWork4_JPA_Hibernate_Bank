package ru.live.kamaz_cs;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "Transactions")

public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String user;
    private double cashOut;
    private String currencyOut;
    private double cashTake;
    private String currencyTake;
    private double cashConversionOut;
    private String currencyConversionOut;
    private String currencyConversionIn;
    private double cashConversionIn;
    private Date date;

    @ManyToMany(mappedBy = "transactions", cascade = CascadeType.DETACH)
    List<User> users = new ArrayList<>();

    public Transaction() {
    }

    public Transaction(String user, double cashOut, String currencyOut, double cashTake, String currencyTake, Date date,
                       double cashConversionOut, String currencyConversionOut, String currencyConversionIn, double cashConversionIn) {
        this.user = user;
        this.cashOut = cashOut;
        this.currencyOut = currencyOut;
        this.cashTake = cashTake;
        this.currencyTake = currencyTake;
        this.date = date;
        this.currencyConversionOut = currencyConversionOut;
        this.cashConversionIn = cashConversionIn;
        this.currencyConversionIn = currencyConversionIn;
        this.cashConversionOut = cashConversionOut;
    }

    public void addUser(User user) {
        if ( ! users.contains(user))
            users.add(user);
        if ( ! user.transactions.contains(this))
            user.transactions.add(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaction)) return false;
        Transaction that = (Transaction) o;
        return getId() == that.getId() &&
                Double.compare(that.getCashOut(), getCashOut()) == 0 &&
                Double.compare(that.getCashTake(), getCashTake()) == 0 &&
                Double.compare(that.getCashConversionOut(), getCashConversionOut()) == 0 &&
                Double.compare(that.getCashConversionIn(), getCashConversionIn()) == 0 &&
                Objects.equals(getUser(), that.getUser()) &&
                Objects.equals(getCurrencyOut(), that.getCurrencyOut()) &&
                Objects.equals(getCurrencyTake(), that.getCurrencyTake()) &&
                Objects.equals(getCurrencyConversionOut(), that.getCurrencyConversionOut()) &&
                Objects.equals(getCurrencyConversionIn(), that.getCurrencyConversionIn()) &&
                Objects.equals(getUsers(), that.getUsers());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getUser(), getCashOut(), getCurrencyOut(), getCashTake(), getCurrencyTake(), getCashConversionOut(), getCurrencyConversionOut(), getCurrencyConversionIn(), getCashConversionIn(), getUsers());
    }

    public List<User> getUsers() {
        return Collections.unmodifiableList(users);
    }

    public double getCashConversionOut() {
        return cashConversionOut;
    }

    public void setCashConversionOut(double cashConversionOut) {
        this.cashConversionOut = cashConversionOut;
    }

    public String getCurrencyConversionOut() {
        return currencyConversionOut;
    }

    public void setCurrencyConversionOut(String currencyConversionOut) {
        this.currencyConversionOut = currencyConversionOut;
    }

    public String getCurrencyConversionIn() {
        return currencyConversionIn;
    }

    public void setCurrencyConversionIn(String currencyConversionIn) {
        this.currencyConversionIn = currencyConversionIn;
    }

    public double getCashConversionIn() {
        return cashConversionIn;
    }

    public void setCashConversionIn(double cashConversionIn) {
        this.cashConversionIn = cashConversionIn;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public double getCashOut() {
        return cashOut;
    }

    public void setCashOut(double cashOut) {
        this.cashOut = cashOut;
    }

    public String getCurrencyOut() {
        return currencyOut;
    }

    public void setCurrencyOut(String currencyOut) {
        this.currencyOut = currencyOut;
    }

    public double getCashTake() {
        return cashTake;
    }

    public void setCashTake(double cashTake) {
        this.cashTake = cashTake;
    }

    public String getCurrencyTake() {
        return currencyTake;
    }

    public void setCurrencyTake(String currencyTake) {
        this.currencyTake = currencyTake;
    }

    public Date getDate() {
        return date;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", user='" + user + '\'' +
                ", cashOut=" + cashOut +
                ", currencyOut='" + currencyOut + '\'' +
                ", cashTake=" + cashTake +
                ", currencyTake='" + currencyTake + '\'' +
                ", usdConversionOut=" + cashConversionOut +
                ", eurConversionOut=" + currencyConversionOut +
                ", uahConversionOut=" + currencyConversionIn +
                ", uahConversionIn=" + cashConversionIn +
                ", date=" + date +
                '}';
    }
}
