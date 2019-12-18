package com.softserve.academy.event.repository.impl;

import com.softserve.academy.event.entity.SurveyContact;
import com.softserve.academy.event.repository.SurveyContactConnectorRepository;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class SurveyContactConnectorRepositoryImpl extends BasicRepositoryImpl<SurveyContact, Long> implements SurveyContactConnectorRepository {

    @Override
    public boolean isEnable(Long contactId, Long surveyId) {
        Session session = sessionFactory.getCurrentSession();
        Query query = session.createQuery("select t.enable " +
                "from " + clazz.getName() + " as t" +
                " where t.contact.id = :contactId and t.survey.id = :surveyId")
                .setParameter("surveyId", surveyId)
                .setParameter("contactId", contactId);
        List<Boolean> res = query.getResultList();
        if (res.isEmpty())return false;
        return res.get(0);
    }

    @Override
    public Optional<SurveyContact> findByContactAndSurvey(Long contactId, Long surveyId) {
        List<SurveyContact> result = sessionFactory.getCurrentSession()
                .createQuery("from " + clazz.getName() + " as t" +
                        " where t.contact.id = :contactId and t.survey.id = :surveyId")
                .setParameter("surveyId", surveyId)
                .setParameter("contactId", contactId)
                .getResultList();
        if(result.isEmpty())
            return Optional.empty();
        return Optional.of(result.get(0));
    }
}
