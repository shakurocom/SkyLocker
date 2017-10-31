package com.shakuro.skylocker.model.entities;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.ToOne;

@Entity(
        active = true
)
public class Alternative {

    @Id
    private Long id;

    @NotNull
    private String text;

    @NotNull
    private String translation;

    private long meaningId;

    @ToOne(joinProperty = "meaningId")
    private Meaning meaning;

    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated(hash = 1907038125)
    private transient AlternativeDao myDao;

    @Generated(hash = 227935336)
    private transient Long meaning__resolvedKey;

    @Generated(hash = 930548703)
    public Alternative(Long id, @NotNull String text, @NotNull String translation, long meaningId) {
        this.id = id;
        this.text = text;
        this.translation = translation;
        this.meaningId = meaningId;
    }

    @Generated(hash = 323669516)
    public Alternative() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTranslation() {
        return this.translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }

    public Long getMeaningId() {
        return this.meaningId;
    }

    public void setMeaningId(Long meaningId) {
        this.meaningId = meaningId;
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

    /** To-one relationship, resolved on first access. */
    @Generated(hash = 1750250012)
    public Meaning getMeaning() {
        long __key = this.meaningId;
        if (meaning__resolvedKey == null || !meaning__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            MeaningDao targetDao = daoSession.getMeaningDao();
            Meaning meaningNew = targetDao.load(__key);
            synchronized (this) {
                meaning = meaningNew;
                meaning__resolvedKey = __key;
            }
        }
        return meaning;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 13712324)
    public void setMeaning(@NotNull Meaning meaning) {
        if (meaning == null) {
            throw new DaoException(
                    "To-one property 'meaningId' has not-null constraint; cannot set to-one to null");
        }
        synchronized (this) {
            this.meaning = meaning;
            meaningId = meaning.getId();
            meaning__resolvedKey = meaningId;
        }
    }

    public void setMeaningId(long meaningId) {
        this.meaningId = meaningId;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 185575403)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getAlternativeDao() : null;
    }
}
