package com.iogarage.ke.pennywise.entities;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.ToMany;
import org.greenrobot.greendao.annotation.ToOne;

import java.util.Date;
import java.util.List;

@Entity( nameInDb = "debts")
public class Debt {

    @Id
    private Long id;
    private Date transactiondate;
    private String personname;
    private String phonenumber;
    private Double amount;
    private String note;
    private Date paydate;
    private Boolean paid;
    private Double balance;
    private Long loannumber;
    private Integer type;
    private String currency;
    private Integer status;
    //private Long loanId;
    private Long reminderId;


    //temporarily disabled
    //@ToOne(joinProperty = "loanId")
    //private LoanType loanType;

    @ToOne(joinProperty = "reminderId")
    private Reminder reminder;

    @ToMany(referencedJoinProperty = "transactionId")
    private List<Payment> payments;
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 615885837)
    private transient DebtDao myDao;

    @Generated(hash = 1354043051)
    public Debt(Long id, Date transactiondate, String personname,
            String phonenumber, Double amount, String note, Date paydate,
            Boolean paid, Double balance, Long loannumber, Integer type,
            String currency, Integer status, Long reminderId) {
        this.id = id;
        this.transactiondate = transactiondate;
        this.personname = personname;
        this.phonenumber = phonenumber;
        this.amount = amount;
        this.note = note;
        this.paydate = paydate;
        this.paid = paid;
        this.balance = balance;
        this.loannumber = loannumber;
        this.type = type;
        this.currency = currency;
        this.status = status;
        this.reminderId = reminderId;
    }

    @Generated(hash = 488411483)
    public Debt() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getTransactiondate() {
        return this.transactiondate;
    }

    public void setTransactiondate(Date transactiondate) {
        this.transactiondate = transactiondate;
    }

    public String getPersonname() {
        return this.personname;
    }

    public void setPersonname(String personname) {
        this.personname = personname;
    }

    public String getPhonenumber() {
        return this.phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public Double getAmount() {
        return this.amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getNote() {
        return this.note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Date getPaydate() {
        return this.paydate;
    }

    public void setPaydate(Date paydate) {
        this.paydate = paydate;
    }

    public Boolean getPaid() {
        return this.paid;
    }

    public void setPaid(Boolean paid) {
        this.paid = paid;
    }

    public Double getBalance() {
        return this.balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public Long getLoannumber() {
        return this.loannumber;
    }

    public void setLoannumber(Long loannumber) {
        this.loannumber = loannumber;
    }

    public Integer getType() {
        return this.type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getCurrency() {
        return this.currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Integer getStatus() {
        return this.status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getReminderId() {
        return this.reminderId;
    }

    public void setReminderId(Long reminderId) {
        this.reminderId = reminderId;
    }

    @Generated(hash = 251961730)
    private transient Long reminder__resolvedKey;

    /** To-one relationship, resolved on first access. */
    @Generated(hash = 2069036571)
    public Reminder getReminder() {
        Long __key = this.reminderId;
        if (reminder__resolvedKey == null || !reminder__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            ReminderDao targetDao = daoSession.getReminderDao();
            Reminder reminderNew = targetDao.load(__key);
            synchronized (this) {
                reminder = reminderNew;
                reminder__resolvedKey = __key;
            }
        }
        return reminder;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 668954891)
    public void setReminder(Reminder reminder) {
        synchronized (this) {
            this.reminder = reminder;
            reminderId = reminder == null ? null : reminder.getId();
            reminder__resolvedKey = reminderId;
        }
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 533536130)
    public List<Payment> getPayments() {
        if (payments == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            PaymentDao targetDao = daoSession.getPaymentDao();
            List<Payment> paymentsNew = targetDao._queryDebt_Payments(id);
            synchronized (this) {
                if (payments == null) {
                    payments = paymentsNew;
                }
            }
        }
        return payments;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 1083720232)
    public synchronized void resetPayments() {
        payments = null;
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1357896984)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getDebtDao() : null;
    }
    
    


}
