package fr.pilato.demo.legacysearch.webapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.pilato.demo.legacysearch.domain.Person;
import fr.pilato.demo.legacysearch.service.PersonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Controller
@RequestMapping("/1/person")
public class PersonRestAPI {
    final Logger logger = LoggerFactory.getLogger(PersonRestAPI.class);

    @Autowired PersonService personService;

    @RequestMapping(method = RequestMethod.GET, value = "/{id}")
    public @ResponseBody Person get(@PathVariable String id) throws Exception {
        return personService.get(id);
    }

    /**
     * Create or update an entity
     */
    @RequestMapping(method = RequestMethod.PUT, value = "/{id}")
    public @ResponseBody JsonResponse upsert(@PathVariable String id, @RequestBody String json) throws Exception {
        if (logger.isDebugEnabled()) logger.debug("create({}, {})", id, json);

        // We try to find an existing document
        ObjectMapper mapper = new ObjectMapper();
        Person person = get(id);

        if (person == null) {
            person = mapper.readValue(json, Person.class);
            person.setReference(id);
        } else {
            mapper.readerForUpdating(person).readValue(json);
        }

        if (logger.isDebugEnabled()) logger.debug("create/update {}: {}", id, person);
        person = personService.save(person);

        return new JsonResponse(true, person.getReference());
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
    public @ResponseBody JsonResponse delete(@PathVariable String id) throws Exception {
        if (logger.isDebugEnabled()) logger.debug("Person: {}", id);

        if (id == null) {
            return new JsonResponse(false);
        }

        return new JsonResponse(personService.delete(id));
    }

    @RequestMapping(method = RequestMethod.POST, value = "/_search", params = {"q","f_country","f_date","size","from"})
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public @ResponseBody
    String search(@RequestParam String q,
                  @RequestParam String f_country,
                  @RequestParam String f_date,
                  @RequestParam Integer from,
                  @RequestParam Integer size) throws Exception {
        return personService.search(q, f_country, f_date, from, size);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/_search", params = {"name","country","city","size","from"})
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public @ResponseBody
    String advancedSearch(@RequestParam String name,
                          @RequestParam String country,
                          @RequestParam String city,
                          @RequestParam Integer from,
                          @RequestParam Integer size) throws Exception {
        return personService.advancedSearch(name, country, city, from, size);
    }


    @RequestMapping(method = RequestMethod.POST, value = "/_init", params = {"size"})
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public @ResponseBody JsonResponse init(@RequestParam Integer size) throws IOException {
        return new JsonResponse(personService.init(size));
    }

    class JsonResponse {
        private boolean ok;
        private String reference;

        JsonResponse(boolean ok) {
            this.ok = ok;
            this.reference = null;
        }

        JsonResponse(boolean ok, String reference) {
            this.ok = ok;
            this.reference = reference;
        }

        public String getReference() {
            return reference;
        }

        public boolean isOk() {
            return ok;
        }
    }
}
