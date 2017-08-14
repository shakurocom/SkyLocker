package com.shakuro.skylocker.model.entities;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.ToMany;

import java.util.List;

@Entity(
        active = true
)
public class Meaning {

    @Id
    private Long id;

    @NotNull
    private Long wordId;

    @Index(unique = true)
    private String text;

    @NotNull
    private String translation;

    private long viewsCounter;

    private long failsCounter;

    private long addedByUserWithId;

    @ToMany(referencedJoinProperty = "meaningId")
    private List<Alternative> alternatives;

    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated(hash = 1973883728)
    private transient MeaningDao myDao;

    @Generated(hash = 721736358)
    public Meaning(Long id, @NotNull Long wordId, String text, @NotNull String translation,
            long viewsCounter, long failsCounter, long addedByUserWithId) {
        this.id = id;
        this.wordId = wordId;
        this.text = text;
        this.translation = translation;
        this.viewsCounter = viewsCounter;
        this.failsCounter = failsCounter;
        this.addedByUserWithId = addedByUserWithId;
    }

    @Generated(hash = 688675587)
    public Meaning() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getWordId() {
        return this.wordId;
    }

    public void setWordId(Long wordId) {
        this.wordId = wordId;
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

    public Long getViewsCounter() {
        return this.viewsCounter;
    }

    public void setViewsCounter(Long viewsCounter) {
        this.viewsCounter = viewsCounter;
    }

    public Long getFailsCounter() {
        return this.failsCounter;
    }

    public void setFailsCounter(Long failsCounter) {
        this.failsCounter = failsCounter;
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

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 1215460014)
    public List<Alternative> getAlternatives() {
        if (alternatives == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            AlternativeDao targetDao = daoSession.getAlternativeDao();
            List<Alternative> alternativesNew = targetDao._queryMeaning_Alternatives(id);
            synchronized (this) {
                if (alternatives == null) {
                    alternatives = alternativesNew;
                }
            }
        }
        return alternatives;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 837130811)
    public synchronized void resetAlternatives() {
        alternatives = null;
    }

    public long getAddedByUserWithId() {
        return this.addedByUserWithId;
    }

    public void setAddedByUserWithId(long addedByUserWithId) {
        this.addedByUserWithId = addedByUserWithId;
    }

    public void setViewsCounter(long viewsCounter) {
        this.viewsCounter = viewsCounter;
    }

    public void setFailsCounter(long failsCounter) {
        this.failsCounter = failsCounter;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 531054655)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getMeaningDao() : null;
    }
}
