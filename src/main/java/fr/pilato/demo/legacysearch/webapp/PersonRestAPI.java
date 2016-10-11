package fr.pilato.demo.legacysearch.webapp;

import com.google.common.base.Optional;
import fr.pilato.demo.legacysearch.domain.Person;
import fr.pilato.demo.legacysearch.service.PersonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.annotations.*;
import restx.factory.Component;
import restx.security.PermitAll;

import javax.inject.Inject;

@Component
@RestxResource("/1/person")
public class PersonRestAPI {
    final Logger logger = LoggerFactory.getLogger(PersonRestAPI.class);

    private final PersonService personService;
    private Integer size;

    @Inject
    public PersonRestAPI(PersonService personService) {
        this.personService = personService;
    }

    @GET("/_byid/{id}")
    @PermitAll
    public Optional<Person> get(Integer id) {
        return Optional.fromNullable(personService.get(id));
    }

    /**
     * Create or update an entity
     */
    @PUT("/{id}")
    @Consumes("application/json")
    @PermitAll
    public Person upsert(Integer id, Person person) {
        logger.debug("upsert({}, {})", id, person);
        Person upsert = personService.upsert(id, person);
        logger.debug("created/updated {}: {}", id, upsert);
        return upsert;
    }

    @DELETE("/{id}")
    @PermitAll
    public void delete(Integer id) {
        logger.debug("Person: {}", id);
        personService.delete(id);
    }

    @GET("/_search")
    @PermitAll
    public String search(Optional<String> q, Optional<String> f_country, Optional<String> f_date,
                         Optional<Integer> from, Optional<Integer> size) {
        return personService.search(q.orNull(), f_country.orNull(), f_date.orNull(), from.or(0), size.or(10));
    }

    @GET("/_advanced_search")
    @PermitAll
    public String advancedSearch(Optional<String> name, Optional<String> country, Optional<String> city,
                                 Optional<Integer> from, Optional<Integer> size) {
        return personService.advancedSearch(name.orNull(), country.orNull(), city.orNull(), from.or(0), size.or(10));
    }

    @GET("/_init")
    @PermitAll
    public InitResult init(Optional<Integer> size) {
        this.size = size.or(1000);
        InitResult result = personService.init(this.size);
        this.size = 0;
        return result;
    }

    @GET("/_init_status")
    @PermitAll
    public InitResult initStatus() {
        return personService.getInitCurrentAchievement();
    }
}
