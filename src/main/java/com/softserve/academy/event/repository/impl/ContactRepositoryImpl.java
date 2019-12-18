package com.softserve.academy.event.repository.impl;

import com.softserve.academy.event.entity.Contact;
import com.softserve.academy.event.entity.User;
import com.softserve.academy.event.repository.ContactRepository;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ContactRepositoryImpl extends BasicRepositoryImpl<Contact, Long> implements ContactRepository {
    @Override
    public Optional<Long> getIdByEmail(String email) {
        Session session = sessionFactory.getCurrentSession();
        Query query = session.createQuery("select c.id " +
                "from " + clazz.getName() + " as c" +
                " where c.email like \'" + email + "\'");
        List<Long> res = query.getResultList();
        if (res.isEmpty()) return Optional.empty();
        return Optional.ofNullable(res.get(0));
    }

    @Override
    public void saveEmail(String email, User user) {
        Session session = sessionFactory.getCurrentSession();
        Contact contact = new Contact();
        contact.setEmail(email);
        contact.setUser(user);
        session.save(contact);
    }

    public Contact getEmailAndUserId(String email, Long userId) {
        Session session = sessionFactory.getCurrentSession();
        Query query = session.createQuery("select *" +
                "from " + clazz.getName() + " as t" +
                " where t.email = " + email + " and t.user_id = " + userId);
        List<Contact> res = query.getResultList();
        if (res.isEmpty()) return null;
        return res.get(0);
    }
}